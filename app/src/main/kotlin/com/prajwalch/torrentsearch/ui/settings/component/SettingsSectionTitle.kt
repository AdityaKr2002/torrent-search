package com.prajwalch.torrentsearch.ui.settings.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle

import com.prajwalch.torrentsearch.ui.theme.spaces

@Composable
fun SettingsSectionTitle(
    @StringRes title: Int,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.titleSmall,
) {
    Text(
        modifier = modifier.padding(MaterialTheme.spaces.large),
        text = stringResource(title),
        color = color,
        style = style,
    )
}