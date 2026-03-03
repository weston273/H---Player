package com.hplayer.player.scan

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hplayer.player.repository.LibraryRepository

class IncrementalScanWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            val repository = LibraryRepository.get(applicationContext)
            repository.runIncrementalScan(listener = null)
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }
}
