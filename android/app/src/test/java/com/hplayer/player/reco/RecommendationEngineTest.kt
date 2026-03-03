package com.hplayer.player.reco

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hplayer.player.db.AppDatabase
import com.hplayer.player.db.entities.AlbumEntity
import com.hplayer.player.db.entities.ArtistEntity
import com.hplayer.player.db.entities.PlayEventEntity
import com.hplayer.player.db.entities.TrackEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecommendationEngineTest {
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun computeTop10_prioritizesFrequencyRecencyAndAffinity() = runBlocking {
        val now = 1_700_000_000_000L

        db.artistDao().upsertAll(
            listOf(
                ArtistEntity("artist:a", "Artist A", "artist a"),
                ArtistEntity("artist:b", "Artist B", "artist b")
            )
        )

        db.albumDao().upsertAll(
            listOf(
                AlbumEntity("album:a", 10L, "Album A", "album a", "artist:a"),
                AlbumEntity("album:b", 11L, "Album B", "album b", "artist:b")
            )
        )

        db.trackDao().upsertTracks(
            listOf(
                TrackEntity(
                    trackId = "track:1",
                    mediaId = 1L,
                    contentUri = "content://media/1",
                    volumeName = "external_primary",
                    title = "Top Song",
                    titleNormalized = "top song",
                    artistId = "artist:a",
                    albumId = "album:a",
                    genreId = null,
                    durationMs = 200_000L,
                    trackNumber = 1,
                    dateModifiedSec = 1_700_000_000L,
                    artworkKey = "art:1",
                    playCount = 40,
                    lastPlayed = now - 60_000L,
                    updatedAt = now
                ),
                TrackEntity(
                    trackId = "track:2",
                    mediaId = 2L,
                    contentUri = "content://media/2",
                    volumeName = "external_primary",
                    title = "Old Song",
                    titleNormalized = "old song",
                    artistId = "artist:b",
                    albumId = "album:b",
                    genreId = null,
                    durationMs = 210_000L,
                    trackNumber = 1,
                    dateModifiedSec = 1_700_000_000L,
                    artworkKey = "art:2",
                    playCount = 8,
                    lastPlayed = now - (20L * 24L * 60L * 60L * 1000L),
                    updatedAt = now
                )
            )
        )

        repeat(20) {
            db.playEventDao().insert(
                PlayEventEntity(
                    trackId = "track:1",
                    playedAt = now - (it * 50_000L),
                    durationListenedMs = 120_000L,
                    completionPercentage = 0.7,
                    isQualifiedPlay = true
                )
            )
        }

        repeat(2) {
            db.playEventDao().insert(
                PlayEventEntity(
                    trackId = "track:2",
                    playedAt = now - (it * 86_400_000L),
                    durationListenedMs = 120_000L,
                    completionPercentage = 0.7,
                    isQualifiedPlay = true
                )
            )
        }

        val top = RecommendationEngine(db).computeTop10(now)

        assertTrue(top.isNotEmpty())
        assertEquals("track:1", top.first())
        assertTrue(top.contains("track:2"))
    }
}
