package com.prajwalch.torrentsearch.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.prajwalch.torrentsearch.data.repository.BookmarksRepository
import com.prajwalch.torrentsearch.data.repository.SearchHistoryRepository
import com.prajwalch.torrentsearch.data.repository.SettingsRepository
import com.prajwalch.torrentsearch.domain.SearchTorrentsUseCase
import com.prajwalch.torrentsearch.domain.model.Category
import com.prajwalch.torrentsearch.domain.model.SearchResults
import com.prajwalch.torrentsearch.domain.model.SortCriteria
import com.prajwalch.torrentsearch.domain.model.SortOptions
import com.prajwalch.torrentsearch.domain.model.SortOrder
import com.prajwalch.torrentsearch.domain.model.Torrent
import com.prajwalch.torrentsearch.network.ConnectivityChecker
import com.prajwalch.torrentsearch.torrentfiledownloader.TorrentFileDownloadState
import com.prajwalch.torrentsearch.torrentfiledownloader.TorrentFileDownloader
import com.prajwalch.torrentsearch.util.createSortComparator

import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import java.io.OutputStream
import javax.inject.Inject

data class SearchUiState(
    val searchQuery: String = "",
    val searchCategory: Category = Category.All,
    val searchResults: SearchResults = SearchResults(),
    val sortOptions: SortOptions = SortOptions(),
    val filterOptions: FilterOptions = FilterOptions(),
    val torrentFileDownloadState: TorrentFileDownloadState = TorrentFileDownloadState.Empty,
    val isLoading: Boolean = true,
    val isSearching: Boolean = false,
    val isRefreshing: Boolean = false,
    val isInternetError: Boolean = false,
    val resultsNotFound: Boolean = false,
    val resultsFilteredOut: Boolean = false,
)

data class FilterOptions(
    val searchProviders: ImmutableList<SearchProviderFilterOption> = persistentListOf(),
    val deadTorrents: Boolean = true,
)

data class SearchProviderFilterOption(
    val searchProviderName: String,
    val selected: Boolean = false,
)

/**
 * A ViewModel that handles the business logic of search screen.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchTorrentsUseCase: SearchTorrentsUseCase,
    private val bookmarksRepository: BookmarksRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
    private val settingsRepository: SettingsRepository,
    private val connectivityChecker: ConnectivityChecker,
    private val torrentFileDownloader: TorrentFileDownloader,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val searchQuery = savedStateHandle.get<String>("query")
        ?: error("SearchViewModel can't function without a search query")
    private val searchCategory = savedStateHandle.get<Category>("category") ?: Category.All
    private val searchOrchestrator = SearchOrchestrator(
        scope = viewModelScope,
        searchTorrentsUseCase = searchTorrentsUseCase,
        connectivityChecker = connectivityChecker,
    )
    private val searchResultsProcessor = SearchResultsProcessor(
        searchResults = searchOrchestrator.searchResults,
        settingsRepository = settingsRepository,
    )

    val torrentFileDownloadState = torrentFileDownloader.state
    val torrentFileDownloadEvents = torrentFileDownloader.events

    val uiState = combine(
        searchOrchestrator.state,
        searchResultsProcessor.state
    ) { searchState, processedState ->
        val resultsFilteredOut = when {
            searchState.isSearching -> false
            searchState.isRefreshing -> false
            searchState.resultsNotFound -> false
            else -> processedState.results.successes.isEmpty()
        }

        SearchUiState(
            searchQuery = searchQuery,
            searchCategory = searchCategory,
            searchResults = processedState.results,
            sortOptions = processedState.sortOptions,
            filterOptions = processedState.filterOptions,
            isLoading = searchState.isLoading,
            isSearching = searchState.isSearching,
            isRefreshing = searchState.isRefreshing,
            isInternetError = searchState.isInternetError,
            resultsNotFound = searchState.resultsNotFound,
            resultsFilteredOut = resultsFilteredOut,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchUiState(isLoading = true),
    )

    init {
        saveSearchQuery()
        // Initiate search.
        viewModelScope.launch {
            val defaultSortOptions = settingsRepository.defaultSortOptions.first()
            searchResultsProcessor.updateSortCriteria(defaultSortOptions.criteria)
            searchResultsProcessor.updateSortOrder(defaultSortOptions.order)

            searchOrchestrator.search(searchQuery, searchCategory)
        }
    }

    private fun saveSearchQuery() = viewModelScope.launch {
        if (settingsRepository.saveSearchHistory.first()) {
            searchHistoryRepository.createNewSearchHistory(query = searchQuery)
        }
    }

    fun refreshSearchResults() {
        searchOrchestrator.refresh(searchQuery, searchCategory)
    }

    fun reload() {
        searchOrchestrator.search(searchQuery, searchCategory)
    }

    fun filterSearchResults(query: String) {
        searchResultsProcessor.updateFilterQuery(query)
    }

    fun updateSortCriteria(criteria: SortCriteria) {
        searchResultsProcessor.updateSortCriteria(criteria)
    }

    fun updateSortOrder(order: SortOrder) {
        searchResultsProcessor.updateSortOrder(order)
    }

    fun toggleSearchProviderResults(providerName: String) {
        searchResultsProcessor.toggleSearchProviderResults(providerName)
    }

    fun toggleDeadTorrents() {
        searchResultsProcessor.toggleDeadTorrents()
    }

    fun bookmarkTorrent(torrent: Torrent) {
        viewModelScope.launch {
            bookmarksRepository.bookmarkTorrent(torrent = torrent)
        }
    }

    fun downloadTorrentFile(url: String, fileName: String) {
        viewModelScope.launch {
            torrentFileDownloader.downloadFile(url = url, fileName = fileName)
        }
    }

    fun writeTorrentFile(outputStream: OutputStream) {
        viewModelScope.launch {
            torrentFileDownloader.writeFile(outputStream = outputStream)
        }
    }

    //    /** Creates search providers filter option from the given search results. */
