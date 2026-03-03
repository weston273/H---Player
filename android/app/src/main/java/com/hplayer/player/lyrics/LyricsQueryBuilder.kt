package com.hplayer.player.lyrics

import com.hplayer.player.util.TagNormalizer

data class LyricsQuery(
    val normalizedTitle: String,
    val normalizedArtist: String,
    val normalizedAlbum: String?,
    val query: String
)

object LyricsQueryBuilder {
    fun build(title: String, artist: String, album: String?): LyricsQuery {
        val normalizedTitle = TagNormalizer.normalizeTitle(title)
        val normalizedArtist = TagNormalizer.normalizeArtist(artist)
        val normalizedAlbum = album?.let { TagNormalizer.normalizeTitle(it) }?.takeIf { it.isNotBlank() }

        val query = listOfNotNull(normalizedTitle, normalizedArtist, normalizedAlbum)
            .joinToString(" ")
            .trim()

        return LyricsQuery(
            normalizedTitle = normalizedTitle,
            normalizedArtist = normalizedArtist,
            normalizedAlbum = normalizedAlbum,
            query = query
        )
    }
}
