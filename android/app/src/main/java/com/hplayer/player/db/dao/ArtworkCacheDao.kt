package com.hplayer.player.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hplayer.player.db.entities.ArtworkCacheEntity

@Dao
interface ArtworkCacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(row: ArtworkCacheEntity)

    @Query("SELECT * FROM artwork_cache WHERE track_id = :trackId LIMIT 1")
    suspend fun getByTrackId(trackId: String): ArtworkCacheEntity?

    @Query("DELETE FROM artwork_cache WHERE artwork_key IN (SELECT artwork_key FROM artwork_cache ORDER BY last_accessed_at ASC LIMIT :count)")
    suspend fun evictLeastRecentlyUsed(count: Int)
}
