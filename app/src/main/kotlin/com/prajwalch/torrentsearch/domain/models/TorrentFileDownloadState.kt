package com.prajwalch.torrentsearch.domain.models

sealed interface TorrentFileDownloadState {
    data object Empty : TorrentFileDownloadState
    
    data object Downloading : TorrentFileDownloadState

    data class Success(val fileName: String) : TorrentFileDownloadState
}