package com.hplayer.player.bridge

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.hplayer.player.db.views.TrackWithDisplay
import com.hplayer.player.model.PlaybackStateSnapshot

fun PlaybackStateSnapshot.toWritableMap(): WritableMap {
    val map = Arguments.createMap()
    map.putBoolean("isPlaying", isPlaying)
    map.putString("currentTrackId", currentTrackId)

    val queueArray = Arguments.createArray()
    queue.forEach {
        val item = Arguments.createMap()
        item.putString("trackId", it.trackId)
        item.putInt("index", it.index)
        queueArray.pushMap(item)
    }

    map.putArray("queue", queueArray)
    map.putDouble("positionMs", positionMs.toDouble())
    map.putDouble("bufferedMs", bufferedMs.toDouble())
    map.putDouble("durationMs", durationMs.toDouble())
    map.putBoolean("shuffleEnabled", shuffleEnabled)
    map.putString("repeatMode", repeatMode)
    return map
}

fun TrackWithDisplay.toWritableMap(): WritableMap {
    val map = Arguments.createMap()
    map.putString("id", trackId)
    map.putDouble("mediaStoreId", mediaId.toDouble())
    map.putString("contentUri", contentUri)
    map.putString("title", title)
    map.putString("artistName", artistName)
    map.putString("albumName", albumName)
    map.putDouble("durationMs", durationMs.toDouble())
    map.putString("genreName", genreName)
    map.putInt("trackNumber", trackNumber ?: 0)
    map.putString("artworkKey", artworkKey)
    return map
}

fun List<TrackWithDisplay>.toWritableArray(): WritableArray {
    val array = Arguments.createArray()
    forEach { array.pushMap(it.toWritableMap()) }
    return array
}
