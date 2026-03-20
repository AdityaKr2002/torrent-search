package com.prajwalch.torrentsearch.ui.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    onInsertQuery: (String) -> Unit,
    onDeleteSearchHistory: (SearchHistoryId) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(modifier = modifier, contentPadding = contentPadding) {
        items(items = histories, key = { it.id }) {
            SearchHistoryListItem(
                modifier = Modifier
                    .animateItem()
                    .clickable { onSearchRequest(it.query) },
                query = it.query,
                onInsertClick = { onInsertQuery(it.query) },
                onDeleteClick = { onDeleteSearchHistory(it.id) },
            )
        }
    }
}

@Composable
private fun SearchHistoryListItem(
    query: String,
    onInsertClick: () -> Unit,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Insert button.
                IconButton(onClick = onInsertClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_insert),
                        contentDescription = null,
                    )
                }
                // Delete button.
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = null,
                    )
                }
            }
        },
    )
}