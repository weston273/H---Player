package com.hplayer.player.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class ArtistEntity(
    @PrimaryKey
    @ColumnInfo(name = "artist_id")
    val artistId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "name_normalized")
    val nameNormalized: String
)
