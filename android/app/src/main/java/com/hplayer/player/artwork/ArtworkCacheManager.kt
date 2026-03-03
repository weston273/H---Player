package com.hplayer.player.artwork

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.LruCache
import com.hplayer.player.db.dao.ArtworkCacheDao
import com.hplayer.player.db.entities.ArtworkCacheEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ArtworkCacheManager(
    private val context: Context,
    private val artworkDao: ArtworkCacheDao
) {
    private val memoryCache = object : LruCache<String, Bitmap>((Runtime.getRuntime().maxMemory() / 16).toInt()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    suspend fun getOrExtract(trackId: String, contentUri: String, artworkKey: String): Bitmap? = withContext(Dispatchers.IO) {
        memoryCache.get(artworkKey)?.let { return@withContext it }

        artworkDao.getByTrackId(trackId)?.let { cached ->
            val file = File(cached.diskPath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    memoryCache.put(artworkKey, bitmap)
                    touch(cached)
                    return@withContext bitmap
                }
            }
        }

        val extracted = extractEmbedded(contentUri) ?: return@withContext null
        memoryCache.put(artworkKey, extracted)

        val diskFile = persistToDisk(artworkKey, extracted)
        artworkDao.upsert(
            ArtworkCacheEntity(
                artworkKey = artworkKey,
                trackId = trackId,
                diskPath = diskFile.absolutePath,
                width = extracted.width,
                height = extracted.height,
                byteSize = extracted.byteCount.toLong(),
                lastAccessedAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
        )

        // Keep disk cache bounded.
        artworkDao.evictLeastRecentlyUsed(20)

        extracted
    }

    private fun extractEmbedded(contentUri: String): Bitmap? {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            context.contentResolver.openFileDescriptor(Uri.parse(contentUri), "r")?.use { pfd ->
                retriever.setDataSource(pfd.fileDescriptor)
            }
            val picture = retriever.embeddedPicture
            retriever.release()

            picture?.let {
                val options = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                BitmapFactory.decodeByteArray(it, 0, it.size, options)
            }
        }.getOrNull()
    }

    private fun persistToDisk(artworkKey: String, bitmap: Bitmap): File {
        val folder = File(context.cacheDir, "artwork_cache").apply { mkdirs() }
        val file = File(folder, "$artworkKey.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 86, out)
        }
        return file
    }

    private suspend fun touch(entry: ArtworkCacheEntity) {
        artworkDao.upsert(entry.copy(lastAccessedAt = System.currentTimeMillis()))
    }
}
