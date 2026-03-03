package com.hplayer.player.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tracks",
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["artist_id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = AlbumEntity::class,
            parentColumns = ["album_id"],
            childColumns = ["album_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = GenreEntity::class,
            parentColumns = ["genre_id"],
            childColumns = ["genre_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["media_id"]),
        Index(value = ["artist_id"]),
        Index(value = ["album_id"]),
        Index(value = ["genre_id"]),
        Index(value = ["play_count"]),
        Index(value = ["last_played"]),
        Index(value = ["is_corrupted"])
    ]
)
data class TrackEntity(
    @PrimaryKey
    @ColumnInfo(name = "track_id")
    val trackId: String,
    @ColumnInfo(name = "media_id")
    val mediaId: Long,
    @ColumnInfo(name = "content_uri")
    val contentUri: String,
    @ColumnInfo(name = "volume_name")
    val volumeName: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "title_normalized")
    val titleNormalized: String,
    @ColumnInfo(name = "artist_id")
    val artistId: String?,
    @ColumnInfo(name = "album_id")
    val albumId: String?,
    @ColumnInfo(name = "genre_id")
    val genreId: String?,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(name = "track_number")
    val trackNumber: Int?,
    @ColumnInfo(name = "date_modified_sec")
    val dateModifiedSec: Long,
    @ColumnInfo(name = "artwork_key")
    val artworkKey: String?,
    @ColumnInfo(name = "play_count")
    val playCount: Int = 0,
    @ColumnInfo(name = "last_played")
    val lastPlayed: Long? = null,
    @ColumnInfo(name = "is_corrupted")
    val isCorrupted: Boolean = false,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
