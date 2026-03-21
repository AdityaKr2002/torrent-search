package com.prajwalch.torrentsearch.ui.settings.searchproviders.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.prajwalch.torrentsearch.domain.model.Category
import com.prajwalch.torrentsearch.providers.SearchProviderId
import com.prajwalch.torrentsearch.ui.component.CategoryChipsRow
import com.prajwalch.torrentsearch.ui.settings.searchproviders.SearchProviderListItem
import com.prajwalch.torrentsearch.ui.theme.spaces

@Composable
fun SearchProviderList(
    searchProviders: List<SearchProviderListItem>,
    onEnableSearchProvider: (SearchProviderId, Boolean) -> Unit,
    onEditConfig: (SearchProviderId) -> Unit,
    onDeleteConfig: (SearchProviderId) -> Unit,
    selectedCategory: Category?,
    onCategoryClick: (Category) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        item {
            CategoryChipsRow(
                categories = Category.entries,
                selectedCategory = selectedCategory,
                onCategoryClick = onCategoryClick,
                contentPadding = PaddingValues(horizontal = MaterialTheme.spaces.large),
            )
        }
        items(items = searchProviders, key = { it.id }) {
            SearchProviderListItem(
                modifier = Modifier.animateItem(),
                name = it.name,
                url = it.url,
                category = it.specializedCategory,
                type = it.type,
                safetyStatus = it.safetyStatus,
                enabled = it.enabled,
                onEnable = { enable -> onEnableSearchProvider(it.id, enable) },
                onEditConfig = { onEditConfig(it.id) },
                onDeleteConfig = { onDeleteConfig(it.id) },
            )
        }
    }
}