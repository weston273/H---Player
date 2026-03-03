package com.hplayer.player.model

data class AudioTrackModel(
    val id: String,
    val mediaId: Long,
    val contentUri: String,
    val title: String,
    val artistName: String,
    val albumName: String,
    val albumId: Long?,
    val genreName: String?,
    val durationMs: Long,
    val dateModifiedSec: Long,
    val artworkKey: String?
)
