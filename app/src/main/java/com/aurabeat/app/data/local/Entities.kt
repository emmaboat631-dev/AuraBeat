package com.aurabeat.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val songId: String,
    val addedAt: Long,
)

@Entity(tableName = "recently_played")
data class RecentlyPlayedEntity(
    @PrimaryKey val songId: String,
    val playedAt: Long,
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val createdAt: Long,
)

@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongCrossRef(
    val playlistId: String,
    val songId: String,
    val position: Int,
    // Cached copy of the song so device/imported tracks survive playlist reload.
    // Defaults keep old rows valid; MockCatalog lookup wins when the id is a bundled song.
    val title: String = "",
    val artistName: String = "",
    val albumName: String = "",
    val mediaUri: String = "",
    val durationMs: Long = 0L,
    val gradientColor1: Int = 0xFF7C3AED.toInt(),
    val gradientColor2: Int = 0xFF1E1B4B.toInt(),
)

@Entity(tableName = "imported_songs")
data class ImportedSongEntity(
    @PrimaryKey val uri: String,
    val title: String,
    val artistName: String,
    val albumName: String,
    val durationMs: Long,
    val addedAt: Long,
)
