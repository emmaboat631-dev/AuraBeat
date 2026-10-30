package com.aurabeat.app.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.aurabeat.app.data.model.Song
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * Provides cover art bytes for a song. Tries the file's own embedded
 * artwork first (ID3v2 APIC / MP4 covr), and falls back to a rendered
 * gradient tile when the file carries no artwork. Both paths return
 * ready-to-decode PNG/JPEG bytes; both are cached per song id.
 */
object Artwork {
    private const val SIZE = 512
    private val cache = ConcurrentHashMap<String, ByteArray>()

    /** Returns the artwork bytes, extracting from the file when possible. */
    fun forSong(context: Context, song: Song): ByteArray =
        cache.getOrPut(song.id) {
            extractEmbedded(context, song.mediaUri) ?: renderGradient(song.artworkGradient)
        }

    /** True if the returned bytes came from the file (not a fallback). */
    fun hasEmbeddedArt(context: Context, song: Song): Boolean {
        val bytes = forSong(context, song)
        // Extracted image data won't start with the PNG magic we write.
        return bytes.size < 2 ||
            !(bytes[0] == 0x89.toByte() && bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte()) ||
            bytes.size > 200_000 // gradient PNGs are small; embedded covers are usually much larger
    }

    private fun extractEmbedded(context: Context, uri: String?): ByteArray? {
        if (uri.isNullOrBlank()) return null
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(uri))
            retriever.embeddedPicture
        } catch (_: Throwable) {
            null
        } finally {
            try { retriever.release() } catch (_: Throwable) {}
        }
    }

    private fun renderGradient(colors: List<Color>): ByteArray {
        val safe = colors.ifEmpty { listOf(Color(0xFF7C3AED), Color(0xFF1E1B4B)) }
        val argb = safe.map { it.toArgb() }.toIntArray()
        val bmp = Bitmap.createBitmap(SIZE, SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, SIZE.toFloat(), SIZE.toFloat(),
                argb, null, Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, SIZE.toFloat(), SIZE.toFloat(), paint)
        val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = android.graphics.RadialGradient(
                SIZE * 0.3f, SIZE * 0.3f, SIZE * 0.7f,
                intArrayOf(0x33FFFFFF, 0x00000000),
                null, Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(0f, 0f, SIZE.toFloat(), SIZE.toFloat(), highlight)
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
        bmp.recycle()
        return out.toByteArray()
    }
}
