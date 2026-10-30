package com.aurabeat.app.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import android.os.Handler
import android.os.Looper
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.aurabeat.app.data.model.Song
import com.aurabeat.app.data.preferences.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for playback state. Wraps a Media3 MediaController
 * bound to [PlayerService]. Every screen observes [state]; nothing else drives playback.
 */
@Singleton
class AuraBeatPlayer @Inject constructor(
    private val context: Context,
    private val prefs: PreferencesRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private var progressJob: Job? = null

    fun initialize() {
        if (controller != null) return
        val token = SessionToken(context, ComponentName(context, PlayerService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        val mainExecutor = java.util.concurrent.Executor { command ->
            Handler(Looper.getMainLooper()).post(command)
        }
        future.addListener({
            controller = future.get().also { c ->
                c.addListener(listener)
                syncFromController()
                scope.launch {
                    val saved = prefs.prefs.first()
                    c.shuffleModeEnabled = saved.shuffle
                    c.repeatMode = when (saved.repeat) {
                        RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                        RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                        RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                    }
                }
            }
        }, mainExecutor)
    }

    fun release() {
        controller?.release()
        controller = null
        progressJob?.cancel()
    }

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.update { it.copy(isPlaying = isPlaying, error = if (isPlaying) null else it.error) }
            if (isPlaying) startProgressLoop() else progressJob?.cancel()
        }
        override fun onPlaybackStateChanged(state: Int) {
            _state.update {
                it.copy(
                    isBuffering = state == Player.STATE_BUFFERING,
                    durationMs = controller?.duration?.coerceAtLeast(0L) ?: 0L,
                )
            }
        }
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) = syncFromController()
        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _state.update { it.copy(shuffleEnabled = shuffleModeEnabled) }
            scope.launch { prefs.setShuffle(shuffleModeEnabled) }
        }
        override fun onRepeatModeChanged(repeatMode: Int) {
            val mode = repeatMode.toRepeatMode()
            _state.update { it.copy(repeatMode = mode) }
            scope.launch { prefs.setRepeat(mode) }
        }
        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            _state.update { it.copy(error = error.localizedMessage ?: "Playback failed", isPlaying = false, isBuffering = false) }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }

    private fun syncFromController() {
        val c = controller ?: return
        val idx = c.currentMediaItemIndex
        val queue = _state.value.queue
        _state.update {
            it.copy(
                queueIndex = idx.coerceIn(-1, queue.size - 1),
                currentSong = queue.getOrNull(idx),
                durationMs = c.duration.coerceAtLeast(0L),
                positionMs = c.currentPosition.coerceAtLeast(0L),
            )
        }
    }

    private fun startProgressLoop() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                controller?.let { c ->
                    _state.update {
                        it.copy(
                            positionMs = c.currentPosition.coerceAtLeast(0L),
                            bufferedMs = c.bufferedPosition.coerceAtLeast(0L),
                            durationMs = c.duration.coerceAtLeast(0L),
                        )
                    }
                }
                delay(500)
            }
        }
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        val c = controller
        _state.update { it.copy(queue = songs, queueIndex = startIndex, currentSong = songs.getOrNull(startIndex)) }
        if (c == null) return
        c.setMediaItems(songs.map { it.toMediaItem(context) }, startIndex, 0L)
        c.prepare()
        c.play()
    }

    fun playSong(song: Song) = playQueue(listOf(song), 0)

    fun togglePlay() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }
    fun seekToFraction(f: Float) {
        val d = _state.value.durationMs
        if (d > 0) seekTo((d * f).toLong().coerceIn(0L, d))
    }
    fun next() {
        val c = controller ?: return
        when {
            c.hasNextMediaItem() -> c.seekToNextMediaItem()
            c.mediaItemCount > 0 -> c.seekTo(0, 0L) // wrap around to the first track
        }
    }

    fun previous() {
        val c = controller ?: return
        // If we're a couple seconds into a track, tapping back restarts it (standard music-app behavior).
        // Otherwise go to previous — wrapping to the last if we're already on the first.
        if (c.currentPosition > 3_000) { c.seekTo(0L); return }
        when {
            c.hasPreviousMediaItem() -> c.seekToPreviousMediaItem()
            c.mediaItemCount > 0 -> c.seekTo(c.mediaItemCount - 1, 0L)
        }
    }
    fun toggleShuffle() {
        val c = controller ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }
    fun cycleRepeat() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun addToQueue(song: Song) {
        controller?.addMediaItem(song.toMediaItem(context))
        _state.update { it.copy(queue = it.queue + song) }
    }

    fun removeFromQueue(index: Int) {
        controller?.removeMediaItem(index)
        _state.update {
            val newQueue = it.queue.toMutableList().apply { if (index in indices) removeAt(index) }
            it.copy(queue = newQueue)
        }
    }

    fun moveInQueue(from: Int, to: Int) {
        controller?.moveMediaItem(from, to)
        _state.update {
            val newQueue = it.queue.toMutableList().apply { add(to, removeAt(from)) }
            it.copy(queue = newQueue)
        }
    }

    fun playAtQueueIndex(index: Int) {
        controller?.seekTo(index, 0L)
        controller?.play()
    }
}

private fun Int.toRepeatMode(): RepeatMode = when (this) {
    Player.REPEAT_MODE_ONE -> RepeatMode.ONE
    Player.REPEAT_MODE_ALL -> RepeatMode.ALL
    else -> RepeatMode.OFF
}

fun Song.toMediaItem(context: Context): MediaItem = MediaItem.Builder()
    .setMediaId(id)
    .setUri(mediaUri ?: "asset:///silent.mp3")
    .setMediaMetadata(
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artistName)
            .setAlbumTitle(albumName)
            // Artwork intentionally not embedded here — pre-extracting via
            // MediaMetadataRetriever for every song synchronously locks file
            // descriptors and makes ExoPlayer throw Source errors on large
            // queues. The Compose UI still shows the extracted art via
            // SongArtwork (which does it off the main thread and lazily).
            .build()
    )
    .build()
