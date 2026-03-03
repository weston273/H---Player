package com.hplayer.player.bridge

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class AudioFocusModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val audioManager: AudioManager = reactContext.getSystemService(AudioManager::class.java)

    private val focusRequest: AudioFocusRequest by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            .setOnAudioFocusChangeListener { /* ExoPlayer handles runtime behavior. */ }
            .build()
    }

    override fun getName(): String = "AudioFocusModule"

    @ReactMethod
    fun requestFocus(promise: Promise) {
        val granted = audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        promise.resolve(granted)
    }

    @ReactMethod
    fun abandonFocus(promise: Promise) {
        val released = audioManager.abandonAudioFocusRequest(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        promise.resolve(released)
    }
}
