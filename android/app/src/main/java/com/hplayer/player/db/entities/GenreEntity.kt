package com.hplayer.player.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "genres")
data class GenreEntity(
    @PrimaryKey
    @ColumnInfo(name = "genre_id")
    val genreId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "name_normalized")
    val nameNormalized: String
)
