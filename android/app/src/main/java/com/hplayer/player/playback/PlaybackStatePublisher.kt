package com.hplayer.player.playback

import com.hplayer.player.model.PlaybackStateSnapshot
import java.util.concurrent.CopyOnWriteArraySet

object PlaybackStatePublisher {
    private val listeners = CopyOnWriteArraySet<(PlaybackStateSnapshot) -> Unit>()

    fun publish(snapshot: PlaybackStateSnapshot) {
        listeners.forEach { listener ->
            listener(snapshot)
        }
    }

    fun subscribe(listener: (PlaybackStateSnapshot) -> Unit): () -> Unit {
        listeners += listener
        return {
            listeners -= listener
        }
    }
}
