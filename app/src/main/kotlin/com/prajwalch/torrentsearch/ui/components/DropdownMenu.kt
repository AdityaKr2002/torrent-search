package com.prajwalch.torrentsearch.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

import com.prajwalch.torrentsearch.R
import com.prajwalch.torrentsearch.models.SortCriteria
import com.prajwalch.torrentsearch.models.SortOrder

@Composable
fun RoundedDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    shape: Shape = MaterialTheme.shapes.medium,
    containerColor: Color = MenuDefaults.containerColor,
    tonalElevation: Dp = MenuDefaults.TonalElevation,
    shadowElevation: Dp = MenuDefaults.ShadowElevation,
    border: BorderStroke? = null,
    content: @Composable (ColumnScope.() -> Unit),
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        scrollState = scrollState,
        shape = shape,
        containerColor = containerColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border,
        content = content,
    )
}

@Composable
fun SortDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    currentSortCriteria: SortCriteria,
    currentSortOrder: SortOrder,
    onSortRequest: (SortCriteria, SortOrder) -> Unit,
    modifier: Modifier = Modifier,
) {
    val checkIcon: @Composable () -> Unit = @Composable {
        Icon(
            modifier = modifier,
            painter = painterResource(R.drawable.ic_check),
            contentDescription = null,
        )
    }

    RoundedDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        for (criteria in SortCriteria.entries) {
            DropdownMenuItem(
                text = { Text(text = criteria.toString()) },
                onClick = { onSortRequest(criteria, currentSortOrder) },
                trailingIcon = { if (criteria == currentSortCriteria) checkIcon() },
            )
        }

        HorizontalDivider()

        for (order in SortOrder.entries) {
            DropdownMenuItem(
                text = { Text(text = order.toString()) },
                onClick = { onSortRequest(currentSortCriteria, order) },
                trailingIcon = { if (order == currentSortOrder) checkIcon() },
            )
        }
    }
}