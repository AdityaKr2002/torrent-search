package com.prajwalch.torrentsearch.ui.searchhistory.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

import com.prajwalch.torrentsearch.R
import com.prajwalch.torrentsearch.domain.model.SearchHistory
import com.prajwalch.torrentsearch.domain.model.SearchHistoryId

@Composable
fun SearchHistoryList(
    histories: List<SearchHistory>,
    onSearchRequest: (String) -> Unit,
    onCopyQueryToClipboard: (String) -> Unit,
    onDeleteSearchHistory: (SearchHistoryId) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(modifier = modifier, contentPadding = contentPadding) {
        items(items = histories, key = { it.id }) {
            SearchHistoryListItem(
                modifier = Modifier
                    .animateItem()
                    .combinedClickable(
                        interactionSource = null,
                        indication = LocalIndication.current,
                        onClick = { onSearchRequest(it.query) },
                        onLongClick = { onCopyQueryToClipboard(it.query) },
                    ),
                query = it.query,
                onDeleteClick = { onDeleteSearchHistory(it.id) },
            )
        }
    }
}

@Composable
private fun SearchHistoryListItem(
    query: String,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        leadingContent = {
            Icon(
                painter = painterResource(R.drawable.ic_history),
                contentDescription = null,
            )
        },
        headlineContent = { Text(text = query) },
        trailingContent = {
            // Delete button.
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = null,
                )
            }
        },
    )
}