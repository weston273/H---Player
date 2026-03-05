package com.hplayer.player.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.hplayer.player.analytics.PlayEventTracker
import com.hplayer.player.db.DatabaseProvider
import com.hplayer.player.db.views.TrackWithDisplay
import com.hplayer.player.model.PlaybackStateSnapshot
import com.hplayer.player.model.QueueItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlaybackEngine(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val db = DatabaseProvider.get(context)
    private val stateStore = PlaybackStateStore(context)
    private val playEventTracker = PlayEventTracker(db)

    private val player: ExoPlayer = ExoPlayer.Builder(context)
        .setHandleAudioBecomingNoisy(true)
        .build().apply {
            val attrs = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .build()
            setAudioAttributes(attrs, true)
        }

    val mediaSession: MediaSession = MediaSession.Builder(context, player)
        .setCallback(object : MediaSession.Callback {})
        .build()

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                publishAndPersistState()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    val currentId = player.currentMediaItem?.mediaId
                    val duration = (player.currentMediaItem?.localConfiguration?.tag as? TrackWithDisplay)?.durationMs
                        ?: (player.duration.takeIf { it > 0 } ?: 0L)
                    playEventTracker.onTrackStarted(currentId, duration)
                } else {
                    playEventTracker.onPausedOrStopped()
                }
                publishAndPersistState()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val duration = (mediaItem?.localConfiguration?.tag as? TrackWithDisplay)?.durationMs
                    ?: (player.duration.takeIf { it > 0 } ?: 0L)
                playEventTracker.onTrackStarted(mediaItem?.mediaId, duration)
                publishAndPersistState()
            }

            override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                publishAndPersistState()
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                publishAndPersistState()
            }
        })

        restoreQueueState()
    }

    fun release() {
        playEventTracker.onPausedOrStopped()
        publishAndPersistState()
        mediaSession.release()
        player.release()
    }

    fun getSnapshot(): PlaybackStateSnapshot {
        val queue = MutableList(player.mediaItemCount) { index ->
            QueueItem(trackId = player.getMediaItemAt(index).mediaId, index = index)
        }
        val repeatMode = when (player.repeatMode) {
            Player.REPEAT_MODE_ALL -> "all"
            Player.REPEAT_MODE_ONE -> "one"
            else -> "off"
        }

        return PlaybackStateSnapshot(
            isPlaying = player.isPlaying,
            currentTrackId = player.currentMediaItem?.mediaId,
            queue = queue,
            positionMs = player.currentPosition,
            bufferedMs = player.bufferedPosition,
            durationMs = player.duration.takeIf { it > 0 } ?: 0,
            shuffleEnabled = player.shuffleModeEnabled,
            repeatMode = repeatMode
        )
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun skipNext() {
        player.seekToNextMediaItem()
    }

    fun skipPrevious() {
        player.seekToPreviousMediaItem()
    }

    fun playTrack(trackId: String) {
        scope.launch {
            val page = withContext(Dispatchers.IO) { db.trackDao().getTracksPage(0, 1000) }
            if (page.isEmpty()) return@launch

            val mediaItems = page.map {
                MediaItem.Builder()
                    .setMediaId(it.trackId)
                    .setUri(Uri.parse(it.contentUri))
                    .setTag(it)
                    .build()
            }

            val targetIndex = mediaItems.indexOfFirst { it.mediaId == trackId }
            if (targetIndex < 0) return@launch

            player.setMediaItems(mediaItems, targetIndex, 0)
            player.prepare()
            player.play()
            publishAndPersistState()
        }
    }

    private fun restoreQueueState() {
        scope.launch {
            val queueState = withContext(Dispatchers.IO) { stateStore.readQueueState() }
            if (queueState.trackIds.isEmpty()) {
                publishAndPersistState()
                return@launch
            }

            val tracks = withContext(Dispatchers.IO) { db.trackDao().getTracksByIds(queueState.trackIds) }
            if (tracks.isEmpty()) {
                publishAndPersistState()
                return@launch
            }

            val orderedTracks = queueState.trackIds.mapNotNull { id -> tracks.find { it.trackId == id } }
            val mediaItems = orderedTracks.map {
                MediaItem.Builder()
                    .setMediaId(it.trackId)
                    .setUri(Uri.parse(it.contentUri))
                    .setTag(it)
                    .build()
            }

            val index = queueState.currentIndex.coerceIn(0, mediaItems.lastIndex)
            player.setMediaItems(mediaItems, index, queueState.positionMs)
            player.prepare()
            publishAndPersistState()
        }
    }

    private fun publishAndPersistState() {
        val snapshot = getSnapshot()
        PlaybackStatePublisher.publish(snapshot)
        stateStore.saveQueue(
            trackIds = snapshot.queue.map { it.trackId },
            currentIndex = player.currentMediaItemIndex.coerceAtLeast(0),
            positionMs = snapshot.positionMs
        )
    }
}
