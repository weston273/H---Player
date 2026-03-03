package com.hplayer.player.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hplayer.player.db.entities.ScanStateEntity

@Dao
interface ScanStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: ScanStateEntity)

    @Query("SELECT * FROM scan_state WHERE state_key = :key LIMIT 1")
    suspend fun getByKey(key: String): ScanStateEntity?
}
