package com.prajwalch.torrentsearch.data.repository

import com.prajwalch.torrentsearch.data.local.dao.TorznabConfigDao
import com.prajwalch.torrentsearch.data.local.entities.TorznabConfigEntity
import com.prajwalch.torrentsearch.data.local.entities.toDomain
import com.prajwalch.torrentsearch.data.local.entities.toSearchProviderInfo
import com.prajwalch.torrentsearch.domain.models.Category
import com.prajwalch.torrentsearch.domain.models.TorznabConfig
import com.prajwalch.torrentsearch.domain.models.TorznabConnectionCheckResult
import com.prajwalch.torrentsearch.providers.AniRena
import com.prajwalch.torrentsearch.providers.AnimeTosho
import com.prajwalch.torrentsearch.providers.BitSearch
import com.prajwalch.torrentsearch.providers.Dmhy
import com.prajwalch.torrentsearch.providers.Eztv
import com.prajwalch.torrentsearch.providers.FileMood
import com.prajwalch.torrentsearch.providers.InternetArchive
import com.prajwalch.torrentsearch.providers.Knaben
import com.prajwalch.torrentsearch.providers.LimeTorrents
import com.prajwalch.torrentsearch.providers.MyPornClub
import com.prajwalch.torrentsearch.providers.Nyaa
import com.prajwalch.torrentsearch.providers.SearchProvider
import com.prajwalch.torrentsearch.providers.SearchProviderId
import com.prajwalch.torrentsearch.providers.SearchProviderInfo
import com.prajwalch.torrentsearch.providers.SubsPlease
import com.prajwalch.torrentsearch.providers.Sukebei
import com.prajwalch.torrentsearch.providers.ThePirateBay
import com.prajwalch.torrentsearch.providers.TheRarBg
import com.prajwalch.torrentsearch.providers.TokyoToshokan
import com.prajwalch.torrentsearch.providers.TorrentDatabase
import com.prajwalch.torrentsearch.providers.TorrentDownload
import com.prajwalch.torrentsearch.providers.TorrentDownloads
import com.prajwalch.torrentsearch.providers.TorrentsCSV
import com.prajwalch.torrentsearch.providers.TorznabSearchProvider
import com.prajwalch.torrentsearch.providers.UIndex
import com.prajwalch.torrentsearch.providers.XXXClub
import com.prajwalch.torrentsearch.providers.XXXTracker
import com.prajwalch.torrentsearch.providers.Yts

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchProvidersRepository @Inject constructor(
    private val torznabConfigDao: TorznabConfigDao,
) {
    private val builtins = listOf(
        AniRena(),
        AnimeTosho(),
        BitSearch(),
        Dmhy(),
        Eztv(),
        FileMood(),
        InternetArchive(),
        Knaben(),
        LimeTorrents(),
        MyPornClub(),
        Nyaa(),
        SubsPlease(),
        Sukebei(),
        ThePirateBay(),
        TheRarBg(),
        TokyoToshokan(),
        TorrentDatabase(),
        TorrentDownload(),
        TorrentDownloads(),
        TorrentsCSV(),
        UIndex(),
        XXXClub(),
        XXXTracker(),
        Yts(),
    )

    suspend fun getSearchProvidersByCategory(category: Category): List<SearchProvider> {
        val searchProviders = getSearchProviders().firstOrNull() ?: return emptyList()

        return if (category == Category.All) {
            searchProviders
        } else {
            searchProviders.filterByCategory(category)
        }
    }

    fun getSearchProviders(): Flow<List<SearchProvider>> {
        return torznabConfigDao.getAllConfigs()
            .map { configEntities ->
                configEntities.map {
                    TorznabSearchProvider(id = it.searchProviderId, config = it.toDomain())
                }
            }
            .map { torznabProvidersInstance -> builtins + torznabProvidersInstance }
    }

    private fun List<SearchProvider>.filterByCategory(category: Category) = this.filter {
        val specializedCategory = it.info.specializedCategory
        (specializedCategory == Category.All) || (category == specializedCategory)
    }

    fun getDefaultSearchProvidersId(): Set<SearchProviderId> {
        return builtins.filter { it.info.enabledByDefault }.map { it.info.id }.toSet()
    }

    fun getSearchProvidersInfo(): Flow<List<SearchProviderInfo>> {
        val builtinProvidersInfo = builtins.map { it.info }

        return torznabConfigDao.getAllConfigs()
            .map { configEntities -> configEntities.toSearchProviderInfo() }
            .map { torznabProvidersInfo -> builtinProvidersInfo + torznabProvidersInfo }
            // TODO: Remove this. Sorting is not the concern of repository.
            .map { searchProviderInfos -> searchProviderInfos.sortedBy { it.name } }
    }

    fun getSearchProvidersCount(): Flow<Int> {
        return torznabConfigDao.getConfigsCount()
            .map { torznabProvidersCount -> builtins.size + torznabProvidersCount }
    }

    suspend fun checkTorznabConnection(url: String, apiKey: String): TorznabConnectionCheckResult {
        return TorznabSearchProvider.checkConnection(url = url.trimEnd('/'), apiKey = apiKey)
    }

    suspend fun createTorznabConfig(
        searchProviderName: String,
        url: String,
        apiKey: String,
        category: Category,
    ) {
        val searchProviderId = UUID.randomUUID().toString()
        val configEntity = TorznabConfigEntity(
            searchProviderId = searchProviderId,
            searchProviderName = searchProviderName,
            url = url.trimEnd { it == '/' },
            apiKey = apiKey,
            category = category.name,
        )
        torznabConfigDao.insertConfig(entity = configEntity)
    }

    suspend fun findTorznabConfig(id: SearchProviderId): TorznabConfig? {
        return torznabConfigDao.findConfigById(id = id)?.toDomain()
    }

    suspend fun updateTorznabConfig(
        id: SearchProviderId,
        searchProviderName: String,
        url: String,
        apiKey: String,
        category: Category,
    ) {
        val configEntity = TorznabConfigEntity(
            searchProviderId = id,
            searchProviderName = searchProviderName,
            url = url,
            apiKey = apiKey,
            category = category.name,
        )
        torznabConfigDao.updateConfig(entity = configEntity)
    }

    suspend fun deleteTorznabConfig(id: SearchProviderId) {
        torznabConfigDao.deleteConfigById(id = id)
    }
}