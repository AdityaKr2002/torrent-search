package com.prajwalch.torrentsearch.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import com.prajwalch.torrentsearch.data.DarkTheme
import com.prajwalch.torrentsearch.data.MaxNumResults
import com.prajwalch.torrentsearch.data.SearchHistoryRepository
import com.prajwalch.torrentsearch.data.SettingsRepository
import com.prajwalch.torrentsearch.models.Category
import com.prajwalch.torrentsearch.providers.SearchProviderId
import com.prajwalch.torrentsearch.providers.SearchProviderSafetyStatus
import com.prajwalch.torrentsearch.providers.SearchProviders

import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** State for the appearance settings. */
data class AppearanceSettingsUiState(
    val enableDynamicTheme: Boolean = true,
    val darkTheme: DarkTheme = DarkTheme.FollowSystem,
    val pureBlack: Boolean = false,
)

/** State for the general settings. */
data class GeneralSettingsUiState(
    val defaultCategory: Category = Category.All,
    val enableNSFWMode: Boolean = false,
)

/** State for the search settings. */
data class SearchSettingsUiState(
    val hideResultsWithZeroSeeders: Boolean = false,
    val searchProviders: List<SearchProviderUiState> = emptyList(),
    val totalSearchProviders: Int = SearchProviders.infos().size,
    val enabledSearchProviders: Int = 0,
    val maxNumResults: MaxNumResults = MaxNumResults.Unlimited,
)

/** State for the search history settings. */
data class SearchHistorySettingsUiState(
    val saveSearchHistory: Boolean = true,
    val showSearchHistory: Boolean = true,
)

/** State for the search providers list. */
data class SearchProviderUiState(
    val id: SearchProviderId,
    val name: String,
    val url: String,
    val specializedCategory: Category,
    val safetyStatus: SearchProviderSafetyStatus,
    val enabled: Boolean,
)

