package com.prajwalch.torrentsearch.ui.settings.component

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

import com.prajwalch.torrentsearch.R
import com.prajwalch.torrentsearch.domain.model.DarkTheme
import com.prajwalch.torrentsearch.ui.component.RoundedDropdownMenu
import com.prajwalch.torrentsearch.ui.darkThemeStringResource

@Composable
fun DarkThemeOptionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    selectedOption: DarkTheme,
    onOptionSelect: (DarkTheme) -> Unit,
    modifier: Modifier = Modifier,
) {
    RoundedDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset(x = 56.dp, y = 0.dp),
    ) {
        DarkTheme.entries.forEach {
            DropdownMenuItem(
                text = { Text(text = darkThemeStringResource(it)) },
                onClick = { onOptionSelect(it) },
                leadingIcon = {
                    if (it == selectedOption) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                        )
                    }
                },
            )
        }
    }
}