package com.prajwalch.torrentsearch.ui.search.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

import com.prajwalch.torrentsearch.R
import com.prajwalch.torrentsearch.ui.search.FilterOptions
import com.prajwalch.torrentsearch.ui.search.SearchProviderFilterOption
import com.prajwalch.torrentsearch.ui.theme.spaces

import kotlinx.collections.immutable.ImmutableList

@Composable
fun SearchResultsFilter(
    filterOptions: FilterOptions,
    onToggleDeadTorrents: () -> Unit,
    onToggleSearchProvider: (providerName: String) -> Unit,
    modifier: Modifier = Modifier,
    enableDeadTorrentsFilter: Boolean = true,
    enableSearchProvidersFilter: Boolean = true,
) {
    val numSelectedSearchProviders = rememberSaveable(filterOptions.searchProviders) {
        filterOptions.searchProviders.count { it.selected }
    }

    var showSearchProvidersFilter by rememberSaveable { mutableStateOf(false) }
    if (showSearchProvidersFilter) {
        SearchProvidersFilterBottomSheet(
            onDismiss = { showSearchProvidersFilter = false },
            filterOptions = filterOptions.searchProviders,
            onToggleSearchProvider = onToggleSearchProvider,
        )
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spaces.small,
            alignment = Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = MaterialTheme.spaces.large),
    ) {
        item(key = "dead_torrents") {
            FilterChip(
                selected = filterOptions.deadTorrents,
                onClick = onToggleDeadTorrents,
                label = { Text(text = stringResource(R.string.search_filters_dead_torrents)) },
                enabled = enableDeadTorrentsFilter,
            )
        }

        item(key = "search_providers") {
            val selected = numSelectedSearchProviders > 0
            val label = stringResource(R.string.search_filters_search_providers).let {
                if (selected) "$it ($numSelectedSearchProviders)" else it
            }

            FilterChip(
                modifier = Modifier.animateItem(),
                selected = selected,
                onClick = { showSearchProvidersFilter = true },
                label = { Text(text = label) },
                trailingIcon = {
                    Icon(
                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                        painter = painterResource(R.drawable.ic_keyboard_arrow_down),
                        contentDescription = null,
                    )
                },
                enabled = enableSearchProvidersFilter,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchProvidersFilterBottomSheet(
    onDismiss: () -> Unit,
    filterOptions: ImmutableList<SearchProviderFilterOption>,
    onToggleSearchProvider: (providerName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(modifier = modifier, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.large),
        ) {
            Text(
                text = stringResource(R.string.search_providers_filter_bottom_sheet_title),
                style = MaterialTheme.typography.titleMedium,
            )
            SearchProvidersChipRow(
                modifier = Modifier.padding(vertical = MaterialTheme.spaces.large),
                filterOptions = filterOptions,
                onToggleSearchProvider = onToggleSearchProvider,
            )
        }
    }
}

@Composable
private fun SearchProvidersChipRow(
    filterOptions: ImmutableList<SearchProviderFilterOption>,
    onToggleSearchProvider: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spaces.small),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        filterOptions.forEach {
            FilterChip(
                selected = it.selected,
                onClick = { onToggleSearchProvider(it.searchProviderName) },
                label = { Text(text = it.searchProviderName) },
            )
        }
    }
}