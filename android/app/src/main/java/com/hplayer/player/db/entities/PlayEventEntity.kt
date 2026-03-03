package com.hplayer.player.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_events",
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["track_id"],
            childColumns = ["track_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["track_id"]),
        Index(value = ["played_at"]),
        Index(value = ["is_qualified_play"])
    ]
)
data class PlayEventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "event_id")
    val eventId: Long = 0,
    @ColumnInfo(name = "track_id")
    val trackId: String,
    @ColumnInfo(name = "played_at")
    val playedAt: Long,
    @ColumnInfo(name = "duration_listened_ms")
    val durationListenedMs: Long,
    @ColumnInfo(name = "completion_percentage")
    val completionPercentage: Double,
    @ColumnInfo(name = "is_qualified_play")
    val isQualifiedPlay: Boolean
)
