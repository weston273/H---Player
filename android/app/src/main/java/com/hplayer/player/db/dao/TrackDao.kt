package com.hplayer.player.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hplayer.player.db.entities.TrackEntity
import com.hplayer.player.db.views.AlbumAggregate
import com.hplayer.player.db.views.AlbumCompletionRow
import com.hplayer.player.db.views.AffinityRow
import com.hplayer.player.db.views.ArtistAggregate
import com.hplayer.player.db.views.RankedTrack
import com.hplayer.player.db.views.TrackFeatureRow
import com.hplayer.player.db.views.TrackWithDisplay

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<TrackEntity>)

    @Query(
        """
        SELECT t.track_id AS trackId,
               t.media_id AS mediaId,
               t.content_uri AS contentUri,
               t.title AS title,
               COALESCE(ar.name, 'Unknown Artist') AS artistName,
               COALESCE(al.name, 'Unknown Album') AS albumName,
               t.duration_ms AS durationMs,
               g.name AS genreName,
               t.track_number AS trackNumber,
               t.artwork_key AS artworkKey
        FROM tracks t
        LEFT JOIN artists ar ON t.artist_id = ar.artist_id
        LEFT JOIN albums al ON t.album_id = al.album_id
        LEFT JOIN genres g ON t.genre_id = g.genre_id
        WHERE t.is_corrupted = 0
        ORDER BY t.title COLLATE NOCASE
        LIMIT :limit OFFSET :offset
        """
    )
    suspend fun getTracksPage(offset: Int, limit: Int): List<TrackWithDisplay>

    @Query(
        """
        SELECT t.track_id AS trackId,
               t.media_id AS mediaId,
               t.content_uri AS contentUri,
               t.title AS title,
               COALESCE(ar.name, 'Unknown Artist') AS artistName,
               COALESCE(al.name, 'Unknown Album') AS albumName,
               t.duration_ms AS durationMs,
               g.name AS genreName,
               t.track_number AS trackNumber,
               t.artwork_key AS artworkKey
        FROM tracks t
        LEFT JOIN artists ar ON t.artist_id = ar.artist_id
        LEFT JOIN albums al ON t.album_id = al.album_id
        LEFT JOIN genres g ON t.genre_id = g.genre_id
        WHERE t.track_id = :trackId
        LIMIT 1
        """
    )
    suspend fun getTrackById(trackId: String): TrackWithDisplay?

    @Query(
        """
        SELECT t.track_id AS trackId,
               t.media_id AS mediaId,
               t.content_uri AS contentUri,
               t.title AS title,
               COALESCE(ar.name, 'Unknown Artist') AS artistName,
               COALESCE(al.name, 'Unknown Album') AS albumName,
               t.duration_ms AS durationMs,
               g.name AS genreName,
               t.track_number AS trackNumber,
               t.artwork_key AS artworkKey
        FROM tracks t
        LEFT JOIN artists ar ON t.artist_id = ar.artist_id
        LEFT JOIN albums al ON t.album_id = al.album_id
        LEFT JOIN genres g ON t.genre_id = g.genre_id
        WHERE t.track_id IN (:trackIds)
        """
    )
    suspend fun getTracksByIds(trackIds: List<String>): List<TrackWithDisplay>

    @Query(
        """
        SELECT t.track_id AS trackId,
               COUNT(pe.event_id) * 1.0 AS score,
               COUNT(pe.event_id) AS playCount,
               MAX(pe.played_at) AS lastPlayed
        FROM play_events pe
        JOIN tracks t ON t.track_id = pe.track_id
        WHERE pe.is_qualified_play = 1
          AND pe.played_at >= :windowStart
        GROUP BY t.track_id
        ORDER BY score DESC, lastPlayed DESC
        LIMIT :limit
        """
    )
    suspend fun getMostPlayedSince(windowStart: Long, limit: Int): List<RankedTrack>

    @Query(
        """
        SELECT t.track_id AS trackId,
               COUNT(pe.event_id) * 1.0 AS score,
               COUNT(pe.event_id) AS playCount,
               MAX(pe.played_at) AS lastPlayed
        FROM play_events pe
        JOIN tracks t ON t.track_id = pe.track_id
        WHERE pe.is_qualified_play = 1
        GROUP BY t.track_id
        ORDER BY score DESC, lastPlayed DESC
        LIMIT :limit
        """
    )
    suspend fun getMostPlayedAllTime(limit: Int): List<RankedTrack>

    @Query(
        """
        UPDATE tracks
        SET play_count = play_count + :delta,
            last_played = :playedAt,
            updated_at = :updatedAt
        WHERE track_id = :trackId
        """
    )
    suspend fun incrementPlayCounters(trackId: String, delta: Int, playedAt: Long, updatedAt: Long)

    @Query("DELETE FROM tracks WHERE track_id IN (:trackIds)")
    suspend fun deleteTracksByIds(trackIds: List<String>)

    @Query("SELECT track_id FROM tracks")
    suspend fun getAllTrackIds(): List<String>

    @Query("SELECT track_id FROM tracks WHERE media_id = :mediaId LIMIT 1")
    suspend fun findTrackIdByMediaId(mediaId: Long): String?

    @Query(
        """
        SELECT ar.artist_id AS artistId,
               ar.name AS name,
               COUNT(pe.event_id) * 1.0 AS score
        FROM play_events pe
        JOIN tracks t ON pe.track_id = t.track_id
        JOIN artists ar ON t.artist_id = ar.artist_id
        WHERE pe.is_qualified_play = 1
        GROUP BY ar.artist_id
        ORDER BY score DESC
        LIMIT :limit
        """
    )
    suspend fun getFavoriteArtists(limit: Int): List<ArtistAggregate>

    @Query(
        """
        SELECT al.album_id AS albumId,
               al.name AS name,
               COALESCE(ar.name, 'Unknown Artist') AS artistName,
               (COUNT(pe.event_id) * 1.0 / MAX(album_track_count.track_count, 1)) AS score
        FROM play_events pe
        JOIN tracks t ON pe.track_id = t.track_id
        JOIN albums al ON t.album_id = al.album_id
        LEFT JOIN artists ar ON al.artist_id = ar.artist_id
        JOIN (
            SELECT album_id, COUNT(track_id) AS track_count
            FROM tracks
            GROUP BY album_id
        ) album_track_count ON album_track_count.album_id = al.album_id
        WHERE pe.is_qualified_play = 1
        GROUP BY al.album_id
        ORDER BY score DESC
        LIMIT :limit
        """
    )
    suspend fun getFavoriteAlbums(limit: Int): List<AlbumAggregate>

    @Query(
        """
        SELECT t.track_id AS trackId,
               (t.play_count * :playWeight) +
               (CASE WHEN t.last_played IS NULL THEN 0 ELSE (:recentFloor / MAX(1.0, (:nowMs - t.last_played) / 86400000.0)) END * :recencyWeight) AS score,
               t.play_count AS playCount,
               t.last_played AS lastPlayed
        FROM tracks t
        WHERE t.is_corrupted = 0
        ORDER BY score DESC
        LIMIT :limit
        """
    )
    suspend fun getRecommendationCandidates(
        limit: Int,
        nowMs: Long,
        playWeight: Double,
        recencyWeight: Double,
        recentFloor: Double
    ): List<RankedTrack>

    @Query(
        """
        SELECT track_id AS trackId,
               artist_id AS artistId,
               album_id AS albumId,
               genre_id AS genreId,
               play_count AS playCount,
               last_played AS lastPlayed,
               duration_ms AS durationMs
        FROM tracks
        WHERE is_corrupted = 0
        ORDER BY play_count DESC, last_played DESC
        LIMIT :limit
        """
    )
    suspend fun getTrackFeatureRows(limit: Int): List<TrackFeatureRow>

    @Query(
        """
        SELECT COALESCE(t.artist_id, '') AS `key`,
               COUNT(pe.event_id) * 1.0 AS score
        FROM play_events pe
        JOIN tracks t ON pe.track_id = t.track_id
        WHERE pe.is_qualified_play = 1
          AND t.artist_id IS NOT NULL
        GROUP BY t.artist_id
        ORDER BY score DESC
        """
    )
    suspend fun getArtistAffinityRows(): List<AffinityRow>

    @Query(
        """
        SELECT COALESCE(t.genre_id, '') AS `key`,
               COUNT(pe.event_id) * 1.0 AS score
        FROM play_events pe
        JOIN tracks t ON pe.track_id = t.track_id
        WHERE pe.is_qualified_play = 1
          AND t.genre_id IS NOT NULL
        GROUP BY t.genre_id
        ORDER BY score DESC
        """
    )
    suspend fun getGenreAffinityRows(): List<AffinityRow>

    @Query(
        """
        WITH album_totals AS (
            SELECT album_id, COUNT(track_id) AS total_tracks
            FROM tracks
            WHERE album_id IS NOT NULL
            GROUP BY album_id
        ),
        album_played AS (
            SELECT t.album_id AS album_id, COUNT(DISTINCT t.track_id) AS played_tracks
            FROM play_events pe
            JOIN tracks t ON pe.track_id = t.track_id
            WHERE pe.is_qualified_play = 1
              AND t.album_id IS NOT NULL
            GROUP BY t.album_id
        )
        SELECT at.album_id AS albumId,
               CAST(COALESCE(ap.played_tracks, 0) AS REAL) / MAX(1, at.total_tracks) AS completionRatio
        FROM album_totals at
        LEFT JOIN album_played ap ON at.album_id = ap.album_id
        """
    )
    suspend fun getAlbumCompletionRows(): List<AlbumCompletionRow>

    @Transaction
    suspend fun replaceTrackBatch(
        tracks: List<TrackEntity>,
        removedTrackIds: List<String>
    ) {
        if (tracks.isNotEmpty()) {
            upsertTracks(tracks)
        }
        if (removedTrackIds.isNotEmpty()) {
            deleteTracksByIds(removedTrackIds)
        }
    }
}
