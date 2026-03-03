package com.hplayer.player.scan

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

private data class ScanRow(
    val mediaId: Long,
    val volume: String,
    val contentUri: String,
    val title: String,
    val artist: String,
    val album: String,
    val albumMediaId: Long?,
    val durationMs: Long,
    val dateModifiedSec: Long,
    val trackNumber: Int?
)

class MediaStoreAudioScanner(private val context: Context) {

    fun scanIncremental(lastWatermarkSec: Long?): List<ScannedTrack> {
        val resolver = context.contentResolver
        val volumeNames = MediaStore.getExternalVolumeNames(context).ifEmpty { setOf(MediaStore.VOLUME_EXTERNAL) }
        val collected = mutableListOf<ScannedTrack>()

        for (volume in volumeNames) {
            val collection = MediaStore.Audio.Media.getContentUri(volume)
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_MODIFIED,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.IS_MUSIC
            )

            val selectionParts = mutableListOf("${MediaStore.Audio.Media.IS_MUSIC} = 1")
            val args = mutableListOf<String>()
            if (lastWatermarkSec != null) {
                selectionParts += "${MediaStore.Audio.Media.DATE_MODIFIED} > ?"
                args += lastWatermarkSec.toString()
            }

            resolver.query(
                collection,
                projection,
                selectionParts.joinToString(" AND "),
                args.toTypedArray(),
                "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val cId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val cTitle = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val cArtist = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val cAlbum = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val cAlbumId = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val cDuration = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val cDateModified = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
                val cTrack = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

                while (cursor.moveToNext()) {
                    val row = ScanRow(
                        mediaId = cursor.getLong(cId),
                        volume = volume,
                        contentUri = ContentUris.withAppendedId(collection, cursor.getLong(cId)).toString(),
                        title = cursor.getString(cTitle) ?: "Unknown Title",
                        artist = cursor.getString(cArtist) ?: "Unknown Artist",
                        album = cursor.getString(cAlbum) ?: "Unknown Album",
                        albumMediaId = cursor.getLong(cAlbumId),
                        durationMs = cursor.getLong(cDuration),
                        dateModifiedSec = cursor.getLong(cDateModified),
                        trackNumber = cursor.getInt(cTrack).takeIf { it > 0 }
                    )
                    collected += ScannedTrackFactory.fromMediaStore(
                        mediaId = row.mediaId,
                        volume = row.volume,
                        contentUri = row.contentUri,
                        title = row.title,
                        artist = row.artist,
                        album = row.album,
                        albumMediaId = row.albumMediaId,
                        durationMs = row.durationMs,
                        dateModifiedSec = row.dateModifiedSec,
                        trackNumber = row.trackNumber
                    )
                }
            }
        }

        return collected
    }
}

data class ScannedTrack(
    val trackId: String,
    val mediaId: Long,
    val volumeName: String,
    val contentUri: String,
    val title: String,
    val titleNormalized: String,
    val artistName: String,
    val artistNameNormalized: String,
    val albumName: String,
    val albumNameNormalized: String,
    val albumMediaId: Long?,
    val durationMs: Long,
    val trackNumber: Int?,
    val dateModifiedSec: Long
)
