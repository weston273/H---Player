package com.hplayer.player.bridge

import android.content.Intent
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.hplayer.player.playback.PlaybackStatePublisher
import com.hplayer.player.service.PlayerMediaSessionService
import com.hplayer.player.service.PlayerServiceRegistry

class PlaybackModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private var listenerCount = 0
    private var unsubscribe: (() -> Unit)? = null

    override fun getName(): String = "PlaybackModule"

    @ReactMethod
    fun addListener(eventName: String) {
        listenerCount += 1
        if (eventName == EVENT_PLAYBACK_STATE && unsubscribe == null) {
            unsubscribe = PlaybackStatePublisher.subscribe { snapshot ->
                if (listenerCount > 0) {
                    reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                        .emit(EVENT_PLAYBACK_STATE, snapshot.toWritableMap())
                }
            }
        }
    }

    @ReactMethod
    fun removeListeners(count: Int) {
        listenerCount = (listenerCount - count).coerceAtLeast(0)
        if (listenerCount == 0) {
            unsubscribe?.invoke()
            unsubscribe = null
        }
    }

    @ReactMethod
    fun getPlaybackState(promise: Promise) {
        withEngine(promise) { engine ->
            promise.resolve(engine.getSnapshot().toWritableMap())
        }
    }

    @ReactMethod
    fun togglePlayPause(promise: Promise) {
        withEngine(promise) { engine ->
            engine.togglePlayPause()
            promise.resolve(null)
        }
    }

    @ReactMethod
    fun seekTo(positionMs: Double, promise: Promise) {
        withEngine(promise) { engine ->
            engine.seekTo(positionMs.toLong())
            promise.resolve(null)
        }
    }

    @ReactMethod
    fun skipNext(promise: Promise) {
        withEngine(promise) { engine ->
            engine.skipNext()
            promise.resolve(null)
        }
    }

    @ReactMethod
    fun skipPrevious(promise: Promise) {
        withEngine(promise) { engine ->
            engine.skipPrevious()
            promise.resolve(null)
        }
    }

    @ReactMethod
    fun playTrack(trackId: String, promise: Promise) {
        withEngine(promise) { engine ->
            engine.playTrack(trackId)
            promise.resolve(null)
        }
    }

    private fun withEngine(promise: Promise, block: (com.hplayer.player.playback.PlaybackEngine) -> Unit) {
        val engine = PlayerServiceRegistry.playbackEngine
        if (engine != null) {
            block(engine)
            return
        }

        val serviceIntent = Intent(reactContext, PlayerMediaSessionService::class.java)
        reactContext.startForegroundService(serviceIntent)

        val delayed = PlayerServiceRegistry.playbackEngine
        if (delayed == null) {
            promise.reject("SERVICE_NOT_READY", "Playback service is starting. Retry shortly.")
            return
        }

        block(delayed)
    }

    companion object {
        private const val EVENT_PLAYBACK_STATE = "playbackStateChanged"
    }
}
