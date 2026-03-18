package com.prajwalch.torrentsearch.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

import com.prajwalch.torrentsearch.torrentfiledownloader.TorrentFileDownloadEvent
import com.prajwalch.torrentsearch.torrentfiledownloader.TorrentFileDownloadState

import kotlinx.coroutines.flow.Flow
import java.io.OutputStream

private const val TORRENT_FILE_MIME_TYPE = "application/x-bittorrent"

@Composable
fun TorrentFileDownloadEffect(
    onWrite: (OutputStream) -> Unit,
    state: TorrentFileDownloadState,
    events: Flow<TorrentFileDownloadEvent>,
    snackbarHostState: SnackbarHostState,
) {
    val contentResolver = LocalContext.current.contentResolver

    val createTorrentFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument(TORRENT_FILE_MIME_TYPE),
    ) { fileUri ->
        fileUri?.let(contentResolver::openOutputStream)?.let(onWrite)
    }

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event) {
                is TorrentFileDownloadEvent.ReadyToWrite -> {
                    createTorrentFileLauncher.launch(event.fileName)
                }

                TorrentFileDownloadEvent.WriteSucceed -> {
                    snackbarHostState.showSnackbar("Contents write succeed")
                }
            }
        }
    }

    LaunchedEffect(state) {
        when (state) {
            TorrentFileDownloadState.Empty -> {
                /* Do nothing */
            }

            TorrentFileDownloadState.Downloading -> {
                snackbarHostState.showSnackbar(
                    message = "Downloading .torrent file contents",
                    duration = SnackbarDuration.Indefinite,
                )
            }

            TorrentFileDownloadState.Writing -> {
                snackbarHostState.showSnackbar(
                    message = "Writing contents to file",
                    duration = SnackbarDuration.Indefinite,
                )
            }
        }
    }
}