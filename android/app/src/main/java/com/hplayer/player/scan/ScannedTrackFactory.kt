package com.hplayer.player.scan

import com.hplayer.player.util.TagNormalizer

object ScannedTrackFactory {
    fun fromMediaStore(
        mediaId: Long,
        volume: String,
        contentUri: String,
        title: String,
        artist: String,
        album: String,
        albumMediaId: Long?,
        durationMs: Long,
        dateModifiedSec: Long,
        trackNumber: Int?
    ): ScannedTrack {
        return ScannedTrack(
            trackId = "$volume:$mediaId",
            mediaId = mediaId,
            volumeName = volume,
            contentUri = contentUri,
            title = title,
            titleNormalized = TagNormalizer.normalizeTitle(title),
            artistName = artist,
            artistNameNormalized = TagNormalizer.normalizeArtist(artist),
            albumName = album,
            albumNameNormalized = TagNormalizer.normalizeTitle(album),
            albumMediaId = albumMediaId,
            durationMs = durationMs,
            trackNumber = trackNumber,
            dateModifiedSec = dateModifiedSec
        )
    }
}
