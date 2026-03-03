package com.hplayer.player.analytics

import com.hplayer.player.db.views.TrackWithDisplay

data class HomeInsights(
    val recentlyPlayed: List<TrackWithDisplay>,
    val mostPlayedWeekly: List<TrackWithDisplay>,
    val mostPlayedMonthly: List<TrackWithDisplay>,
    val mostPlayedAllTime: List<TrackWithDisplay>,
    val recommendedTop10: List<TrackWithDisplay>,
    val favoriteArtists: List<FavoriteArtist>,
    val favoriteAlbums: List<FavoriteAlbum>
)

data class FavoriteArtist(
    val artistId: String,
    val name: String,
    val score: Double
)

data class FavoriteAlbum(
    val albumId: String,
    val name: String,
    val artistName: String,
    val score: Double
)
