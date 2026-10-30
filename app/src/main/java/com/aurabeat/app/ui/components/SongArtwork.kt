package com.aurabeat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.aurabeat.app.data.model.Song
import com.aurabeat.app.player.Artwork
import com.aurabeat.app.ui.theme.AuraRadius
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Renders a song's cover art. Uses the file's own embedded artwork when
 * present (extracted off the main thread, cached forever), and falls back
 * to the gradient tile if the file carries none — so the layout never
 * shifts.
 */
@Composable
fun SongArtwork(
    song: Song?,
    modifier: Modifier = Modifier,
    corner: Dp = AuraRadius.md,
) {
    if (song == null) {
        AlbumArtwork(
            gradient = listOf(Color(0xFF7C3AED), Color(0xFF1E1B4B)),
            modifier = modifier,
            corner = corner,
        )
        return
    }
    val context = LocalContext.current
    var bytes by remember(song.id) { mutableStateOf<ByteArray?>(null) }
    LaunchedEffect(song.id) {
        bytes = withContext(Dispatchers.IO) { Artwork.forSong(context, song) }
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(Brush.linearGradient(song.artworkGradient)),
    ) {
        val current = bytes
        if (current != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(current)
                    .crossfade(true)
                    .build(),
                contentDescription = "Cover art for ${song.title}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
