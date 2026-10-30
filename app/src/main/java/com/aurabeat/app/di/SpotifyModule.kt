package com.aurabeat.app.di

import com.aurabeat.app.spotify.SpotifyAccountsApi
import com.aurabeat.app.spotify.SpotifyWebApi
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SpotifyModule {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Provides @Singleton
    fun provideOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        )
        .build()

    @Provides @Singleton @Named("accounts")
    fun provideAccountsRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://accounts.spotify.com/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton @Named("api")
    fun provideApiRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    @Provides @Singleton
    fun provideAccountsApi(@Named("accounts") retrofit: Retrofit): SpotifyAccountsApi =
        retrofit.create(SpotifyAccountsApi::class.java)

    @Provides @Singleton
    fun provideWebApi(@Named("api") retrofit: Retrofit): SpotifyWebApi =
        retrofit.create(SpotifyWebApi::class.java)
}
