package com.prajwalch.torrentsearch.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.prajwalch.torrentsearch.data.local.entities.BookmarkedTorrent

import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkedTorrentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmarkedTorrent: BookmarkedTorrent)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmarks(bookmarkedTorrents: List<BookmarkedTorrent>)

    @Query("SELECT * FROM bookmarks ORDER by id DESC")
    fun getAllBookmarks(): Flow<List<BookmarkedTorrent>>

    @Delete
    suspend fun deleteBookmark(bookmarkedTorrent: BookmarkedTorrent)

    @Query("DELETE from bookmarks")
    suspend fun deleteAllBookmarks()
}