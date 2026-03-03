package com.hplayer.player.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScannedTrackFactoryTest {

    @Test
    fun fromMediaStore_buildsStableTrackId_andNormalizesMetadata() {
        val track = ScannedTrackFactory.fromMediaStore(
            mediaId = 42L,
            volume = "external_primary",
            contentUri = "content://media/external_primary/audio/media/42",
            title = "My Song (Remastered 2011) feat. Someone",
            artist = "Artist ft. Guest",
            album = "Best Hits (Deluxe)",
            albumMediaId = 7L,
            durationMs = 245000L,
            dateModifiedSec = 1700000000L,
            trackNumber = 3
        )

        assertEquals("external_primary:42", track.trackId)
        assertEquals("My Song", track.titleNormalized)
        assertEquals("Artist", track.artistNameNormalized)
        assertTrue(track.albumNameNormalized.contains("Best", ignoreCase = true))
    }
}
