package com.hplayer.player.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.room.withTransaction
import com.hplayer.player.db.DatabaseProvider
import com.hplayer.player.db.entities.AlbumEntity
import com.hplayer.player.db.entities.ArtistEntity
import com.hplayer.player.db.entities.GenreEntity
import com.hplayer.player.db.entities.ScanStateEntity
import com.hplayer.player.db.entities.TrackEntity
import com.hplayer.player.scan.MediaStoreAudioScanner
import com.hplayer.player.scan.ScanProgress
import com.hplayer.player.scan.ScanProgressListener
import com.hplayer.player.scan.ScannedTrack
import com.hplayer.player.util.TagNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryRepository private constructor(private val context: Context) {
    private val db = DatabaseProvider.get(context)
    private val scanner = MediaStoreAudioScanner(context)

    suspend fun runIncrementalScan(listener: ScanProgressListener?) = withContext(Dispatchers.IO) {
        listener?.onProgress(ScanProgress(stage = "querying", processed = 0, total = 0))

        val lastScan = db.scanStateDao().getByKey(SCAN_WATERMARK_KEY)?.stateValue?.toLongOrNull()
        val scannedRows = scanner.scanIncremental(lastScan)

        listener?.onProgress(ScanProgress(stage = "normalizing", processed = 0, total = scannedRows.size))

        val now = System.currentTimeMillis()
        val artists = mutableMapOf<String, ArtistEntity>()
        val albums = mutableMapOf<String, AlbumEntity>()
        val genres = mutableMapOf<String, GenreEntity>()

        val tracks = scannedRows.mapIndexed { index, row ->
            val enriched = enrichMissingMetadata(row)

            val artistId = buildStableId("artist", enriched.artistNameNormalized)
            val albumId = buildStableId("album", "${enriched.albumNameNormalized}|$artistId")
            val genreName = null
            val genreId = genreName?.let { buildStableId("genre", TagNormalizer.normalizeTitle(it)) }

            artists[artistId] = ArtistEntity(
                artistId = artistId,
                name = enriched.artistName,
                nameNormalized = enriched.artistNameNormalized
            )

            albums[albumId] = AlbumEntity(
                albumId = albumId,
                mediaAlbumId = enriched.albumMediaId,
                name = enriched.albumName,
                nameNormalized = enriched.albumNameNormalized,
                artistId = artistId
            )

            if (genreId != null && genreName != null) {
                genres[genreId] = GenreEntity(
                    genreId = genreId,
                    name = genreName,
                    nameNormalized = TagNormalizer.normalizeTitle(genreName)
                )
            }

            listener?.onProgress(
                ScanProgress(
                    stage = "normalizing",
                    processed = index + 1,
                    total = scannedRows.size
                )
            )

            TrackEntity(
                trackId = enriched.trackId,
                mediaId = enriched.mediaId,
                contentUri = enriched.contentUri,
                volumeName = enriched.volumeName,
                title = enriched.title,
                titleNormalized = enriched.titleNormalized,
                artistId = artistId,
                albumId = albumId,
                genreId = genreId,
                durationMs = enriched.durationMs,
                trackNumber = enriched.trackNumber,
                dateModifiedSec = enriched.dateModifiedSec,
                artworkKey = buildStableId("art", enriched.trackId),
                updatedAt = now
            )
        }

        listener?.onProgress(ScanProgress(stage = "persisting", processed = 0, total = tracks.size))

        db.withTransaction {
            db.artistDao().upsertAll(artists.values.toList())
            db.albumDao().upsertAll(albums.values.toList())
            db.genreDao().upsertAll(genres.values.toList())

            // Incremental pass only upserts changed/new records.
            // Deletions are handled by explicit maintenance scans, not delta windows.
            if (tracks.isNotEmpty()) {
                db.trackDao().upsertTracks(tracks)
            }

            val nextWatermark = scannedRows.maxOfOrNull { it.dateModifiedSec } ?: lastScan ?: 0L
            db.scanStateDao().upsert(
                ScanStateEntity(
                    stateKey = SCAN_WATERMARK_KEY,
                    stateValue = nextWatermark.toString(),
                    updatedAt = now
                )
            )
        }

        listener?.onProgress(ScanProgress(stage = "done", processed = tracks.size, total = tracks.size))
    }

    suspend fun getTracksPage(offset: Int, limit: Int) = withContext(Dispatchers.IO) {
        db.trackDao().getTracksPage(offset, limit)
    }

    private fun enrichMissingMetadata(track: ScannedTrack): ScannedTrack {
        if (track.title.isNotBlank() && track.artistName.isNotBlank() && track.albumName.isNotBlank()) {
            return track
        }

        return runCatching {
            val retriever = MediaMetadataRetriever()
            val uri = Uri.parse(track.contentUri)
            context.contentResolver.openFileDescriptor(uri, "r")?.use { fd ->
                retriever.setDataSource(fd.fileDescriptor)
            }

            val parsedTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val parsedArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val parsedAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)

            retriever.release()

            track.copy(
                title = parsedTitle?.ifBlank { track.title } ?: track.title,
                titleNormalized = TagNormalizer.normalizeTitle(parsedTitle ?: track.title),
                artistName = parsedArtist?.ifBlank { track.artistName } ?: track.artistName,
                artistNameNormalized = TagNormalizer.normalizeArtist(parsedArtist ?: track.artistName),
                albumName = parsedAlbum?.ifBlank { track.albumName } ?: track.albumName,
                albumNameNormalized = TagNormalizer.normalizeTitle(parsedAlbum ?: track.albumName)
            )
        }.getOrDefault(track)
    }

    private fun buildStableId(type: String, raw: String): String {
        return "$type:${raw.lowercase().trim().hashCode()}"
    }

    companion object {
        private const val SCAN_WATERMARK_KEY = "scan_watermark_sec"

        @Volatile
        private var instance: LibraryRepository? = null

        fun get(context: Context): LibraryRepository {
            return instance ?: synchronized(this) {
                instance ?: LibraryRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
