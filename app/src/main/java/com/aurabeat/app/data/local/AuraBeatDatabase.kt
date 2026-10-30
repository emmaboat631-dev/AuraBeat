package com.aurabeat.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        RecentlyPlayedEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        ImportedSongEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AuraBeatDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentlyPlayedDao(): RecentlyPlayedDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun importedSongDao(): ImportedSongDao
}
