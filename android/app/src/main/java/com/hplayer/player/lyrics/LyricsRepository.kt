package com.hplayer.player.lyrics

import com.hplayer.player.db.dao.LyricsDao
import com.hplayer.player.db.entities.LyricsCacheEntity
import com.hplayer.player.util.StringSimilarity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.security.MessageDigest

class LyricsRepository(
    private val lyricsDao: LyricsDao,
    private val lyricsSources: List<LyricsSource>
) {
    suspend fun getLyrics(trackId: String, title: String, artist: String, album: String?): String? = withContext(Dispatchers.IO) {
        val query = LyricsQueryBuilder.build(title, artist, album)
        val queryHash = hash(query.query)

        val cached = lyricsDao.getByTrackId(trackId)
        if (cached != null && cached.queryHash == queryHash) {
            if (cached.status == STATUS_SUCCESS) return@withContext cached.lyricsText
            if (cached.status == STATUS_FAILED) return@withContext null
        }

        val result = fetchWithRetry(query)
        if (result == null) {
            lyricsDao.upsert(
                LyricsCacheEntity(
                    cacheId = "lyrics:$trackId",
                    trackId = trackId,
                    queryHash = queryHash,
                    status = STATUS_FAILED,
                    lyricsText = null,
                    confidence = null,
                    source = null,
                    failureReason = "no-high-confidence-match",
                    updatedAt = System.currentTimeMillis()
                )
            )
            return@withContext null
        }

        lyricsDao.upsert(
            LyricsCacheEntity(
                cacheId = "lyrics:$trackId",
                trackId = trackId,
                queryHash = queryHash,
                status = STATUS_SUCCESS,
                lyricsText = result.candidate.lyrics,
                confidence = result.score,
                source = result.candidate.source,
                failureReason = null,
                updatedAt = System.currentTimeMillis()
            )
        )

        result.candidate.lyrics
    }

    private suspend fun fetchWithRetry(query: LyricsQuery): ScoredLyricsResult? {
        repeat(MAX_RETRIES + 1) { attempt ->
            val result = runCatching {
                withTimeout(TIMEOUT_MS) {
                    val candidates = lyricsSources.flatMap { it.search(query) }
                    scoreCandidates(query, candidates)
                }
            }.getOrNull()

            if (result != null && result.score >= CONFIDENCE_THRESHOLD) {
                return result
            }

            if (attempt < MAX_RETRIES) {
                delay(RETRY_DELAYS_MS[attempt])
            }
        }

        return null
    }

    private fun scoreCandidates(query: LyricsQuery, candidates: List<LyricsCandidate>): ScoredLyricsResult? {
        if (candidates.isEmpty()) return null

        return candidates
            .map { candidate ->
                val titleScore = StringSimilarity.normalizedLevenshtein(query.normalizedTitle, candidate.title)
                val artistScore = StringSimilarity.normalizedLevenshtein(query.normalizedArtist, candidate.artist)
                val albumScore = if (query.normalizedAlbum == null || candidate.album.isNullOrBlank()) {
                    0.5
                } else {
                    StringSimilarity.normalizedLevenshtein(query.normalizedAlbum, candidate.album)
                }

                val confidence = (titleScore * 0.5) + (artistScore * 0.4) + (albumScore * 0.1)
                ScoredLyricsResult(candidate, confidence)
            }
            .maxByOrNull { it.score }
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    data class ScoredLyricsResult(
        val candidate: LyricsCandidate,
        val score: Double
    )

    companion object {
        private const val STATUS_SUCCESS = "success"
        private const val STATUS_FAILED = "failed"
        private const val CONFIDENCE_THRESHOLD = 0.84
        private const val TIMEOUT_MS = 5_000L
        private const val MAX_RETRIES = 2
        private val RETRY_DELAYS_MS = listOf(350L, 800L)
    }
}
