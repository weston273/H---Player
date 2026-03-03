package com.hplayer.player.util

object TagNormalizer {
    private val featRegex = Regex("\\b(feat\\.?|ft\\.?|featuring)\\b.*$", RegexOption.IGNORE_CASE)
    private val remasterRegex = Regex("\\b(remaster(ed)?|deluxe|version|mono|stereo mix)\\b", RegexOption.IGNORE_CASE)
    private val parentheticalRegex = Regex("[\\(\\[].*?[\\)\\]]")
    private val multiWhitespace = Regex("\\s+")

    fun normalizeTitle(raw: String): String {
        return raw
            .replace(parentheticalRegex, " ")
            .replace(featRegex, " ")
            .replace(remasterRegex, " ")
            .replace(Regex("[^A-Za-z0-9\\s]"), " ")
            .replace(multiWhitespace, " ")
            .trim()
            .ifBlank { raw.trim() }
    }

    fun normalizeArtist(raw: String): String {
        return raw
            .replace(featRegex, " ")
            .replace(Regex("[^A-Za-z0-9\\s]"), " ")
            .replace(multiWhitespace, " ")
            .trim()
            .ifBlank { raw.trim() }
    }
}
