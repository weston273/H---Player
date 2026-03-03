package com.hplayer.player.playback

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PlaybackStateStoreTest {

    @Test
    fun saveAndReadQueueState_roundTripsValues() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = PlaybackStateStore(context)

        store.saveQueue(
            trackIds = listOf("track_a", "track_b", "track_c"),
            currentIndex = 1,
            positionMs = 12_345L
        )

        val restored = PlaybackStateStore(context).readQueueState()

        assertEquals(listOf("track_a", "track_b", "track_c"), restored.trackIds)
        assertEquals(1, restored.currentIndex)
        assertEquals(12_345L, restored.positionMs)
    }
}