//    private fun createSearchProvidersFilterOption(
//        searchResults: ImmutableList<Torrent>,
//    ): ImmutableList<SearchProviderFilterOption> {
//        return searchResults
//            .asSequence()
//            .distinctBy { it.providerName }
//            .map { it.providerName }
//            .sorted()
//            .map { SearchProviderFilterOption(searchProviderName = it, selected = true) }
//            .toImmutableList()
//    }
}

/**
 * A helper class which simply coordinates search and exposes different states
 * and the results during the entire search lifecycle (i.e. from start to end).
 *
 * It doesn't process or inspect any search results, it's up to the caller to
 * take [searchResults] and perform different transformation.
 */
private class SearchOrchestrator(
    /**
     * A [CoroutineScope] in which the search should be performed.
     */
    private val scope: CoroutineScope,
    /**
     * A primary class which handles the actual search.
     */
    private val searchTorrentsUseCase: SearchTorrentsUseCase,
    /**
     * A helper class for checking network connectivity status.
     */
    private val connectivityChecker: ConnectivityChecker,
) {
    /**
     * Represents different states of search.
     */
    data class SearchState(
        val isLoading: Boolean = true,
        val isSearching: Boolean = false,
        val isRefreshing: Boolean = false,
        val isInternetError: Boolean = false,
        val resultsNotFound: Boolean = false,
    )

    /**
     * A mutable stream of [SearchState] for emitting new state to [state].
     */
    private val _state = MutableStateFlow(SearchState())

    /**
     * A stream of [SearchState].
     */
    val state = _state.asStateFlow()

    /**
     * A mutable stream of [SearchResults] for emitting a new state
     * to [searchResults].
     */
    private val _searchResults = MutableStateFlow(SearchResults())

    /**
     * A stream of [SearchResults].
     */
    val searchResults = _searchResults.asStateFlow()

    /**
     * An ongoing background search job.
     */
    private var searchJob: Job? = null

    /**
     * Initiates a new search for given query and category.
     */
    fun search(query: String, category: Category) {
        searchJob?.cancel()
        searchJob = scope.launch {
            _state.value = SearchState(isLoading = true)

            if (!isInternetAvailable()) {
                _state.update { it.copy(isLoading = false, isInternetError = true) }
                return@launch
            }

            executeSearch(query = query, category = category)
        }
    }

    /**
     * Initiates a refresh task for given query and category.
     *
     * Any failures or interruptions will simply cause the refresh task to
     * stop immediately without clearing previous search results.
     */
    fun refresh(query: String, category: Category) {
        searchJob?.cancel()
        searchJob = scope.launch {
            _state.update { it.copy(isRefreshing = true) }

            if (!isInternetAvailable()) {
                _state.update { it.copy(isRefreshing = false) }
                return@launch
            }

            executeSearch(query = query, category = category)
        }
    }

    /**
     * Returns `true` if the internet is available for performing a search.
     */
    private suspend fun isInternetAvailable(): Boolean {
        return connectivityChecker.isInternetAvailable()
    }

    /**
     * Executes a search for a given query and category, updating state
     * throughout the entire search lifecycle.
     */
    private suspend fun executeSearch(query: String, category: Category) {
        searchTorrentsUseCase(query = query, category = category)
            .conflate()
            .onStart { onSearchStart() }
            .onCompletion { onSearchCompletion() }
            .collect { _searchResults.value = it }
    }

    /**
     * Invoked when search starts.
     */
    private fun onSearchStart() {
        _state.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                isSearching = true,
            )
        }
    }

    /**
     * Invoked when search completes either normally or abnormally.
     */
    private fun onSearchCompletion() {
        _state.update {
            it.copy(
                isSearching = false,
                resultsNotFound = _searchResults.value.successes.isEmpty(),
            )
        }
    }
}

