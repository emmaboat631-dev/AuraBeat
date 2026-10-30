package com.aurabeat.app.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.aurabeat.app.data.MockCatalog
import com.aurabeat.app.data.local.FavoriteDao
import com.aurabeat.app.data.local.FavoriteEntity
import com.aurabeat.app.data.local.PlaylistDao
import com.aurabeat.app.data.local.PlaylistEntity
import com.aurabeat.app.data.local.PlaylistSongCrossRef
import com.aurabeat.app.data.local.RecentlyPlayedDao
import com.aurabeat.app.data.local.RecentlyPlayedEntity
import com.aurabeat.app.data.model.Album
import com.aurabeat.app.data.model.Artist
import com.aurabeat.app.data.model.Genre
import com.aurabeat.app.data.model.Playlist
import com.aurabeat.app.data.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val favoriteDao: FavoriteDao,
    private val recentDao: RecentlyPlayedDao,
    private val playlistDao: PlaylistDao,
) {
    fun allSongs(): List<Song> = MockCatalog.songs
    fun allArtists(): List<Artist> = MockCatalog.artists
    fun allAlbums(): List<Album> = MockCatalog.albums
    fun allPlaylists(): List<Playlist> = MockCatalog.playlists
    fun allGenres(): List<Genre> = MockCatalog.genres

    fun song(id: String): Song? = MockCatalog.songById(id)
    fun album(id: String): Album? = MockCatalog.albumById(id)
    fun artist(id: String): Artist? = MockCatalog.artistById(id)
    fun playlist(id: String): Playlist? = MockCatalog.playlistById(id)
    fun songsIn(ids: List<String>): List<Song> = MockCatalog.songsIn(ids)

    /** Live view of a playlist — merges the curated catalog with user-created
     *  playlists in Room so opening either kind hits the same screen. */
    fun playlistFlow(id: String): Flow<Playlist?> =
        userPlaylists().map { userList ->
            userList.firstOrNull { it.id == id } ?: MockCatalog.playlistById(id)
        }

    fun search(query: String): SearchResults {
        if (query.isBlank()) return SearchResults()
        val q = query.trim().lowercase()
        return SearchResults(
            songs   = MockCatalog.songs.filter   { it.title.lowercase().contains(q) || it.artistName.lowercase().contains(q) },
            artists = MockCatalog.artists.filter { it.name.lowercase().contains(q) },
            albums  = MockCatalog.albums.filter  { it.title.lowercase().contains(q) || it.artistName.lowercase().contains(q) },
        )
    }

    fun favoriteIds(): Flow<List<String>> = favoriteDao.favoriteIds()
    // Songs stored as bare ids; playlists as "pl:<id>"; albums as "al:<id>".
    // favoriteSongs() drops anything prefixed so it stays a clean song list.
    fun favoriteSongs(): Flow<List<Song>> = favoriteDao.favoriteIds()
        .map { ids -> MockCatalog.songsIn(ids.filterNot { it.contains(':') }) }
    fun isFavorite(id: String): Flow<Boolean> = favoriteDao.isFavorite(id)
    suspend fun toggleFavorite(id: String, currently: Boolean) {
        if (currently) favoriteDao.remove(id)
        else favoriteDao.add(FavoriteEntity(id, System.currentTimeMillis()))
    }

    fun isPlaylistFavorite(id: String): Flow<Boolean> = favoriteDao.isFavorite("pl:$id")
    suspend fun togglePlaylistFavorite(id: String, currently: Boolean) {
        val key = "pl:$id"
        if (currently) favoriteDao.remove(key)
        else favoriteDao.add(FavoriteEntity(key, System.currentTimeMillis()))
    }

    fun isAlbumFavorite(id: String): Flow<Boolean> = favoriteDao.isFavorite("al:$id")
    suspend fun toggleAlbumFavorite(id: String, currently: Boolean) {
        val key = "al:$id"
        if (currently) favoriteDao.remove(key)
        else favoriteDao.add(FavoriteEntity(key, System.currentTimeMillis()))
    }

    fun recentSongs(): Flow<List<Song>> = recentDao.recentIds().map { MockCatalog.songsIn(it) }
    suspend fun logPlayed(id: String) {
        recentDao.log(RecentlyPlayedEntity(id, System.currentTimeMillis()))
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    fun userPlaylists(): Flow<List<Playlist>> =
        playlistDao.playlists().flatMapLatest { rows ->
            if (rows.isEmpty()) kotlinx.coroutines.flow.flowOf(emptyList())
            else combine(rows.map { row ->
                playlistDao.songs(row.id).map { songIds -> row to songIds }
            }) { arr ->
                arr.map { (row, ids) ->
                    Playlist(
                        id = row.id,
                        title = row.title,
                        curator = "You",
                        trackIds = ids,
                        gradient = accentFor(row.id),
                    )
                }
            }
        }

    suspend fun createPlaylist(title: String): String {
        val id = "u-" + UUID.randomUUID().toString().take(8)
        playlistDao.create(PlaylistEntity(id, title, System.currentTimeMillis()))
        return id
    }

    suspend fun addSongToPlaylist(playlistId: String, songId: String, position: Int) {
        // Legacy path: id only. Kept for callers that don't have the full Song at hand.
        playlistDao.addSong(PlaylistSongCrossRef(playlistId, songId, position))
    }

    suspend fun addSongToPlaylist(playlistId: String, song: Song, position: Int) {
        playlistDao.addSong(
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = song.id,
                position = position,
                title = song.title,
                artistName = song.artistName,
                albumName = song.albumName,
                mediaUri = song.mediaUri.orEmpty(),
                durationMs = song.durationMs,
                gradientColor1 = song.artworkGradient.getOrNull(0)?.toArgb() ?: 0xFF7C3AED.toInt(),
                gradientColor2 = song.artworkGradient.getOrNull(1)?.toArgb() ?: 0xFF1E1B4B.toInt(),
            )
        )
    }

    /** Reconstructed songs for a playlist. Bundled tracks resolve via MockCatalog;
     *  device/imported tracks use the cached fields on the cross-ref row. */
    fun playlistSongsFlow(playlistId: String): Flow<List<Song>> =
        playlistDao.playlistItems(playlistId).map { refs ->
            refs.mapNotNull { ref ->
                MockCatalog.songById(ref.songId) ?: reconstructFromRef(ref)
            }
        }

    private fun reconstructFromRef(ref: com.aurabeat.app.data.local.PlaylistSongCrossRef): Song? {
        if (ref.title.isBlank() || ref.mediaUri.isBlank()) return null
        return Song(
            id = ref.songId,
            title = ref.title,
            artistId = "cached-${ref.artistName.hashCode()}",
            artistName = ref.artistName,
            albumId = "cached-${ref.albumName.hashCode()}",
            albumName = ref.albumName,
            durationMs = ref.durationMs,
            artworkGradient = listOf(Color(ref.gradientColor1), Color(ref.gradientColor2)),
            mediaUri = ref.mediaUri,
        )
    }

    suspend fun playlistSongCount(playlistId: String): Int =
        playlistDao.songs(playlistId).first().size

    private fun accentFor(id: String): List<Color> {
        val palettes = listOf(
            listOf(Color(0xFF7C3AED), Color(0xFF1E1B4B)),
            listOf(Color(0xFFFF7A00), Color(0xFF3B0764)),
            listOf(Color(0xFF06B6D4), Color(0xFF1E1B4B)),
            listOf(Color(0xFFEC4899), Color(0xFF3B0764)),
        )
        return palettes[(id.hashCode() and 0x7fffffff) % palettes.size]
    }
}

data class SearchResults(
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
) {
    val isEmpty get() = songs.isEmpty() && artists.isEmpty() && albums.isEmpty()
}
