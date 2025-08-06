package com.prajwalch.torrentsearch.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.prajwalch.torrentsearch.R
import com.prajwalch.torrentsearch.models.Category
import com.prajwalch.torrentsearch.providers.SearchProviderId
import com.prajwalch.torrentsearch.providers.SearchProviderSafetyStatus
import com.prajwalch.torrentsearch.ui.components.NavigateBackIconButton
import com.prajwalch.torrentsearch.ui.viewmodel.SearchProviderUiState
import com.prajwalch.torrentsearch.ui.viewmodel.SearchProvidersViewModel

@Composable
fun SearchProvidersScreen(
    onNavigateBack: () -> Unit,
    viewModel: SearchProvidersViewModel,
    modifier: Modifier = Modifier,
) {
    val searchProvidersUiState by viewModel.searchProvidersUiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            SearchProvidersScreenTopBar(
                onNavigateBack = onNavigateBack,
                onEnableAllSearchProviders = viewModel::enableAllSearchProviders,
                onDisableAllSearchProviders = viewModel::disableAllSearchProviders,
                onResetToDefault = viewModel::resetEnabledSearchProvidersToDefault,
            )
        }
    ) { innerPadding ->
        SearchProviderList(
            modifier = Modifier.consumeWindowInsets(innerPadding),
            contentPadding = innerPadding,
            searchProviders = searchProvidersUiState,
            onProviderCheckedChange = viewModel::enableSearchProvider,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchProvidersScreenTopBar(
    onNavigateBack: () -> Unit,
    onEnableAllSearchProviders: () -> Unit,
    onDisableAllSearchProviders: () -> Unit,
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        modifier = modifier,
        title = { Text(text = stringResource(R.string.setting_search_providers)) },
        navigationIcon = {
            NavigateBackIconButton(
                onClick = onNavigateBack,
                contentDescriptionId = R.string.button_go_to_settings_screen,
            )
        },
        actions = {
            IconButton(onClick = onEnableAllSearchProviders) {
                Icon(
                    painter = painterResource(R.drawable.ic_select_all),
                    contentDescription = stringResource(
                        R.string.button_enable_all_search_providers,
                    ),
                )
            }
            IconButton(onClick = onDisableAllSearchProviders) {
                Icon(
                    painter = painterResource(R.drawable.ic_deselect_all),
                    contentDescription = stringResource(
                        R.string.button_disable_all_search_providers,
                    ),
                )
            }
            IconButton(onClick = onResetToDefault) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = stringResource(
                        R.string.button_reset_search_providers,
                    ),
                )
            }
        }
    )
}

@Composable
private fun SearchProviderList(
    searchProviders: List<SearchProviderUiState>,
    onProviderCheckedChange: (SearchProviderId, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    var showUnsafeDetailsDialog by remember { mutableStateOf<SearchProviderUiState?>(null) }

    showUnsafeDetailsDialog?.let { searchProviderInfo ->
        SearchProviderUnsafeDetailsDialog(
            onDismissRequest = { showUnsafeDetailsDialog = null },
            providerName = searchProviderInfo.name,
            url = searchProviderInfo.url,
            // FIXME: This is poisonous code.
            unsafeReason = (searchProviderInfo.safetyStatus as SearchProviderSafetyStatus.Unsafe).reason,
        )
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
    ) {
        items(items = searchProviders, key = { it.id }) { searchProvider ->
            SearchProviderListItem(
                name = searchProvider.name,
                url = searchProvider.url,
                specializedCategory = searchProvider.specializedCategory,
                safetyStatus = searchProvider.safetyStatus,
                onShowUnsafeReason = { showUnsafeDetailsDialog = searchProvider },
                checked = searchProvider.enabled,
                onCheckedChange = { onProviderCheckedChange(searchProvider.id, it) },
            )
        }
    }
}

@Composable
private fun SearchProviderUnsafeDetailsDialog(
    onDismissRequest: () -> Unit,
    providerName: String,
    url: String,
    unsafeReason: String,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        icon = {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = null,
            )
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = providerName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = url,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        text = { Text(text = unsafeReason) },
    )
}

@Composable
private fun SearchProviderListItem(
    name: String,
    url: String,
    specializedCategory: Category,
    safetyStatus: SearchProviderSafetyStatus,
    onShowUnsafeReason: () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = Modifier
            .clickable(
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) },
            )
            .then(modifier),
        headlineContent = {
            SearchProviderName(
                name = name,
                safetyStatus = safetyStatus,
                onShowUnsafeReason = onShowUnsafeReason,
            )
        },
        supportingContent = {
            SearchProviderMetadata(
                url = url,
                specializedCategory = specializedCategory,
            )
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
    )
}

@Composable
private fun SearchProviderName(
    name: String,
    safetyStatus: SearchProviderSafetyStatus,
    onShowUnsafeReason: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BadgedBox(
        modifier = modifier,
        badge = {
            if (safetyStatus.isUnsafe()) {
                UnsafeBadge(onClick = onShowUnsafeReason)
            }
        },
    ) {
        Text(text = name)
    }
}

@Composable
private fun UnsafeBadge(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Badge(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
            .then(modifier),
    ) {
        Text(text = stringResource(R.string.badge_unsafe))
    }
}

@Composable
private fun SearchProviderMetadata(
    url: String,
    specializedCategory: Category,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.clickable { uriHandler.openUri(url) },
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = url.removePrefix("https://"))
            Icon(
                modifier = Modifier.size(12.dp),
                painter = painterResource(R.drawable.ic_open_in_new),
                contentDescription = null,
            )
        }
        Text(text = "\u2022")
        Text(text = specializedCategory.name.lowercase())
    }
}