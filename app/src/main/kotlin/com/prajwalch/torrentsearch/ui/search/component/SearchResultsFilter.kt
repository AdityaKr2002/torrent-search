package com.prajwalch.torrentsearch.ui.search.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource

import com.prajwalch.torrentsearch.R
import com.prajwalch.torrentsearch.domain.model.Category
import com.prajwalch.torrentsearch.ui.categoryStringResource
import com.prajwalch.torrentsearch.ui.component.RoundedDropdownMenu
import com.prajwalch.torrentsearch.ui.search.FilterOptions
import com.prajwalch.torrentsearch.ui.search.SearchProviderFilterOption
import com.prajwalch.torrentsearch.ui.theme.spaces

import kotlinx.collections.immutable.ImmutableList

@Composable
fun SearchResultsFilter(
    filterOptions: FilterOptions,
    onToggleDeadTorrents: () -> Unit,
    onToggleSearchProvider: (providerName: String) -> Unit,
    onSelectAllSearchProviders: () -> Unit,
    onDeselectAllSearchProviders: () -> Unit,
    onInvertSearchProvidersSelection: () -> Unit,
    onUpdateCategory: (Category) -> Unit,
    modifier: Modifier = Modifier,
    enableDeadTorrentsFilter: Boolean = true,
    enableSearchProvidersFilter: Boolean = true,
    enableCategoryFilter: Boolean = true,
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
            onSelectAll = onSelectAllSearchProviders,
            onDeselectAll = onDeselectAllSearchProviders,
            onInvertSelection = onInvertSearchProvidersSelection,
        )
    }

    val arrowDownIcon: @Composable () -> Unit = @Composable {
        Icon(
            modifier = Modifier.size(FilterChipDefaults.IconSize),
            painter = painterResource(R.drawable.ic_keyboard_arrow_down),
            contentDescription = null,
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
                trailingIcon = arrowDownIcon,
                enabled = enableSearchProvidersFilter,
            )
        }

        item(key = "category", contentType = filterOptions.category) {
            val isDefaultCategorySelected = filterOptions.category == Category.All
            var showCategoryOptions by rememberSaveable(filterOptions.category) {
                mutableStateOf(false)
            }

            Box {
                FilterChip(
                    selected = !isDefaultCategorySelected,
                    onClick = { showCategoryOptions = true },
                    label = { Text(text = categoryStringResource(filterOptions.category)) },
                    trailingIcon = arrowDownIcon,
                    enabled = enableCategoryFilter,
                )
                CategoryOptionsDropdownMenu(
                    expanded = showCategoryOptions,
                    onDismiss = { showCategoryOptions = false },
                    selectedCategory = filterOptions.category,
                    onCategorySelect = onUpdateCategory,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchProvidersFilterBottomSheet(
    onDismiss: () -> Unit,
    filterOptions: ImmutableList<SearchProviderFilterOption>,
    onToggleSearchProvider: (providerName: String) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onInvertSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(modifier = modifier, onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spaces.large),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(R.string.search_providers_filter_bottom_sheet_title),
                    style = MaterialTheme.typography.titleMedium,
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    SearchProvidersFilterActionButton(
                        onClick = onSelectAll,
                        painter = painterResource(R.drawable.ic_select_all),
                        contentDescription = stringResource(
                            R.string.search_providers_filter_action_select_all,
                        ),
                    )
                    SearchProvidersFilterActionButton(
                        onClick = onDeselectAll,
                        painter = painterResource(R.drawable.ic_deselect_all),
                        contentDescription = stringResource(
                            R.string.search_providers_filter_action_deselect_all,
                        ),
                    )
                    SearchProvidersFilterActionButton(
                        onClick = onInvertSelection,
                        painter = painterResource(R.drawable.ic_flip),
                        contentDescription = stringResource(
                            R.string.search_providers_filter_action_invert_selection,
                        ),
                    )
                }
            }

            SearchProvidersChipRow(
                modifier = Modifier.padding(vertical = MaterialTheme.spaces.large),
                filterOptions = filterOptions,
                onToggleSearchProvider = onToggleSearchProvider,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchProvidersFilterActionButton(
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val tooltipPositionProvider =
        TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above)

    TooltipBox(
        modifier = modifier,
        positionProvider = tooltipPositionProvider,
        tooltip = { PlainTooltip { Text(text = contentDescription) } },
        state = rememberTooltipState(),
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painter,
                contentDescription = contentDescription,
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

@Composable
private fun CategoryOptionsDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    selectedCategory: Category,
    onCategorySelect: (Category) -> Unit,
    modifier: Modifier = Modifier,
) {
    RoundedDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        Category.entries.forEach {
            DropdownMenuItem(
                text = { Text(text = categoryStringResource(it)) },
                onClick = { onCategorySelect(it) },
                trailingIcon = {
                    if (it == selectedCategory) {
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