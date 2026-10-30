package com.aurabeat.app.presentation.album

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aurabeat.app.presentation.PlayerViewModel
import com.aurabeat.app.presentation.playlist.PlaylistLookupViewModel
import com.aurabeat.app.ui.components.AlbumArtwork
import com.aurabeat.app.ui.components.SongRow
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraPrimaryLight
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraTextSecondary

@Composable
fun AlbumScreen(
    id: String,
    nav: NavHostController,
    playerVm: PlayerViewModel,
    lookup: PlaylistLookupViewModel = hiltViewModel(),
) {
    val album = remember(id) { lookup.repo.album(id) } ?: return
    val songs = remember(id) { lookup.repo.songsIn(album.trackIds) }
    val state by playerVm.state.collectAsStateWithLifecycle()
    val isFavorite by playerVm.isAlbumFavorite(id)
        .collectAsStateWithLifecycle(initialValue = false)

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 220.dp),
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .background(Brush.verticalGradient(album.gradient + Color(0xFF09070D))),
            ) {
                IconButton(onClick = { nav.popBackStack() }, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                IconButton(
                    onClick = { playerVm.toggleAlbumFavorite(id) },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                ) {
                    Icon(
                        if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isFavorite) "Unfavorite album" else "Favorite album",
                        tint = if (isFavorite) AuraPrimary else Color.White,
                    )
                }
                Column(Modifier.align(Alignment.BottomStart).padding(AuraSpacing.md)) {
                    com.aurabeat.app.ui.components.SongArtwork(songs.firstOrNull(), Modifier.size(170.dp), corner = 22.dp)
                    Spacer(Modifier.height(AuraSpacing.md))
                    Text(album.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text("${album.artistName} • ${album.year} • ${songs.size} songs",
                        style = MaterialTheme.typography.bodyMedium, color = AuraTextSecondary)
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(AuraSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm),
            ) {
                Row(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(AuraPrimaryLight, AuraPrimary)))
                        .clickable { playerVm.playQueue(songs) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Play", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
                Row(
                    Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { playerVm.playQueue(songs.shuffled()) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Rounded.Shuffle, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Shuffle", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
        }
        items(songs) { song ->
            SongRow(
                song = song,
                isPlaying = state.currentSong?.id == song.id,
                showArtwork = false,
                onClick = { playerVm.play(song, songs) },
            )
        }
    }
}
