package com.hplayer.player.playback

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackStateStoreInstrumentedTest {

    @Test
    fun queueStatePersistsAcrossStoreInstances() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = PlaybackStateStore(context)

        store.saveQueue(
            trackIds = listOf("track_x", "track_y"),
            currentIndex = 0,
            positionMs = 999L
        )

        val restored = PlaybackStateStore(context).readQueueState()
        assertEquals(listOf("track_x", "track_y"), restored.trackIds)
        assertEquals(0, restored.currentIndex)
        assertEquals(999L, restored.positionMs)
    }
}
