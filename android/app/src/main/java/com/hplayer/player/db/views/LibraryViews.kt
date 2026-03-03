package com.hplayer.player.db.views

data class TrackWithDisplay(
    val trackId: String,
    val mediaId: Long,
    val contentUri: String,
    val title: String,
    val artistName: String,
    val albumName: String,
    val durationMs: Long,
    val genreName: String?,
    val trackNumber: Int?,
    val artworkKey: String?
)

data class RankedTrack(
    val trackId: String,
    val score: Double,
    val playCount: Int,
    val lastPlayed: Long?
)

data class ArtistAggregate(
    val artistId: String,
    val name: String,
    val score: Double
)

data class AlbumAggregate(
    val albumId: String,
    val name: String,
    val artistName: String,
    val score: Double
)

data class TrackFeatureRow(
    val trackId: String,
    val artistId: String?,
    val albumId: String?,
    val genreId: String?,
    val playCount: Int,
    val lastPlayed: Long?,
    val durationMs: Long
)

data class AffinityRow(
    val key: String,
    val score: Double
)

data class AlbumCompletionRow(
    val albumId: String,
    val completionRatio: Double
)
