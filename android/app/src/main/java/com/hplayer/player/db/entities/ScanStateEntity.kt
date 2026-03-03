package com.hplayer.player.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_state")
data class ScanStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "state_key")
    val stateKey: String,
    @ColumnInfo(name = "state_value")
    val stateValue: String,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
