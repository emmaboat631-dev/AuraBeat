package com.aurabeat.app.di

import android.content.Context
import androidx.room.Room
import com.aurabeat.app.data.local.AuraBeatDatabase
import com.aurabeat.app.data.local.FavoriteDao
import com.aurabeat.app.data.local.ImportedSongDao
import com.aurabeat.app.data.local.PlaylistDao
import com.aurabeat.app.data.local.RecentlyPlayedDao
import com.aurabeat.app.data.preferences.PreferencesRepository
import com.aurabeat.app.player.AuraBeatPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AuraBeatDatabase =
        Room.databaseBuilder(ctx, AuraBeatDatabase::class.java, "aurabeat.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideFavoriteDao(db: AuraBeatDatabase): FavoriteDao = db.favoriteDao()
    @Provides fun provideRecentlyPlayedDao(db: AuraBeatDatabase): RecentlyPlayedDao = db.recentlyPlayedDao()
    @Provides fun providePlaylistDao(db: AuraBeatDatabase): PlaylistDao = db.playlistDao()
    @Provides fun provideImportedSongDao(db: AuraBeatDatabase): ImportedSongDao = db.importedSongDao()

    @Provides @Singleton
    fun providePlayer(
        @ApplicationContext ctx: Context,
        prefs: PreferencesRepository,
    ): AuraBeatPlayer = AuraBeatPlayer(ctx, prefs)
}
