package com.hplayer.player.scan

data class ScanProgress(
    val stage: String,
    val processed: Int,
    val total: Int,
    val message: String? = null
)

fun interface ScanProgressListener {
    fun onProgress(progress: ScanProgress)
}