/**
 * A helper class for performing filter and sort operations.
 *
 * It simply takes [Flow] of [SearchResults], applies different transformations
 * and exposes the processed results together with the values it used to do the
 * transformation via the [state].
 */
private class SearchResultsProcessor(
    /**
     * A stream of [SearchResults] which needed to process
     */
    searchResults: Flow<SearchResults>,
    /**
     * A settings repository for fetching the user-defined transformation values.
     */
    settingsRepository: SettingsRepository,
) {
    /**
     * A final state after the transformation.
     */
    data class ProcessedState(
        val results: SearchResults,
        val filterOptions: FilterOptions,
        val sortOptions: SortOptions,
    )

    // Transformation values.
    private val filterQuery = MutableStateFlow("")
    private val filterOptions = MutableStateFlow(FilterOptions())
    private val sortOptions = MutableStateFlow(SortOptions())

    /**
     * A stream of [ProcessedState] for real-time observation.
     *
     * A new final state is emitted each time a new search results is received
     * or transformation values changes.
     */
    val state = combine(
        searchResults,
        filterQuery,
        filterOptions,
        sortOptions,
        settingsRepository.enableNSFWMode,
    ) { searchResults, filterQuery, filters, sortOptions, nsfwModeEnabled ->
        val filteredResults = processSearchResults(
            rawSearchResults = searchResults,
            filterQuery = filterQuery,
            filters = filters,
            sortOptions = sortOptions,
            nsfwModeEnabled = nsfwModeEnabled,
        )
        ProcessedState(
            results = filteredResults,
            filterOptions = filters,
            sortOptions = sortOptions,
        )
    }.flowOn(Dispatchers.Default)

    /**
     * Processes and returns a new search results based on given values.
     */
    private fun processSearchResults(
        rawSearchResults: SearchResults,
        filterQuery: String,
        filters: FilterOptions,
        sortOptions: SortOptions,
        nsfwModeEnabled: Boolean,
    ): SearchResults {
        val enabledSearchProvidersName = filters
            .searchProviders
            .filter { it.selected }
            .map { it.searchProviderName }
        val sortComparator = createSortComparator(
            criteria = sortOptions.criteria,
            order = sortOptions.order,
        )
        val processedSuccesses = rawSearchResults
            .successes
            .asSequence()
            .filter {
                filters.searchProviders.isEmpty() || it.providerName in enabledSearchProvidersName
            }
            .filter { nsfwModeEnabled || !it.isNSFW() }
            .filter { filters.deadTorrents || !it.isDead() }
            .filter { filterQuery.isBlank() || it.name.contains(filterQuery, ignoreCase = true) }
            .sortedWith(comparator = sortComparator)
            .toImmutableList()

        return SearchResults(
            successes = processedSuccesses,
            failures = rawSearchResults.failures,
        )
    }

    /**
     * Shows only those search results that contains the given query.
     */
    fun updateFilterQuery(query: String) {
        filterQuery.value = if (query.isNotBlank()) query.trim() else query
    }

    /**
     * Shows or hides search results based on the given provider name.
     */
    fun toggleSearchProviderResults(providerName: String) {
        val currentSearchProvidersFilters = filterOptions.value.searchProviders
        val updatedSearchProvidersFilters = currentSearchProvidersFilters
            .map { if (it.searchProviderName == providerName) it.copy(selected = !it.selected) else it }
            .toImmutableList()

        filterOptions.update {
            it.copy(searchProviders = updatedSearchProvidersFilters)
        }
    }

    /**
     * Shows or hides dead torrents from search results.
     */
    fun toggleDeadTorrents() {
        filterOptions.update {
            it.copy(deadTorrents = !it.deadTorrents)
        }
    }

    /**
     * Updates the current sort criteria with the given one.
     */
    fun updateSortCriteria(criteria: SortCriteria) {
        sortOptions.update { it.copy(criteria = criteria) }
    }

    /**
     * Updates the current sort order with the given one.
     */
    fun updateSortOrder(order: SortOrder) {
        sortOptions.update { it.copy(order = order) }
    }
}