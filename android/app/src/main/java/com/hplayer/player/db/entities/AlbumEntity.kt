package com.hplayer.player.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "albums",
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["artist_id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["artist_id"])]
)
data class AlbumEntity(
    @PrimaryKey
    @ColumnInfo(name = "album_id")
    val albumId: String,
    @ColumnInfo(name = "media_album_id")
    val mediaAlbumId: Long?,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "name_normalized")
    val nameNormalized: String,
    @ColumnInfo(name = "artist_id")
    val artistId: String?
)
