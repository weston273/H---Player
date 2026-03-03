package com.hplayer.player.bridge

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.hplayer.player.analytics.AnalyticsRepository
import com.hplayer.player.db.DatabaseProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AnalyticsModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository = AnalyticsRepository(DatabaseProvider.get(reactContext))

    override fun getName(): String = "AnalyticsModule"

    @ReactMethod
    fun getHomeInsights(promise: Promise) {
        scope.launch {
            runCatching {
                repository.getHomeInsights()
            }.fold(
                onSuccess = { insights ->
                    val map = Arguments.createMap()
                    map.putArray("recentlyPlayed", insights.recentlyPlayed.toWritableArray())
                    map.putArray("mostPlayedWeekly", insights.mostPlayedWeekly.toWritableArray())
                    map.putArray("mostPlayedMonthly", insights.mostPlayedMonthly.toWritableArray())
                    map.putArray("mostPlayedAllTime", insights.mostPlayedAllTime.toWritableArray())
                    map.putArray("recommendedTop10", insights.recommendedTop10.toWritableArray())

                    val artists = Arguments.createArray()
                    insights.favoriteArtists.forEach {
                        val row = Arguments.createMap()
                        row.putString("artistId", it.artistId)
                        row.putString("name", it.name)
                        row.putDouble("score", it.score)
                        artists.pushMap(row)
                    }
                    map.putArray("favoriteArtists", artists)

                    val albums = Arguments.createArray()
                    insights.favoriteAlbums.forEach {
                        val row = Arguments.createMap()
                        row.putString("albumId", it.albumId)
                        row.putString("name", it.name)
                        row.putString("artistName", it.artistName)
                        row.putDouble("score", it.score)
                        albums.pushMap(row)
                    }
                    map.putArray("favoriteAlbums", albums)

                    promise.resolve(map)
                },
                onFailure = { promise.reject("INSIGHTS_FAILED", it) }
            )
        }
    }
}
