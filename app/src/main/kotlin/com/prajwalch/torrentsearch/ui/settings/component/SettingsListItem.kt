package com.prajwalch.torrentsearch.ui.settings.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

@Composable
fun SettingsListItem(
    onClick: () -> Unit,
    @DrawableRes icon: Int,
    @StringRes headline: Int,
    modifier: Modifier = Modifier,
    supportingContent: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(modifier),
        leadingContent = {
            Icon(
                painter = painterResource(icon),
                contentDescription = stringResource(headline),
            )
        },
        headlineContent = { Text(text = stringResource(headline)) },
        supportingContent = supportingContent?.let { { Text(text = it) } },
        trailingContent = trailingContent,
    )
}