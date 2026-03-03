package com.hplayer.player.playback

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class PlaybackStateStore(context: Context) {
    private val prefs = context.getSharedPreferences("playback_state", Context.MODE_PRIVATE)

    fun saveQueue(trackIds: List<String>, currentIndex: Int, positionMs: Long) {
        val payload = JSONObject().apply {
            put("queue", JSONArray(trackIds))
            put("currentIndex", currentIndex)
            put("positionMs", positionMs)
        }
        prefs.edit().putString(KEY_QUEUE_STATE, payload.toString()).apply()
    }

    fun readQueueState(): QueueState {
        val raw = prefs.getString(KEY_QUEUE_STATE, null) ?: return QueueState(emptyList(), 0, 0L)
        return runCatching {
            val obj = JSONObject(raw)
            val queueArray = obj.optJSONArray("queue") ?: JSONArray()
            val queue = buildList {
                for (i in 0 until queueArray.length()) {
                    add(queueArray.getString(i))
                }
            }
            QueueState(
                trackIds = queue,
                currentIndex = obj.optInt("currentIndex", 0),
                positionMs = obj.optLong("positionMs", 0L)
            )
        }.getOrDefault(QueueState(emptyList(), 0, 0L))
    }

    data class QueueState(
        val trackIds: List<String>,
        val currentIndex: Int,
        val positionMs: Long
    )

    companion object {
        private const val KEY_QUEUE_STATE = "queue_state"
    }
}
