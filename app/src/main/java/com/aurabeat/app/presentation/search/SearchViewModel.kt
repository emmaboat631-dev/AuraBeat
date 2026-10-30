package com.aurabeat.app.presentation.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurabeat.app.data.repository.MusicRepository
import com.aurabeat.app.data.repository.SearchResults
import com.aurabeat.app.spotify.SpotifyPlaybackController
import com.aurabeat.app.spotify.SpotifyRepository
import com.aurabeat.app.spotify.SpotifyTrack
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SearchFilter(val label: String) { ALL("All"), SONGS("Songs"), ARTISTS("Artists"), ALBUMS("Albums") }

data class SearchUiState(
    val query: String = "",
    val filter: SearchFilter = SearchFilter.ALL,
    val results: SearchResults = SearchResults(),
    val spotifyResults: List<SpotifyTrack> = emptyList(),
    val spotifyConnected: Boolean = false,
    val spotifyLoading: Boolean = false,
    val spotifyError: String? = null,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: MusicRepository,
    private val spotify: SpotifyRepository,
    private val spotifyPlayback: SpotifyPlaybackController,
) : ViewModel() {
    var state by mutableStateOf(SearchUiState())
        private set

    private val queryFlow = MutableStateFlow("")
    private var spotifyJob: Job? = null

    init {
        // Local search — instant
        queryFlow
            .debounce(120)
            .onEach { q -> state = state.copy(results = repo.search(q)) }
            .launchIn(viewModelScope)

        // Spotify search — separate flow so slow network doesn't block local results
        queryFlow
            .debounce(400)
            .onEach { q -> searchSpotify(q) }
            .launchIn(viewModelScope)

        // Track connection status so the section hides when signed out
        spotify.authState
            .onEach { auth -> state = state.copy(spotifyConnected = auth.connected) }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(q: String) {
        state = state.copy(query = q)
        queryFlow.value = q
    }

    fun onFilterChanged(filter: SearchFilter) {
        state = state.copy(filter = filter)
    }

    fun playOnSpotify(track: SpotifyTrack, playerVm: com.aurabeat.app.presentation.PlayerViewModel) {
        // Pause any local playback so both engines don't fight over the audio focus
        if (playerVm.state.value.isPlaying) playerVm.togglePlay()
        viewModelScope.launch { spotifyPlayback.playTrack(track.uri, track) }
    }

    private fun searchSpotify(q: String) {
        spotifyJob?.cancel()
        if (q.isBlank() || !state.spotifyConnected) {
            state = state.copy(spotifyResults = emptyList(), spotifyLoading = false, spotifyError = null)
            return
        }
        spotifyJob = viewModelScope.launch {
            state = state.copy(spotifyLoading = true, spotifyError = null)
            state = when (val outcome = spotify.searchTracks(q)) {
                is SpotifyRepository.SearchOutcome.Success ->
                    state.copy(spotifyResults = outcome.tracks, spotifyLoading = false, spotifyError = null)
                is SpotifyRepository.SearchOutcome.Denied ->
                    state.copy(spotifyResults = emptyList(), spotifyLoading = false, spotifyError = outcome.reason)
                SpotifyRepository.SearchOutcome.NotAuthenticated ->
                    state.copy(spotifyResults = emptyList(), spotifyLoading = false, spotifyError = null)
            }
        }
    }
}