/** ViewModel that handles the business logic of Settings screen. */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val searchHistoryRepository: SearchHistoryRepository,
) : ViewModel() {
    /** All search providers (enabled + disabled). */
    private val allSearchProviders = SearchProviders.infos()

    /**
     * Currently enabled search providers.
     *
     * Settings screen receives all the providers with enable/disable
     * state instead of only enabled ones and reports state change event
     * for only one at a time through [enableSearchProvider].
     *
     * Only then we will create a set of enabled providers and pass them to
     * repository like how it expects.
     */
    private var enabledSearchProviders = settingsRepository
        .searchProviders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet(),
        )

    val appearanceSettingsUiState = combine(
        settingsRepository.enableDynamicTheme,
        settingsRepository.darkTheme,
        settingsRepository.pureBlack,
        ::AppearanceSettingsUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppearanceSettingsUiState()
    )

    val generalSettingsUiState = combine(
        settingsRepository.defaultCategory,
        settingsRepository.enableNSFWMode,
        ::GeneralSettingsUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = GeneralSettingsUiState()
    )

    val searchSettingsUiState = combine(
        settingsRepository.hideResultsWithZeroSeeders,
        settingsRepository.maxNumResults,
    ) { hideResultsWithZeroSeeders, maxNumResults ->
        SearchSettingsUiState(
            hideResultsWithZeroSeeders = hideResultsWithZeroSeeders,
            searchProviders = allSearchProvidersToUiStates(),
            totalSearchProviders = allSearchProviders.size,
            enabledSearchProviders = enabledSearchProviders.value.size,
            maxNumResults = maxNumResults,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SearchSettingsUiState()
    )

    val searchHistorySettingsUiState = combine(
        settingsRepository.saveSearchHistory,
        settingsRepository.showSearchHistory,
        ::SearchHistorySettingsUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SearchHistorySettingsUiState()
    )

    /** Converts list of search provider to list of UI states. */
    private fun allSearchProvidersToUiStates(): List<SearchProviderUiState> {
        return allSearchProviders.map { searchProviderInfo ->
            SearchProviderUiState(
                id = searchProviderInfo.id,
                name = searchProviderInfo.name,
                url = searchProviderInfo.url.removePrefix("https://"),
                specializedCategory = searchProviderInfo.specializedCategory,
                safetyStatus = searchProviderInfo.safetyStatus,
                enabled = enabledSearchProviders.value.contains(searchProviderInfo.id)
            )
        }
    }

    /** Enables/disables dynamic theme. */
    fun updateEnableDynamicTheme(enable: Boolean) {
        viewModelScope.launch { settingsRepository.updateEnableDynamicTheme(enable) }
    }

    /** Changes the dark theme mode. */
    fun updateDarkTheme(darkTheme: DarkTheme) {
        viewModelScope.launch { settingsRepository.updateDarkTheme(darkTheme) }
    }

    /** Enables/disables pure black mode. */
    fun updatePureBlack(enable: Boolean) {
        viewModelScope.launch { settingsRepository.updatePureBlack(enable) }
    }

    /** Changes the default category to given one. */
    fun updateDefaultCategory(category: Category) {
        viewModelScope.launch { settingsRepository.updateDefaultCategory(category) }
    }

    /** Enables/disables NSFW mode. */
    fun updateEnableNSFWMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateEnableNSFWMode(enabled)
            if (!enabled) disableRestrictedSearchProviders()
        }
    }

    /** Disables NSFW and Unsafe search providers which are currently enabled. */
    private suspend fun disableRestrictedSearchProviders() {
        val newEnabledSearchProviders = allSearchProviders
            .filter { enabledSearchProviders.value.contains(it.id) }
            .filter { !it.specializedCategory.isNSFW && !it.safetyStatus.isUnsafe() }
            .map { it.id }
            .toSet()

        if (newEnabledSearchProviders.isEmpty()) {
            return
        }

        if (newEnabledSearchProviders != enabledSearchProviders.value) {
            settingsRepository.updateSearchProviders(newEnabledSearchProviders.toSet())
        }
    }

    /** Enables/disables an option to hide zero seeders. */
    fun updateHideResultsWithZeroSeeders(enable: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateHideResultsWithZeroSeeders(enable)
        }
    }

    /** Enables/disables search provider associated with given id. */
    fun enableSearchProvider(providerId: SearchProviderId, enable: Boolean) {
        val updatedSearchProviders = if (enable) {
            enabledSearchProviders.value + providerId
        } else {
            enabledSearchProviders.value - providerId
        }

        viewModelScope.launch {
            settingsRepository.updateSearchProviders(providers = updatedSearchProviders)
        }
    }

    /** Enables all search providers. */
    fun enableAllSearchProviders() {
        viewModelScope.launch {
            val allSearchProvidersId = SearchProviders.infos().map { it.id }.toSet()
            settingsRepository.updateSearchProviders(providers = allSearchProvidersId)
        }
    }

    /** Disables all search providers. */
    fun disableAllSearchProviders() {
        viewModelScope.launch {
            settingsRepository.updateSearchProviders(providers = emptySet())
        }
    }

    /** Resets search providers to default. */
    fun resetSearchProvidersToDefault() {
        viewModelScope.launch {
            settingsRepository.updateSearchProviders(providers = SearchProviders.enabledIds())
        }
    }

    /** Updates the maximum number of results. */
    fun updateMaxNumResults(maxNumResults: MaxNumResults) {
        viewModelScope.launch { settingsRepository.updateMaxNumResults(maxNumResults) }
    }

    /** Saves/unsaves search history. */
    fun saveSearchHistory(save: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSaveSearchHistory(save)
        }
    }

    /** Shows/hides search history. */
    fun showSearchHistory(show: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateShowSearchHistory(show = show)
        }
    }

    /** Clears all search history. */
    fun clearSearchHistory() {
        viewModelScope.launch {
            searchHistoryRepository.clearAll()
        }
    }

    companion object {
        /** Provides a factor function for [SettingsViewModel]. */
        fun provideFactory(
            settingsRepository: SettingsRepository,
            searchHistoryRepository: SearchHistoryRepository,
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        settingsRepository = settingsRepository,
                        searchHistoryRepository = searchHistoryRepository,
                    ) as T
                }
            }
        }
    }
}