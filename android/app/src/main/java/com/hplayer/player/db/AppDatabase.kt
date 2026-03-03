package com.hplayer.player.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hplayer.player.db.dao.AlbumDao
import com.hplayer.player.db.dao.ArtistDao
import com.hplayer.player.db.dao.ArtworkCacheDao
import com.hplayer.player.db.dao.GenreDao
import com.hplayer.player.db.dao.LyricsDao
import com.hplayer.player.db.dao.PlayEventDao
import com.hplayer.player.db.dao.PlaylistDao
import com.hplayer.player.db.dao.ScanStateDao
import com.hplayer.player.db.dao.TrackDao
import com.hplayer.player.db.entities.AlbumEntity
import com.hplayer.player.db.entities.ArtistEntity
import com.hplayer.player.db.entities.ArtworkCacheEntity
import com.hplayer.player.db.entities.GenreEntity
import com.hplayer.player.db.entities.LyricsCacheEntity
import com.hplayer.player.db.entities.PlayEventEntity
import com.hplayer.player.db.entities.PlaylistEntity
import com.hplayer.player.db.entities.PlaylistTrackEntity
import com.hplayer.player.db.entities.ScanStateEntity
import com.hplayer.player.db.entities.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        ArtistEntity::class,
        AlbumEntity::class,
        GenreEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
        PlayEventEntity::class,
        LyricsCacheEntity::class,
        ArtworkCacheEntity::class,
        ScanStateEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun artistDao(): ArtistDao
    abstract fun albumDao(): AlbumDao
    abstract fun genreDao(): GenreDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playEventDao(): PlayEventDao
    abstract fun lyricsDao(): LyricsDao
    abstract fun artworkCacheDao(): ArtworkCacheDao
    abstract fun scanStateDao(): ScanStateDao
}
