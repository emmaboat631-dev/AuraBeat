package com.aurabeat.app.presentation.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurabeat.app.data.model.Album
import com.aurabeat.app.data.model.Artist
import com.aurabeat.app.data.model.Genre
import com.aurabeat.app.data.model.Playlist
import com.aurabeat.app.data.model.Song
import com.aurabeat.app.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class HomeUiState(
    val genres: List<Genre> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val recent: List<Song> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: MusicRepository,
) : ViewModel() {
    var state by mutableStateOf(
        HomeUiState(
            genres = repo.allGenres(),
            albums = repo.allAlbums(),
            artists = repo.allArtists(),
            playlists = repo.allPlaylists(),
            recent = repo.allSongs().take(8),
        )
    )
        private set

    init {
        repo.recentSongs()
            .onEach { recents ->
                val list = recents.ifEmpty { repo.allSongs().take(8) }
                state = state.copy(recent = list)
            }
            .launchIn(viewModelScope)
    }
}
