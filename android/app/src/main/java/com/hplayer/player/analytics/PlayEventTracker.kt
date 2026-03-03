package com.hplayer.player.analytics

import com.hplayer.player.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PlayEventTracker(db: AppDatabase) {
    private val repository = AnalyticsRepository(db)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var activeTrackId: String? = null
    private var activeTrackDurationMs: Long = 0L
    private var startedAtMs: Long = 0L

    fun onTrackStarted(trackId: String?, durationMs: Long, nowMs: Long = System.currentTimeMillis()) {
        if (trackId == null) return

        if (activeTrackId != null && activeTrackId != trackId) {
            flush(nowMs)
        }

        activeTrackId = trackId
        activeTrackDurationMs = durationMs.coerceAtLeast(1L)
        startedAtMs = nowMs
    }

    fun onPausedOrStopped(nowMs: Long = System.currentTimeMillis()) {
        flush(nowMs)
    }

    private fun flush(nowMs: Long) {
        val trackId = activeTrackId ?: return
        val listened = (nowMs - startedAtMs).coerceAtLeast(0L)
        val duration = activeTrackDurationMs.coerceAtLeast(1L)

        scope.launch {
            repository.recordPlayEvent(
                trackId = trackId,
                playedAt = nowMs,
                durationListenedMs = listened,
                trackDurationMs = duration
            )
        }

        activeTrackId = null
        activeTrackDurationMs = 0L
        startedAtMs = 0L
    }
}
