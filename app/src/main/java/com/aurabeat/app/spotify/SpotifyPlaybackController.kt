package com.aurabeat.app.spotify

import android.content.Context
import android.util.Log
import com.aurabeat.app.BuildConfig
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Snapshot of what Spotify is doing right now (via App Remote). Independent
 * from AuraBeat's own Media3-backed PlayerState — the two engines run side
 * by side.
 */
data class SpotifyPlaybackSnapshot(
    val active: Boolean = false,        // has a Spotify track been kicked off in this session
    val connecting: Boolean = false,
    val isPlaying: Boolean = false,
    val trackName: String? = null,
    val artistName: String? = null,
    val albumName: String? = null,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val error: String? = null,
)

@Singleton
class SpotifyPlaybackController @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val TAG = "SpotifyAppRemote"

    private var remote: SpotifyAppRemote? = null
    private val _state = MutableStateFlow(SpotifyPlaybackSnapshot())
    val state: StateFlow<SpotifyPlaybackSnapshot> = _state.asStateFlow()

    /** Ensures we hold a live connection to the Spotify app. Suspends until connected or failed. */
    suspend fun ensureConnected(): Boolean {
        remote?.let { if (it.isConnected) return true }
        _state.update { it.copy(connecting = true, error = null) }

        val done = CompletableDeferred<Boolean>()
        val params = ConnectionParams.Builder(BuildConfig.SPOTIFY_CLIENT_ID)
            .setRedirectUri(BuildConfig.SPOTIFY_REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, params, object : Connector.ConnectionListener {
            override fun onConnected(appRemote: SpotifyAppRemote) {
                Log.d(TAG, "App Remote connected")
                remote = appRemote
                subscribe(appRemote)
                _state.update { it.copy(connecting = false, error = null) }
                done.complete(true)
            }
            override fun onFailure(throwable: Throwable) {
                val message = throwable.message ?: throwable::class.java.simpleName
                Log.w(TAG, "App Remote failure: $message")
                _state.update { it.copy(connecting = false, error = friendlyError(message)) }
                done.complete(false)
            }
        })
        return done.await()
    }

    fun disconnect() {
        remote?.let { SpotifyAppRemote.disconnect(it) }
        remote = null
        _state.update { it.copy(isPlaying = false, active = false) }
    }

    suspend fun playTrack(spotifyUri: String, displayHint: SpotifyTrack? = null) {
        if (!ensureConnected()) return
        try {
            remote?.playerApi?.play(spotifyUri)
            _state.update {
                it.copy(
                    active = true,
                    trackName = displayHint?.name ?: it.trackName,
                    artistName = displayHint?.artists?.joinToString { a -> a.name } ?: it.artistName,
                    albumName = displayHint?.album?.name ?: it.albumName,
                    durationMs = displayHint?.durationMs ?: it.durationMs,
                    error = null,
                )
            }
        } catch (t: Throwable) {
            _state.update { it.copy(error = friendlyError(t.message)) }
        }
    }

    suspend fun togglePlay() {
        if (!ensureConnected()) return
        val playing = _state.value.isPlaying
        if (playing) remote?.playerApi?.pause() else remote?.playerApi?.resume()
    }

    suspend fun skipNext()     { if (ensureConnected()) remote?.playerApi?.skipNext() }
    suspend fun skipPrevious() { if (ensureConnected()) remote?.playerApi?.skipPrevious() }

    private fun subscribe(appRemote: SpotifyAppRemote) {
        appRemote.playerApi.subscribeToPlayerState()
            .setEventCallback { playerState: PlayerState ->
                val track = playerState.track
                _state.update { current ->
                    current.copy(
                        isPlaying = !playerState.isPaused,
                        trackName = track?.name ?: current.trackName,
                        artistName = track?.artist?.name ?: current.artistName,
                        albumName = track?.album?.name ?: current.albumName,
                        durationMs = track?.duration ?: current.durationMs,
                        positionMs = playerState.playbackPosition,
                    )
                }
            }
            .setErrorCallback { t ->
                _state.update { it.copy(error = friendlyError(t.message)) }
            }
    }

    private fun friendlyError(raw: String?): String = when {
        raw == null -> "Spotify unavailable"
        raw.contains("CouldNotFindSpotifyApp", ignoreCase = true) -> "Install the Spotify app to play tracks here"
        raw.contains("NotLoggedInException", ignoreCase = true) -> "Sign in to the Spotify app first"
        raw.contains("UserNotAuthorizedException", ignoreCase = true) -> "Spotify blocked this — Premium may be required"
        else -> raw
    }
}
