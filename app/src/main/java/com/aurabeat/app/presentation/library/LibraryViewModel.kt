package com.aurabeat.app.presentation.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurabeat.app.data.model.Playlist
import com.aurabeat.app.data.model.Song
import com.aurabeat.app.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val favorites: List<Song> = emptyList(),
    val recent: List<Song> = emptyList(),
    val curatedPlaylists: List<Playlist> = emptyList(),
    val userPlaylists: List<Playlist> = emptyList(),
    val showCreateDialog: Boolean = false,
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repo: MusicRepository,
) : ViewModel() {
    var state by mutableStateOf(LibraryUiState(curatedPlaylists = repo.allPlaylists()))
        private set

    init {
        repo.favoriteSongs().onEach { state = state.copy(favorites = it) }.launchIn(viewModelScope)
        repo.recentSongs().onEach { state = state.copy(recent = it) }.launchIn(viewModelScope)
        repo.userPlaylists().onEach { state = state.copy(userPlaylists = it) }.launchIn(viewModelScope)
    }

    fun openCreateDialog() { state = state.copy(showCreateDialog = true) }
    fun dismissCreateDialog() { state = state.copy(showCreateDialog = false) }

    fun createPlaylist(title: String) {
        val name = title.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            repo.createPlaylist(name)
            state = state.copy(showCreateDialog = false)
        }
    }
}
