package com.hplayer.player.bridge

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.documentfile.provider.DocumentFile
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.hplayer.player.repository.LibraryRepository
import com.hplayer.player.scan.ScanProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MediaStoreModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val libraryRepository = LibraryRepository.get(reactContext)

    override fun getName(): String = "MediaStoreModule"

    @ReactMethod
    fun addListener(eventName: String) = Unit

    @ReactMethod
    fun removeListeners(count: Int) = Unit

    @ReactMethod
    fun runIncrementalScan(promise: Promise) {
        scope.launch {
            runCatching {
                libraryRepository.runIncrementalScan(
                    listener = ScanProgressListener { progress ->
                        val map = Arguments.createMap().apply {
                            putString("stage", progress.stage)
                            putInt("processed", progress.processed)
                            putInt("total", progress.total)
                            putString("message", progress.message)
                        }
                        reactContext
                            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                            .emit("scanProgress", map)
                    }
                )
            }.fold(
                onSuccess = { promise.resolve(null) },
                onFailure = { promise.reject("SCAN_FAILED", it) }
            )
        }
    }

    @ReactMethod
    fun getTracks(offset: Double, limit: Double, promise: Promise) {
        scope.launch {
            runCatching {
                libraryRepository.getTracksPage(offset.toInt(), limit.toInt()).toWritableArray()
            }.fold(
                onSuccess = { promise.resolve(it) },
                onFailure = { promise.reject("TRACKS_FAILED", it) }
            )
        }
    }

    @ReactMethod
    fun persistDocumentTree(treeUriString: String, promise: Promise) {
        runCatching {
            val uri = Uri.parse(treeUriString)
            reactContext.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val document = DocumentFile.fromTreeUri(reactContext, uri)
            val canRead = document?.canRead() == true
            promise.resolve(canRead)
        }.onFailure {
            promise.reject("SAF_PERSIST_FAILED", it)
        }
    }

    @ReactMethod
    fun getSdkInt(promise: Promise) {
        promise.resolve(Build.VERSION.SDK_INT)
    }
}
