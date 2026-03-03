package com.hplayer.player.model

data class PlaybackStateSnapshot(
    val isPlaying: Boolean,
    val currentTrackId: String?,
    val queue: List<QueueItem>,
    val positionMs: Long,
    val bufferedMs: Long,
    val durationMs: Long,
    val shuffleEnabled: Boolean,
    val repeatMode: String
)

data class QueueItem(
    val trackId: String,
    val index: Int
)
