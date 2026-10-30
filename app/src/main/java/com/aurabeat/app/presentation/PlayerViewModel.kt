package com.aurabeat.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurabeat.app.data.model.Song
import com.aurabeat.app.data.repository.MusicRepository
import com.aurabeat.app.player.AuraBeatPlayer
import com.aurabeat.app.player.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val player: AuraBeatPlayer,
    private val repo: MusicRepository,
) : ViewModel() {

    val state: StateFlow<PlayerState> = player.state

    fun initialize() = player.initialize()

    fun play(song: Song, contextQueue: List<Song> = listOf(song)) {
        val idx = contextQueue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        player.playQueue(contextQueue, idx)
        viewModelScope.launch { repo.logPlayed(song.id) }
    }

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        player.playQueue(songs, startIndex)
        viewModelScope.launch { songs.getOrNull(startIndex)?.let { repo.logPlayed(it.id) } }
    }

    fun isFavorite(songId: String): Flow<Boolean> = repo.isFavorite(songId)

    fun toggleFavorite(songId: String) {
        viewModelScope.launch {
            val currently = repo.isFavorite(songId).first()
            repo.toggleFavorite(songId, currently)
        }
    }

    fun isPlaylistFavorite(id: String): Flow<Boolean> = repo.isPlaylistFavorite(id)
    fun togglePlaylistFavorite(id: String) {
        viewModelScope.launch {
            val currently = repo.isPlaylistFavorite(id).first()
            repo.togglePlaylistFavorite(id, currently)
        }
    }

    fun isAlbumFavorite(id: String): Flow<Boolean> = repo.isAlbumFavorite(id)
    fun toggleAlbumFavorite(id: String) {
        viewModelScope.launch {
            val currently = repo.isAlbumFavorite(id).first()
            repo.toggleAlbumFavorite(id, currently)
        }
    }

    fun userPlaylists(): Flow<List<com.aurabeat.app.data.model.Playlist>> = repo.userPlaylists()

    fun addSongToPlaylist(playlistId: String, song: Song) {
        viewModelScope.launch {
            val existing = repo.playlistSongCount(playlistId)
            repo.addSongToPlaylist(playlistId, song, existing)
        }
    }

    fun createPlaylistAndAdd(title: String, song: Song) {
        viewModelScope.launch {
            val id = repo.createPlaylist(title)
            repo.addSongToPlaylist(id, song, 0)
        }
    }

    fun togglePlay() = player.togglePlay()
    fun next() = player.next()
    fun previous() = player.previous()
    fun seekFraction(f: Float) = player.seekToFraction(f)
    fun seekTo(ms: Long) = player.seekTo(ms)
    fun toggleShuffle() = player.toggleShuffle()
    fun cycleRepeat() = player.cycleRepeat()
    fun addToQueue(song: Song) = player.addToQueue(song)
    fun removeFromQueue(index: Int) = player.removeFromQueue(index)
    fun moveInQueue(from: Int, to: Int) = player.moveInQueue(from, to)
    fun playAtQueueIndex(index: Int) = player.playAtQueueIndex(index)

    override fun onCleared() {
        // Player is app-scoped; do not release here.
    }
}
