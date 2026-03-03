package com.hplayer.player.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lyrics_cache",
    indices = [Index(value = ["track_id"], unique = true), Index(value = ["status"])]
)
data class LyricsCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "cache_id")
    val cacheId: String,
    @ColumnInfo(name = "track_id")
    val trackId: String,
    @ColumnInfo(name = "query_hash")
    val queryHash: String,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "lyrics_text")
    val lyricsText: String?,
    @ColumnInfo(name = "confidence")
    val confidence: Double?,
    @ColumnInfo(name = "source")
    val source: String?,
    @ColumnInfo(name = "failure_reason")
    val failureReason: String?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
