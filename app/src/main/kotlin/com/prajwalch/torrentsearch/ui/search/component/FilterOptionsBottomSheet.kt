package com.prajwalch.torrentsearch.ui.search.component

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

import com.prajwalch.torrentsearch.R
import com.prajwalch.torrentsearch.ui.search.FilterOptions
import com.prajwalch.torrentsearch.ui.search.SearchProviderFilterOption
import com.prajwalch.torrentsearch.ui.theme.spaces

import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterOptionsBottomSheet(
    onDismiss: () -> Unit,
    filterOptions: FilterOptions,
    onToggleSearchProvider: (String) -> Unit,
    onToggleDeadTorrents: () -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
    ) {
        Column(modifier = Modifier.padding(bottom = MaterialTheme.spaces.large)) {
            AnimatedVisibility(visible = filterOptions.searchProviders.isNotEmpty()) {
                FiltersSectionTitle(titleId = R.string.search_filters_section_search_providers)
                SearchProvidersChipsRow(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spaces.large),
                    filterOptions = filterOptions.searchProviders,
                    onToggleSearchProvider = onToggleSearchProvider,
                    isSearching = isSearching,
                )
            }
            FiltersSectionTitle(titleId = R.string.search_filters_section_additional_options)
            FlowRow(
                modifier = Modifier.padding(horizontal = MaterialTheme.spaces.large),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChip(
                    selected = filterOptions.deadTorrents,
                    onClick = onToggleDeadTorrents,
                    label = { Text(text = stringResource(R.string.search_filters_dead_torrents)) },
                )
            }
        }
    }
}

@Composable
private fun FiltersSectionTitle(@StringRes titleId: Int, modifier: Modifier = Modifier) {
    Text(
        modifier = Modifier
            .padding(MaterialTheme.spaces.large)
            .then(modifier),
        text = stringResource(titleId),
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.titleSmall,
    )
}

@Composable
private fun SearchProvidersChipsRow(
    filterOptions: ImmutableList<SearchProviderFilterOption>,
    onToggleSearchProvider: (String) -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(
            space = MaterialTheme.spaces.small,
        ),
    ) {
        for (filterOption in filterOptions) {
            FilterChip(
                selected = filterOption.selected,
                onClick = { onToggleSearchProvider(filterOption.searchProviderName) },
                label = { Text(text = filterOption.searchProviderName) },
                enabled = !isSearching,
            )
        }
    }
}