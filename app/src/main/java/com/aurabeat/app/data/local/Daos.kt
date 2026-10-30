package com.aurabeat.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT songId FROM favorites ORDER BY addedAt DESC")
    fun favoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE songId = :id")
    suspend fun remove(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :id)")
    fun isFavorite(id: String): Flow<Boolean>
}

@Dao
interface RecentlyPlayedDao {
    @Query("SELECT songId FROM recently_played ORDER BY playedAt DESC LIMIT 20")
    fun recentIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun log(entry: RecentlyPlayedEntity)
}

@Dao
interface ImportedSongDao {
    @Query("SELECT * FROM imported_songs ORDER BY addedAt DESC")
    fun all(): Flow<List<ImportedSongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(entity: ImportedSongEntity)

    @Query("DELETE FROM imported_songs WHERE uri = :uri")
    suspend fun remove(uri: String)

    @Query("SELECT COUNT(*) FROM imported_songs")
    fun count(): Flow<Int>
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun playlists(): Flow<List<PlaylistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(p: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSong(ref: PlaylistSongCrossRef)

    @Query("SELECT songId FROM playlist_songs WHERE playlistId = :id ORDER BY position ASC")
    fun songs(id: String): Flow<List<String>>

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :id ORDER BY position ASC")
    fun playlistItems(id: String): Flow<List<PlaylistSongCrossRef>>
}
