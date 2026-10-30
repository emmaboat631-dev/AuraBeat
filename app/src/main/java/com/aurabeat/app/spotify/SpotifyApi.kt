package com.aurabeat.app.spotify

import com.aurabeat.app.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface SpotifyAccountsApi {
    @FormUrlEncoded
    @POST("api/token")
    suspend fun exchangeCode(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String = BuildConfig.SPOTIFY_REDIRECT_URI,
        @Field("client_id") clientId: String = BuildConfig.SPOTIFY_CLIENT_ID,
        @Field("code_verifier") codeVerifier: String,
    ): TokenResponse

    @FormUrlEncoded
    @POST("api/token")
    suspend fun refresh(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String = BuildConfig.SPOTIFY_CLIENT_ID,
    ): TokenResponse
}

interface SpotifyWebApi {
    @GET("v1/me")
    suspend fun me(@Header("Authorization") auth: String): UserProfile

    @GET("v1/search")
    suspend fun search(
        @Header("Authorization") auth: String,
        @Query("q") q: String,
        @Query("type") type: String = "track,artist,album",
        @Query("limit") limit: Int = 10,
    ): SearchResponse
}

@Serializable
data class TokenResponse(
    @SerialName("access_token")  val accessToken: String,
    @SerialName("token_type")    val tokenType: String,
    @SerialName("expires_in")    val expiresIn: Int,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("scope")         val scope: String? = null,
)

@Serializable
data class UserProfile(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    val email: String? = null,
    val country: String? = null,
    val product: String? = null,
    val images: List<Image> = emptyList(),
)

@Serializable
data class Image(val url: String, val height: Int? = null, val width: Int? = null)

@Serializable
data class SearchResponse(
    val tracks: TrackPage? = null,
)

@Serializable
data class TrackPage(val items: List<SpotifyTrack> = emptyList())

@Serializable
data class SpotifyTrack(
    val id: String,
    val name: String,
    val uri: String,
    val artists: List<SpotifyArtist> = emptyList(),
    val album: SpotifyAlbum? = null,
    @SerialName("duration_ms") val durationMs: Long = 0,
)

@Serializable
data class SpotifyArtist(val id: String, val name: String)

@Serializable
data class SpotifyAlbum(val id: String, val name: String, val images: List<Image> = emptyList())
