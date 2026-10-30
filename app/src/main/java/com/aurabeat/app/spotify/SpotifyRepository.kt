package com.aurabeat.app.spotify

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.spotifyStore by preferencesDataStore(name = "spotify_prefs")

data class SpotifyAuthState(
    val connected: Boolean = false,
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresAt: Long = 0,
    val profile: UserProfile? = null,
    val error: String? = null,
)

@Singleton
class SpotifyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accounts: SpotifyAccountsApi,
    private val webApi: SpotifyWebApi,
) {
    private object Keys {
        val ACCESS       = stringPreferencesKey("access_token")
        val REFRESH      = stringPreferencesKey("refresh_token")
        val EXPIRES_AT   = longPreferencesKey("expires_at")
        val CODE_VERIFIER = stringPreferencesKey("pkce_verifier")
        val OAUTH_STATE  = stringPreferencesKey("oauth_state")
    }

    val authState: Flow<SpotifyAuthState> = context.spotifyStore.data.map { s ->
        SpotifyAuthState(
            connected  = !s[Keys.ACCESS].isNullOrBlank(),
            accessToken = s[Keys.ACCESS],
            refreshToken = s[Keys.REFRESH],
            expiresAt = s[Keys.EXPIRES_AT] ?: 0,
        )
    }

    /**
     * Launches Custom Tabs to Spotify's authorize endpoint. When the user
     * approves, Spotify redirects back to `com.aurabeat.app://spotify-callback`
     * which MainActivity picks up and calls [handleRedirect].
     */
    suspend fun startAuthorization() {
        val verifier = SpotifyAuth.generateCodeVerifier()
        val challenge = SpotifyAuth.codeChallengeFor(verifier)
        val state = SpotifyAuth.generateState()
        context.spotifyStore.edit {
            it[Keys.CODE_VERIFIER] = verifier
            it[Keys.OAUTH_STATE] = state
        }
        val url = SpotifyAuth.buildAuthUrl(challenge, state)
        val tabs = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        tabs.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        tabs.launchUrl(context, url)
    }

    /** Called from MainActivity with the redirect URI containing `code` + `state`. */
    suspend fun handleRedirect(uri: Uri): Result<Unit> = runCatching {
        val err = uri.getQueryParameter("error")
        if (err != null) throw IllegalStateException("Spotify auth denied: $err")
        val code = uri.getQueryParameter("code")
            ?: throw IllegalStateException("Missing authorization code")
        val state = uri.getQueryParameter("state")
        val store = context.spotifyStore.data.first()
        if (state != store[Keys.OAUTH_STATE])
            throw IllegalStateException("OAuth state mismatch")
        val verifier = store[Keys.CODE_VERIFIER]
            ?: throw IllegalStateException("Missing PKCE verifier")

        val token = accounts.exchangeCode(code = code, codeVerifier = verifier)
        saveToken(token)
    }

    suspend fun disconnect() {
        context.spotifyStore.edit { it.clear() }
    }

    /** Returns a valid access token, refreshing if needed. Null if not signed in. */
    suspend fun validAccessToken(): String? {
        val store = context.spotifyStore.data.first()
        val access = store[Keys.ACCESS] ?: return null
        val expires = store[Keys.EXPIRES_AT] ?: 0
        val now = System.currentTimeMillis()
        if (now < expires - 60_000) return access
        val refresh = store[Keys.REFRESH] ?: return access // no refresh token — return anyway; API will 401
        return try {
            val fresh = accounts.refresh(refreshToken = refresh)
            saveToken(fresh, fallbackRefresh = refresh)
            fresh.accessToken
        } catch (_: Throwable) {
            null
        }
    }

    suspend fun profile(): UserProfile? {
        val token = validAccessToken() ?: return null
        return runCatching { webApi.me("Bearer $token") }.getOrNull()
    }

    sealed interface SearchOutcome {
        data class Success(val tracks: List<SpotifyTrack>) : SearchOutcome
        data object NotAuthenticated : SearchOutcome
        data class Denied(val reason: String) : SearchOutcome
    }

    // Simple in-memory cache to protect against rate-limits from repeat searches.
    // Key = lowercased query, value = (tracks, timestamp). 5 minute TTL.
    private data class CacheEntry(val tracks: List<SpotifyTrack>, val at: Long)
    private val searchCache = java.util.concurrent.ConcurrentHashMap<String, CacheEntry>()
    private val cacheTtlMs = 5 * 60 * 1000L

    suspend fun searchTracks(query: String): SearchOutcome {
        val key = query.trim().lowercase()
        if (key.isEmpty()) return SearchOutcome.Success(emptyList())

        // Serve from cache when fresh — no API call at all
        searchCache[key]?.let { hit ->
            if (System.currentTimeMillis() - hit.at < cacheTtlMs) {
                return SearchOutcome.Success(hit.tracks)
            }
        }

        val token = validAccessToken() ?: return SearchOutcome.NotAuthenticated
        return searchOnce(token, query)
            .let { outcome ->
                if (outcome is SearchOutcome.Denied && outcome.reason.startsWith("Too many")) {
                    // One retry after 800ms for rate-limit blips
                    kotlinx.coroutines.delay(800)
                    searchOnce(token, query)
                } else outcome
            }
            .also { outcome ->
                if (outcome is SearchOutcome.Success) {
                    searchCache[key] = CacheEntry(outcome.tracks, System.currentTimeMillis())
                }
            }
    }

    private suspend fun searchOnce(token: String, query: String): SearchOutcome = try {
        val items = webApi.search(auth = "Bearer $token", q = query).tracks?.items ?: emptyList()
        SearchOutcome.Success(items)
    } catch (e: retrofit2.HttpException) {
        val body = try { e.response()?.errorBody()?.string() } catch (_: Throwable) { null }
        android.util.Log.w("Spotify", "search HTTP ${e.code()}: ${e.message()} — body=$body")
        SearchOutcome.Denied(
            when (e.code()) {
                401 -> "Session expired — reconnect Spotify from Profile"
                403 -> "Spotify wouldn't return results for “$query”. Try a longer/more common query, or open in Spotify below."
                429 -> "Too many requests to Spotify — wait a moment and try again"
                else -> "Spotify search error ${e.code()}"
            }
        )
    } catch (e: Throwable) {
        android.util.Log.w("Spotify", "search failed", e)
        SearchOutcome.Denied("Spotify unreachable — check your connection")
    }

    private suspend fun saveToken(response: TokenResponse, fallbackRefresh: String? = null) {
        val expiresAt = System.currentTimeMillis() + response.expiresIn * 1000L
        context.spotifyStore.edit {
            it[Keys.ACCESS] = response.accessToken
            it[Keys.EXPIRES_AT] = expiresAt
            (response.refreshToken ?: fallbackRefresh)?.let { rt -> it[Keys.REFRESH] = rt }
            it.remove(Keys.CODE_VERIFIER)
            it.remove(Keys.OAUTH_STATE)
        }
    }
}
