package com.prajwalch.torrentsearch.data.repository

import com.prajwalch.torrentsearch.data.remote.TorrentsRemoteDataSource
import com.prajwalch.torrentsearch.domain.model.Category
import com.prajwalch.torrentsearch.domain.model.SearchException
import com.prajwalch.torrentsearch.domain.model.SearchResults
import com.prajwalch.torrentsearch.domain.model.Torrent
import com.prajwalch.torrentsearch.providers.SearchProvider

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.withContext

import java.io.OutputStream
import java.util.UUID

import javax.inject.Inject

typealias TorrentFileId = UUID

class TorrentsRepository @Inject constructor(
    private val remoteDataSource: TorrentsRemoteDataSource,
) {
    private val torrentFileContentCache = mutableMapOf<TorrentFileId, ByteArray>()

    fun search(
        query: String,
        category: Category,
        searchProviders: List<SearchProvider>,
    ) = remoteDataSource.searchTorrents(
        query = query,
        category = category,
        searchProviders = searchProviders,
    ).scan(SearchResults()) { searchResults, batchResult ->
        processBatchResult(
            currentSearchResults = searchResults,
            batchResult = batchResult,
            category = category,
        )
    }.flowOn(Dispatchers.IO)

    private fun processBatchResult(
        currentSearchResults: SearchResults,
        batchResult: Result<List<Torrent>>,
        category: Category,
    ): SearchResults = batchResult.fold(
        onSuccess = {
            val newSuccesses = filterTorrentsByCategory(torrents = it, category = category)
            currentSearchResults.appendSuccesses(newSuccesses)
        },
        onFailure = {
            currentSearchResults.appendFailure(it as SearchException)
        },
    )

    private fun filterTorrentsByCategory(
        torrents: List<Torrent>,
        category: Category,
    ): List<Torrent> {
        return if (category == Category.All) {
            torrents
        } else {
            torrents.filter { it.category == category }
        }
    }

    suspend fun downloadTorrentFile(url: String): TorrentFileId {
        val id = UUID.nameUUIDFromBytes(url.toByteArray())
        if (torrentFileContentCache.containsKey(id)) return id

        val fileContent = remoteDataSource.downloadTorrentFile(url = url)
        torrentFileContentCache[id] = fileContent

        return id
    }

    suspend fun writeTorrentFile(
        fileId: TorrentFileId,
        outputStream: OutputStream,
    ) = withContext(Dispatchers.IO) {
        val fileContent = torrentFileContentCache[fileId]
        fileContent?.let(outputStream::write)
    }
}

private fun SearchResults.appendSuccesses(successes: List<Torrent>): SearchResults {
    return this.copy(successes = this.successes.plus(successes).toImmutableList())
}

private fun SearchResults.appendFailure(failure: SearchException): SearchResults {
    return this.copy(failures = this.failures.plus(failure).toImmutableList())
}