package com.prajwalch.torrentsearch.ui.search.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import com.prajwalch.torrentsearch.R
import com.prajwalch.torrentsearch.ui.component.EmptyPlaceholder

@Composable
fun NoInternetConnection(onTryAgain: () -> Unit, modifier: Modifier = Modifier) {
    EmptyPlaceholder(
        modifier = modifier,
        icon = R.drawable.ic_signal_wifi_off,
        title = R.string.search_internet_connection_error,
        actions = { TryAgainButton(onClick = onTryAgain) }
    )
}