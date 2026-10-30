package com.aurabeat.app.spotify

import android.net.Uri
import android.util.Base64
import com.aurabeat.app.BuildConfig
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Spotify OAuth 2.0 Authorization Code Flow with PKCE. Runs entirely
 * client-side — no client secret ever leaves the phone.
 *
 * https://developer.spotify.com/documentation/web-api/tutorials/code-pkce-flow
 */
object SpotifyAuth {

    /** Scopes we ask for: profile identity, playback control, library reads. */
    const val SCOPES = "user-read-private user-read-email " +
        "user-read-playback-state user-modify-playback-state user-read-currently-playing " +
        "streaming user-library-read user-top-read playlist-read-private"

    private const val AUTH_ENDPOINT = "https://accounts.spotify.com/authorize"

    private val base64Flags = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP

    /** 64 random bytes → 43+ char code_verifier (URL-safe base64, no padding). */
    fun generateCodeVerifier(): String {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, base64Flags)
    }

    /** SHA-256 of the verifier, base64url-encoded, becomes the code_challenge. */
    fun codeChallengeFor(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray())
        return Base64.encodeToString(digest, base64Flags)
    }

    /** Random state string to defend against CSRF. */
    fun generateState(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, base64Flags)
    }

    fun buildAuthUrl(codeChallenge: String, state: String): Uri =
        Uri.parse(AUTH_ENDPOINT).buildUpon()
            .appendQueryParameter("client_id", BuildConfig.SPOTIFY_CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", BuildConfig.SPOTIFY_REDIRECT_URI)
            .appendQueryParameter("state", state)
            .appendQueryParameter("scope", SCOPES)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", codeChallenge)
            .build()
}
