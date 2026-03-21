package com.prajwalch.torrentsearch.ui.settings.searchproviders.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow

import com.prajwalch.torrentsearch.ui.component.TextUrl

@Composable
fun SearchProviderUrl(url: String, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val isHttps = url.startsWith("https://")

    if (isHttps) {
        TextUrl(
            modifier = modifier,
            text = url.removePrefix("https://"),
            onClick = { uriHandler.openUri(url) },
        )
    } else {
        Text(
            modifier = modifier,
            text = url,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
        )
    }
}