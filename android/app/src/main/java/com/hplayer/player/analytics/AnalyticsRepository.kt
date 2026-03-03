package com.hplayer.player.analytics

import com.hplayer.player.db.AppDatabase
import com.hplayer.player.db.entities.PlayEventEntity
import com.hplayer.player.db.views.TrackWithDisplay
import com.hplayer.player.reco.RecommendationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalyticsRepository(private val db: AppDatabase) {
    private val recommendationEngine = RecommendationEngine(db)

    suspend fun recordPlayEvent(
        trackId: String,
        playedAt: Long,
        durationListenedMs: Long,
        trackDurationMs: Long
    ) = withContext(Dispatchers.IO) {
        val safeDuration = trackDurationMs.coerceAtLeast(1L)
        val completion = (durationListenedMs.toDouble() / safeDuration.toDouble()).coerceIn(0.0, 1.0)
        val isQualified = durationListenedMs >= QUALIFIED_PLAY_MS || completion >= QUALIFIED_COMPLETION

        db.playEventDao().insert(
            PlayEventEntity(
                trackId = trackId,
                playedAt = playedAt,
                durationListenedMs = durationListenedMs,
                completionPercentage = completion,
                isQualifiedPlay = isQualified
            )
        )

        if (isQualified) {
            db.trackDao().incrementPlayCounters(
                trackId = trackId,
                delta = 1,
                playedAt = playedAt,
                updatedAt = playedAt
            )
        }
    }

    suspend fun getHomeInsights(nowMs: Long = System.currentTimeMillis()): HomeInsights = withContext(Dispatchers.IO) {
        val weeklyStart = nowMs - DAYS_7_MS
        val monthlyStart = nowMs - DAYS_30_MS

        val recentIds = db.playEventDao().getRecentTrackIds(limit = 50)
            .distinct()
            .take(20)

        val weekly = db.trackDao().getMostPlayedSince(weeklyStart, 20)
        val monthly = db.trackDao().getMostPlayedSince(monthlyStart, 20)
        val allTime = db.trackDao().getMostPlayedAllTime(20)
        val top10 = recommendationEngine.computeTop10(nowMs)

        HomeInsights(
            recentlyPlayed = mapTrackIds(recentIds),
            mostPlayedWeekly = mapTrackIds(weekly.map { it.trackId }),
            mostPlayedMonthly = mapTrackIds(monthly.map { it.trackId }),
            mostPlayedAllTime = mapTrackIds(allTime.map { it.trackId }),
            recommendedTop10 = mapTrackIds(top10),
            favoriteArtists = db.trackDao().getFavoriteArtists(limit = 10).map {
                FavoriteArtist(it.artistId, it.name, it.score)
            },
            favoriteAlbums = db.trackDao().getFavoriteAlbums(limit = 10).map {
                FavoriteAlbum(it.albumId, it.name, it.artistName, it.score)
            }
        )
    }

    private suspend fun mapTrackIds(ids: List<String>): List<TrackWithDisplay> {
        if (ids.isEmpty()) return emptyList()
        val rows = db.trackDao().getTracksByIds(ids)
        return ids.mapNotNull { id -> rows.find { it.trackId == id } }
    }

    companion object {
        private const val QUALIFIED_PLAY_MS = 30_000L
        private const val QUALIFIED_COMPLETION = 0.5
        private const val DAYS_7_MS = 7L * 24L * 60L * 60L * 1000L
        private const val DAYS_30_MS = 30L * 24L * 60L * 60L * 1000L
    }
}
