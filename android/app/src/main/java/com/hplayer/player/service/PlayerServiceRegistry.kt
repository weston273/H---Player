package com.hplayer.player.service

import com.hplayer.player.playback.PlaybackEngine

object PlayerServiceRegistry {
    @Volatile
    var playbackEngine: PlaybackEngine? = null
}
