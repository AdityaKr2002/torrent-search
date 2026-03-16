package com.prajwalch.torrentsearch.ui.bookmarks

import android.content.ContentResolver
import android.net.Uri

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.prajwalch.torrentsearch.data.repository.BookmarksRepository
import com.prajwalch.torrentsearch.data.repository.SettingsRepository
import com.prajwalch.torrentsearch.data.repository.TorrentsRepository
import com.prajwalch.torrentsearch.domain.models.SortCriteria
import com.prajwalch.torrentsearch.domain.models.SortOptions
import com.prajwalch.torrentsearch.domain.models.SortOrder
import com.prajwalch.torrentsearch.domain.models.Torrent
import com.prajwalch.torrentsearch.domain.models.TorrentFileDownloadState
import com.prajwalch.torrentsearch.utils.createSortComparator

import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

import kotlin.time.Duration.Companion.seconds

/** UI state for the Bookmarks screen. */
data class BookmarksUiState(
    val bookmarks: List<Torrent> = emptyList(),
    val sortOptions: SortOptions = SortOptions(),
    val torrentFileDownloadState: TorrentFileDownloadState = TorrentFileDownloadState.Empty,
)

/** ViewModel that handles the business logic of Bookmarks screen. */
@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val bookmarksRepository: BookmarksRepository,
    private val torrentsRepository: TorrentsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val filterQuery = MutableStateFlow("")
    private val torrentFileDownloadState =
        MutableStateFlow<TorrentFileDownloadState>(TorrentFileDownloadState.Empty)

    val uiState = combine(
        filterQuery,
        torrentFileDownloadState,
        bookmarksRepository.observeAllBookmarks(),
        settingsRepository.enableNSFWMode,
        settingsRepository.bookmarksSortOptions,
    ) { filterQuery, torrentFileDownloadState, bookmarks, nsfwModeEnabled, sortOptions ->
        val bookmarks = bookmarks
            .filter { nsfwModeEnabled || !it.isNSFW() }
            .filter { filterQuery.isBlank() || it.name.contains(filterQuery, ignoreCase = true) }
            .sortedWith(
                createSortComparator(criteria = sortOptions.criteria, order = sortOptions.order)
            )

        BookmarksUiState(
            bookmarks = bookmarks,
            sortOptions = sortOptions,
            torrentFileDownloadState = torrentFileDownloadState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5.seconds),
        initialValue = BookmarksUiState(),
    )

    private var downloadedTorrentFileContent: ByteArray? = null

    /** Deletes the given bookmarked torrent. */
    fun deleteBookmarkedTorrent(torrent: Torrent) {
        viewModelScope.launch {
            bookmarksRepository.deleteBookmarkedTorrent(torrent)
        }
    }

    /** Deletes all bookmarks. */
    fun deleteAllBookmarks() {
        viewModelScope.launch {
            bookmarksRepository.deleteAllBookmarks()
        }
    }

    /** Sets or updates the sort criteria. */
    fun setSortCriteria(criteria: SortCriteria) {
        viewModelScope.launch {
            settingsRepository.setBookmarksSortCriteria(criteria)
        }
    }

    /** Sets or updates the sort order. */
    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch {
            settingsRepository.setBookmarksSortOrder(order)
        }
    }

    /** Filters the bookmarks using the given query. */
    fun filterBookmarks(query: String) {
        filterQuery.value = query
    }

    /** Attempts to import bookmarks from the given stream. */
    fun importBookmarks(inputStream: InputStream) {
        viewModelScope.launch {
            bookmarksRepository.importBookmarks(inputStream = inputStream)
        }
    }

    /** Attempts to export bookmarks to the given stream. */
    fun exportBookmarks(outputStream: OutputStream) {
        viewModelScope.launch {
            bookmarksRepository.exportBookmarks(outputStream = outputStream)
        }
    }

    fun downloadTorrentFile(url: String, fileName: String) {
        torrentFileDownloadState.value = TorrentFileDownloadState.Downloading

        viewModelScope.launch {
            val content = torrentsRepository.downloadTorrentFile(url = url)
            downloadedTorrentFileContent = content
            torrentFileDownloadState.value = TorrentFileDownloadState.Success(fileName)
        }
    }

    fun writeTorrentFile(fileUri: Uri, contentResolver: ContentResolver) {
        val content = downloadedTorrentFileContent ?: return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val outputStream = contentResolver.openOutputStream(fileUri) ?: return@withContext
                outputStream.use { it.write(content) }
            }
            downloadedTorrentFileContent = null
        }
    }

    fun clearDownloadedTorrentFile() {
        torrentFileDownloadState.value = TorrentFileDownloadState.Empty
        downloadedTorrentFileContent = null
    }
}