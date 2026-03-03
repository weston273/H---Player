package com.hplayer.player.lyrics

data class LyricsCandidate(
    val lyrics: String,
    val source: String,
    val title: String,
    val artist: String,
    val album: String?
)

interface LyricsSource {
    suspend fun search(query: LyricsQuery): List<LyricsCandidate>
}
