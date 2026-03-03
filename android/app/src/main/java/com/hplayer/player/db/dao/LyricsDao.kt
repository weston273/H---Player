package com.hplayer.player.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hplayer.player.db.entities.LyricsCacheEntity

@Dao
interface LyricsDao {

    @Query("SELECT * FROM lyrics_cache WHERE track_id = :trackId LIMIT 1")
    suspend fun getByTrackId(trackId: String): LyricsCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: LyricsCacheEntity)
}
