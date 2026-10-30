package com.aurabeat.app.player

import com.aurabeat.app.data.model.Song

enum class RepeatMode { OFF, ONE, ALL }

data class PlayerState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val queue: List<Song> = emptyList(),
    val queueIndex: Int = -1,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val volume: Float = 1f,
    val error: String? = null,
) {
    val progress: Float get() = if (durationMs > 0) (positionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    val hasNext: Boolean get() = queueIndex >= 0 && queueIndex < queue.size - 1
    val hasPrevious: Boolean get() = queueIndex > 0
}
