package com.prajwalch.torrentsearch.data.repository

import com.prajwalch.torrentsearch.data.local.dao.TorznabConfigDao
import com.prajwalch.torrentsearch.data.local.entities.TorznabConfigEntity
import com.prajwalch.torrentsearch.domain.model.Category
import com.prajwalch.torrentsearch.domain.model.TorznabConfig
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.map

import java.util.UUID
import javax.inject.Inject

class TorznabConfigRepository @Inject constructor(
    private val dao: TorznabConfigDao,
) {
    suspend fun createConfig(
        searchProviderName: String,
        url: String,
        apiKey: String,
        category: Category,
    ) {
        val configId = UUID.randomUUID().toString()
        val configEntity = TorznabConfigEntity(
            id = configId,
            searchProviderName = searchProviderName,
            url = url.trimEnd { it == '/' },
            apiKey = apiKey,
            category = category.name,
        )
        dao.insertConfig(entity = configEntity)
    }

    fun getAllConfigs(): Flow<List<TorznabConfig>> {
        return dao.getAllConfigs().map { it.toDomain() }
    }

    suspend fun getAllConfigsId(): List<String> {
        return dao.getConfigsId()
    }

    suspend fun findConfigById(id: String): TorznabConfig? {
        return dao.findConfigById(id)?.toDomain()
    }

    fun getConfigsCount(): Flow<Int> {
        return dao.getConfigsCount()
    }

    suspend fun updateConfig(
        id: String,
        searchProviderName: String,
        url: String,
        apiKey: String,
        category: Category,
    ) {
        val configEntity = TorznabConfigEntity(
            id = id,
            searchProviderName = searchProviderName,
            url = url,
            apiKey = apiKey,
            category = category.name,
        )
        dao.updateConfig(entity = configEntity)
    }

    suspend fun deleteConfigById(id: String) {
        dao.deleteConfigById(id)
    }
}

private fun TorznabConfigEntity.toDomain() =
    TorznabConfig(
        id = this.id,
        searchProviderName = this.searchProviderName,
        url = this.url,
        apiKey = this.apiKey,
        category = Category.valueOf(this.category),
    )

private fun List<TorznabConfigEntity>.toDomain() = this.map { it.toDomain() }