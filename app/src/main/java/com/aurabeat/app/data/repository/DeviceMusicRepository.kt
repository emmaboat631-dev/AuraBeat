package com.aurabeat.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.compose.ui.graphics.Color
import androidx.documentfile.provider.DocumentFile
import com.aurabeat.app.data.local.ImportedSongDao
import com.aurabeat.app.data.local.ImportedSongEntity
import com.aurabeat.app.data.model.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceMusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val importedDao: ImportedSongDao,
) {
    private val gradientPool = listOf(
        listOf(Color(0xFF7C3AED), Color(0xFF1E1B4B)),
        listOf(Color(0xFFFF7A00), Color(0xFF3B0764)),
        listOf(Color(0xFFA855F7), Color(0xFF0F172A)),
        listOf(Color(0xFF06B6D4), Color(0xFF1E1B4B)),
        listOf(Color(0xFFEC4899), Color(0xFF3B0764)),
        listOf(Color(0xFFF59E0B), Color(0xFF3B0764)),
    )

    private fun gradientFor(seed: String): List<Color> =
        gradientPool[(seed.hashCode() and 0x7fffffff) % gradientPool.size]

    /** Scans MediaStore.Audio for every music track the OS knows about. */
    fun scanMediaStore(): Flow<List<Song>> = flow {
        emit(withContext(Dispatchers.IO) { queryMediaStore() })
    }

    private fun queryMediaStore(): List<Song> {
        val songs = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} > 20000"
        val sort = "${MediaStore.Audio.Media.TITLE} ASC"
        val cursor: Cursor? = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, null, sort,
        )
        cursor?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                val title = c.getString(titleCol) ?: "Unknown"
                val artist = c.getString(artistCol) ?: "Unknown Artist"
                val album = c.getString(albumCol) ?: "Unknown Album"
                val duration = c.getLong(durCol)
                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                songs += Song(
                    id = "ms-$id",
                    title = title,
                    artistId = "ms-artist-${artist.hashCode()}",
                    artistName = artist,
                    albumId = "ms-album-${album.hashCode()}",
                    albumName = album,
                    durationMs = duration,
                    artworkGradient = gradientFor("$id-$artist"),
                    mediaUri = uri.toString(),
                    genre = "Device",
                )
            }
        }
        return songs
    }

    /** SAF-imported songs live in Room; observed as [Song] for the UI. */
    fun importedSongs(): Flow<List<Song>> = importedDao.all().map { rows ->
        rows.map { row ->
            Song(
                id = "imp-${row.uri.hashCode()}",
                title = row.title,
                artistId = "imp-artist-${row.artistName.hashCode()}",
                artistName = row.artistName,
                albumId = "imp-album-${row.albumName.hashCode()}",
                albumName = row.albumName,
                durationMs = row.durationMs,
                artworkGradient = gradientFor(row.uri),
                mediaUri = row.uri,
                genre = "Imported",
            )
        }
    }

    /** Union view: MediaStore + imported. Both are refreshed lazily. */
    fun deviceLibrary(): Flow<List<Song>> = combine(scanMediaStore(), importedSongs()) { ms, imp ->
        (ms + imp).distinctBy { it.mediaUri }
    }

    fun importedCount(): Flow<Int> = importedDao.count()

    /**
     * Persists a URI granted via SAF (`ACTION_OPEN_DOCUMENT`). Takes a
     * durable permission so it survives app restarts, then extracts basic
     * metadata via [MediaMetadataRetriever].
     */
    suspend fun importFile(uri: Uri) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (_: SecurityException) { /* URI was granted with one-time perm */ }

        val meta = extractMetadata(uri) ?: return@withContext
        importedDao.add(
            ImportedSongEntity(
                uri = uri.toString(),
                title = meta.title,
                artistName = meta.artist,
                albumName = meta.album,
                durationMs = meta.duration,
                addedAt = System.currentTimeMillis(),
            )
        )
    }

    /** Walks a SAF tree URI and imports every audio child. */
    suspend fun importFolder(treeUri: Uri) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.takePersistableUriPermission(
                treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (_: SecurityException) {}
        val tree = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext
        walkAudio(tree).forEach { importFile(it) }
    }

    private fun walkAudio(root: DocumentFile): List<Uri> {
        val out = mutableListOf<Uri>()
        val stack = ArrayDeque<DocumentFile>().apply { add(root) }
        while (stack.isNotEmpty()) {
            val node = stack.removeFirst()
            if (node.isDirectory) {
                node.listFiles().forEach { stack.add(it) }
            } else if ((node.type ?: "").startsWith("audio/")) {
                out += node.uri
            }
        }
        return out
    }

    suspend fun removeImport(uri: String) = importedDao.remove(uri)

    private data class Meta(val title: String, val artist: String, val album: String, val duration: Long)

    private fun extractMetadata(uri: Uri): Meta? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: displayName(uri) ?: "Unknown"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "Unknown Album"
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            Meta(title, artist, album, duration)
        } catch (_: Throwable) {
            null
        } finally {
            try { retriever.release() } catch (_: Throwable) {}
        }
    }

    private fun displayName(uri: Uri): String? {
        val c = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null) ?: return null
        return c.use {
            if (it.moveToFirst()) it.getString(0)?.substringBeforeLast('.') else null
        }
    }
}
