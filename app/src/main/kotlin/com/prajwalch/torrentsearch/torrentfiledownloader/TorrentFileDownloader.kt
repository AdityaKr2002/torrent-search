package com.prajwalch.torrentsearch.torrentfiledownloader

import com.prajwalch.torrentsearch.data.repository.TorrentFileId
import com.prajwalch.torrentsearch.data.repository.TorrentsRepository

import dagger.hilt.android.scopes.ViewModelScoped

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

import java.io.OutputStream
import javax.inject.Inject

sealed interface TorrentFileDownloadState {
    data object Empty : TorrentFileDownloadState

    data object Downloading : TorrentFileDownloadState

    data object Writing : TorrentFileDownloadState
}

sealed interface TorrentFileDownloadEvent {
    data class ReadyToWrite(val fileName: String) : TorrentFileDownloadEvent

    data object WriteSucceed : TorrentFileDownloadEvent
}

@ViewModelScoped
class TorrentFileDownloader @Inject constructor(
    private val torrentsRepository: TorrentsRepository,
) {
    private val _state = MutableStateFlow<TorrentFileDownloadState>(TorrentFileDownloadState.Empty)
    val state = _state.asStateFlow()

    private val _events = Channel<TorrentFileDownloadEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var pendingFileId: TorrentFileId? = null

    suspend fun downloadFile(url: String, fileName: String) {
        reset()

        _state.value = TorrentFileDownloadState.Downloading
        pendingFileId = torrentsRepository.downloadTorrentFile(url = url)

        _events.send(TorrentFileDownloadEvent.ReadyToWrite(fileName))
        _state.value = TorrentFileDownloadState.Empty
    }

    suspend fun writeFile(outputStream: OutputStream) {
        val pendingFileId = pendingFileId ?: return

        _state.value = TorrentFileDownloadState.Writing
        torrentsRepository.writeTorrentFile(fileId = pendingFileId, outputStream = outputStream)

        _events.send(TorrentFileDownloadEvent.WriteSucceed)
        reset()
    }

    fun reset() {
        _state.value = TorrentFileDownloadState.Empty
        pendingFileId = null
    }
}