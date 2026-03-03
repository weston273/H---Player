package com.hplayer.player.reco

import com.hplayer.player.db.AppDatabase
import com.hplayer.player.db.views.TrackFeatureRow

class RecommendationEngine(private val db: AppDatabase) {

    private var cachedAtMs: Long = 0
    private var cachedEventTimestamp: Long = -1
    private var cachedTop10: List<String> = emptyList()

    suspend fun computeTop10(nowMs: Long): List<String> {
        val latestEvent = db.playEventDao().getLatestEventTimestamp() ?: 0L
        if (latestEvent == cachedEventTimestamp && nowMs - cachedAtMs < CACHE_TTL_MS) {
            return cachedTop10
        }

        val candidates = db.trackDao().getTrackFeatureRows(limit = 1200)
        if (candidates.isEmpty()) {
            return emptyList()
        }

        val artistAffinity = normalize(db.trackDao().getArtistAffinityRows().associate { it.key to it.score })
        val genreAffinity = normalize(db.trackDao().getGenreAffinityRows().associate { it.key to it.score })
        val albumCompletion = db.trackDao().getAlbumCompletionRows().associate { it.albumId to it.completionRatio.coerceIn(0.0, 1.0) }

        val maxPlay = candidates.maxOf { it.playCount }.coerceAtLeast(1)

        val scored = candidates.map { row ->
            val frequency = row.playCount.toDouble() / maxPlay.toDouble()
            val recency = recencyScore(nowMs, row.lastPlayed)
            val genre = row.genreId?.let { genreAffinity[it] } ?: 0.0
            val artist = row.artistId?.let { artistAffinity[it] } ?: 0.0
            val completion = row.albumId?.let { albumCompletion[it] } ?: 0.0

            // Final score = 0.35F + 0.25R + 0.15G + 0.15A + 0.10C
            val score =
                (0.35 * frequency) +
                    (0.25 * recency) +
                    (0.15 * genre) +
                    (0.15 * artist) +
                    (0.10 * completion)

            row.trackId to score
        }

        cachedTop10 = scored
            .sortedByDescending { it.second }
            .take(10)
            .map { it.first }

        cachedAtMs = nowMs
        cachedEventTimestamp = latestEvent
        return cachedTop10
    }

    private fun recencyScore(nowMs: Long, lastPlayed: Long?): Double {
        if (lastPlayed == null) return 0.0
        val ageDays = ((nowMs - lastPlayed).coerceAtLeast(0L)).toDouble() / DAY_MS
        return 1.0 / (1.0 + ageDays)
    }

    private fun normalize(raw: Map<String, Double>): Map<String, Double> {
        val max = raw.values.maxOrNull()?.takeIf { it > 0 } ?: return emptyMap()
        return raw.mapValues { (_, value) -> value / max }
    }

    companion object {
        private const val DAY_MS = 86_400_000.0
        private const val CACHE_TTL_MS = 60_000L
    }
}
