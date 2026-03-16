package com.prajwalch.torrentsearch.ui

import android.net.Uri

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
fun rememberCreateTorrentFileLauncher(
    action: (Uri?) -> Unit,
): ManagedActivityResultLauncher<String, Uri?> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/x-bittorrent"),
    ) { fileUri ->
        action(fileUri)
    }
}