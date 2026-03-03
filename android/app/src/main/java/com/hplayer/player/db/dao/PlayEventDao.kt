package com.hplayer.player.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hplayer.player.db.entities.PlayEventEntity

@Dao
interface PlayEventDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(event: PlayEventEntity)

    @Query(
        """
        SELECT track_id
        FROM play_events
        WHERE is_qualified_play = 1
        ORDER BY played_at DESC
        LIMIT :limit
        """
    )
    suspend fun getRecentTrackIds(limit: Int): List<String>

    @Query("SELECT MAX(played_at) FROM play_events")
    suspend fun getLatestEventTimestamp(): Long?
}
