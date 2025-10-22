package com.prajwalch.torrentsearch.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

import com.prajwalch.torrentsearch.models.Category
import com.prajwalch.torrentsearch.models.InfoHashOrMagnetUri
import com.prajwalch.torrentsearch.models.Torrent

@Entity(
    tableName = "bookmarks",
    indices = [Index("name", unique = true)],
)
data class BookmarkedTorrent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val size: String,
    val seeders: Int,
    val peers: Int,
    val providerId: String,
    val providerName: String,
    val uploadDate: String,
    val category: String,
    val descriptionPageUrl: String,
    val magnetUri: String,
)

fun BookmarkedTorrent.toDomain() =
    Torrent(
        id = this.id,
        name = this.name,
        size = this.size,
        seeders = this.seeders.toUInt(),
        peers = this.peers.toUInt(),
        providerId = this.providerId,
        providerName = this.providerName,
        uploadDate = this.uploadDate,
        category = if (this.category.isNotEmpty()) {
            Category.valueOf(this.category)
        } else {
            null
        },
        descriptionPageUrl = this.descriptionPageUrl,
        infoHashOrMagnetUri = InfoHashOrMagnetUri.MagnetUri(uri = this.magnetUri),
        bookmarked = true,
    )

fun Torrent.toEntity() =
    BookmarkedTorrent(
        id = this.id,
        name = this.name,
        size = this.size,
        seeders = this.seeders.toInt(),
        peers = this.peers.toInt(),
        providerId = this.providerId,
        providerName = this.providerName,
        uploadDate = this.uploadDate,
        category = this.category?.name ?: "",
        descriptionPageUrl = this.descriptionPageUrl,
        magnetUri = this.magnetUri(),
    )

fun List<BookmarkedTorrent>.toDomain() = this.map { it.toDomain() }