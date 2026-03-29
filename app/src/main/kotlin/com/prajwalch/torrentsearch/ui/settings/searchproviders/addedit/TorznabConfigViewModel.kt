package com.prajwalch.torrentsearch.ui.settings.searchproviders.addedit

import android.util.Patterns

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.prajwalch.torrentsearch.domain.SearchProvidersManager
import com.prajwalch.torrentsearch.domain.model.Category
import com.prajwalch.torrentsearch.domain.model.TorznabConnectionCheckResult
import com.prajwalch.torrentsearch.providers.SearchProviderId
import com.prajwalch.torrentsearch.providers.TorznabSearchProvider

import dagger.hilt.android.lifecycle.HiltViewModel

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import javax.inject.Inject

data class TorznabConfigUiState(
    val searchProviderName: String = "",
    val url: String = "",
    val apiKey: String = "",
    val category: Category = Category.All,
    val isNewConfig: Boolean = true,
    val isUrlValid: Boolean = true,
    val isConnectionCheckRunning: Boolean = false,
) {
    fun isConfigNotBlank() =
        searchProviderName.isNotBlank() && url.isNotBlank() && apiKey.isNotBlank()
}

sealed interface TorznabConfigEvent {
    data object ConfigSaved : TorznabConfigEvent

    data class ConnectionCheckCompleted(
        val result: TorznabConnectionCheckResult,
    ) : TorznabConfigEvent
}

@HiltViewModel
class TorznabConfigViewModel @Inject constructor(
    private val searchProvidersManager: SearchProvidersManager,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    /**
     * ID of the search provider whose config to edit (edit mode).
     *
     * if `null`, a new config is created (add mode).
     */
    private val searchProviderId = savedStateHandle.get<String>("id")

    private val _uiState = MutableStateFlow(
        TorznabConfigUiState(isNewConfig = searchProviderId == null),
    )
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<TorznabConfigEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        searchProviderId?.let(::loadConfig)
    }

    private fun loadConfig(id: SearchProviderId) = viewModelScope.launch {
        val config = searchProvidersManager.findTorznabConfigById(id) ?: return@launch

        _uiState.value = TorznabConfigUiState(
            searchProviderName = config.searchProviderName,
            url = config.url,
            apiKey = config.apiKey,
            category = config.category,
            isNewConfig = false,
        )
    }

    fun setSearchProviderName(name: String) {
        _uiState.update { it.copy(searchProviderName = name) }
    }

    fun setUrl(url: String) {
        _uiState.update { it.copy(url = url) }
    }

    fun setAPIKey(apiKey: String) {
        _uiState.update { it.copy(apiKey = apiKey) }
    }

    fun setCategory(category: Category) {
        _uiState.update { it.copy(category = category) }
    }

    fun checkConnection() {
        _uiState.update { it.copy(isConnectionCheckRunning = false) }

        if (!isUrlValid()) {
            _uiState.update { it.copy(isUrlValid = false) }
            return
        }

        _uiState.update {
            it.copy(
                isUrlValid = true,
                isConnectionCheckRunning = true,
            )
        }

        viewModelScope.launch {
            val connectionCheckResult = TorznabSearchProvider
                .checkConnection(url = _uiState.value.url, apiKey = _uiState.value.apiKey)

            _uiState.update { it.copy(isConnectionCheckRunning = false) }
            _events.send(TorznabConfigEvent.ConnectionCheckCompleted(connectionCheckResult))
        }
    }

    fun saveConfig() {
        if (!isUrlValid()) {
            _uiState.update { it.copy(isUrlValid = false) }
            return
        }
        _uiState.update { it.copy(isUrlValid = true) }

        viewModelScope.launch {
            if (searchProviderId == null) {
                createConfig()
            } else {
                updateConfig(searchProviderId)
            }
            _events.send(TorznabConfigEvent.ConfigSaved)
        }
    }

    private fun isUrlValid(): Boolean = Patterns.WEB_URL.matcher(_uiState.value.url).matches()

    private suspend fun createConfig() {
        searchProvidersManager.createTorznabConfig(
            searchProviderName = _uiState.value.searchProviderName,
            url = _uiState.value.url,
            apiKey = _uiState.value.apiKey,
            category = _uiState.value.category,
        )
    }

    private suspend fun updateConfig(id: SearchProviderId) {
        searchProvidersManager.updateTorznabConfig(
            id = id,
            searchProviderName = _uiState.value.searchProviderName,
            url = _uiState.value.url,
            apiKey = _uiState.value.apiKey,
            category = _uiState.value.category,
        )
    }
}