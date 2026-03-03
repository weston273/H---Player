package com.hplayer.player.service

import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.hplayer.player.playback.PlaybackEngine

class PlayerMediaSessionService : MediaSessionService() {
    private lateinit var engine: PlaybackEngine

    override fun onCreate() {
        super.onCreate()
        engine = PlaybackEngine(applicationContext)
        PlayerServiceRegistry.playbackEngine = engine
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        return engine.mediaSession
    }

    override fun onDestroy() {
        PlayerServiceRegistry.playbackEngine = null
        engine.release()
        super.onDestroy()
    }
}
