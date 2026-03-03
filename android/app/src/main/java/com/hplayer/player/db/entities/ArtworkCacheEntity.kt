package com.hplayer.player.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artwork_cache",
    indices = [Index(value = ["track_id"], unique = true), Index(value = ["last_accessed_at"])]
)
data class ArtworkCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "artwork_key")
    val artworkKey: String,
    @ColumnInfo(name = "track_id")
    val trackId: String,
    @ColumnInfo(name = "disk_path")
    val diskPath: String,
    @ColumnInfo(name = "width")
    val width: Int,
    @ColumnInfo(name = "height")
    val height: Int,
    @ColumnInfo(name = "byte_size")
    val byteSize: Long,
    @ColumnInfo(name = "last_accessed_at")
    val lastAccessedAt: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
