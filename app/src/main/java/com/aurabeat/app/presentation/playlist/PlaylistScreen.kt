package com.aurabeat.app.presentation.playlist

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aurabeat.app.data.repository.MusicRepository
import com.aurabeat.app.navigation.Screen
import com.aurabeat.app.presentation.PlayerViewModel
import com.aurabeat.app.ui.components.AlbumArtwork
import com.aurabeat.app.ui.components.SongRow
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraPrimaryLight
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraSurfaceElev
import com.aurabeat.app.ui.theme.AuraTextSecondary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlaylistLookupViewModel @Inject constructor(val repo: MusicRepository) : androidx.lifecycle.ViewModel()

@Composable
fun PlaylistScreen(
    id: String,
    nav: NavHostController,
    playerVm: PlayerViewModel,
    vm: PlaylistLookupViewModel = hiltViewModel(),
) {
    val playlist by remember(id) { vm.repo.playlistFlow(id) }
        .collectAsStateWithLifecycle(initialValue = null)
    // For user playlists, load reconstructed songs from Room so device/imported
    // tracks come back correctly. For catalog playlists we fall back to songsIn.
    val playlistSongs by remember(id) { vm.repo.playlistSongsFlow(id) }
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val songs = if (playlistSongs.isNotEmpty()) playlistSongs
        else playlist?.trackIds?.let { vm.repo.songsIn(it) } ?: emptyList()
    val state by playerVm.state.collectAsStateWithLifecycle()
    val isFavorite by playerVm.isPlaylistFavorite(id)
        .collectAsStateWithLifecycle(initialValue = false)
    var optionsSong by remember { mutableStateOf<com.aurabeat.app.data.model.Song?>(null) }

    // Playlist still loading (Room hasn't emitted yet) → show placeholder instead of blank screen.
    val resolved = playlist
    if (resolved == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading playlist…", style = MaterialTheme.typography.bodyMedium, color = AuraTextSecondary)
        }
        return
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 220.dp),
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(Brush.verticalGradient(resolved.gradient + Color(0xFF09070D))),
            ) {
                IconButton(onClick = { nav.popBackStack() }, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(
                    Modifier.align(Alignment.BottomStart).padding(AuraSpacing.md),
                ) {
                    com.aurabeat.app.ui.components.SongArtwork(songs.firstOrNull(), Modifier.size(150.dp), corner = 20.dp)
                    Spacer(Modifier.height(AuraSpacing.md))
                    Text(resolved.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text("${resolved.curator} • ${songs.size} songs", style = MaterialTheme.typography.bodyMedium, color = AuraTextSecondary)
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(AuraSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PillButton(
                    text = "Play All",
                    gradient = listOf(AuraPrimaryLight, AuraPrimary),
                    icon = Icons.Rounded.PlayArrow,
                    onClick = { playerVm.playQueue(songs) },
                    modifier = Modifier.weight(1f),
                )
                PillButton(
                    text = "Shuffle",
                    gradient = listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.06f)),
                    icon = Icons.Rounded.Shuffle,
                    onClick = { playerVm.playQueue(songs.shuffled()) },
                    modifier = Modifier.weight(1f),
                    textColor = Color.White,
                )
                IconButton(
                    onClick = { playerVm.togglePlaylistFavorite(id) },
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).background(AuraSurfaceElev),
                ) {
                    Icon(
                        if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                        tint = if (isFavorite) AuraPrimary else Color.White,
                    )
                }
            }
        }
        items(songs) { song ->
            SongRow(
                song = song,
                isPlaying = state.currentSong?.id == song.id,
                onClick = { playerVm.play(song, songs) },
                onLongClick = { optionsSong = song },
            )
        }
    }

    optionsSong?.let { song ->
        val isSongFavorite by playerVm.isFavorite(song.id)
            .collectAsStateWithLifecycle(initialValue = false)
        com.aurabeat.app.ui.components.SongOptionsSheet(
            song = song,
            isFavorite = isSongFavorite,
            onDismiss = { optionsSong = null },
            onPlay = { playerVm.play(song, songs) },
            onAddToQueue = { playerVm.addToQueue(song) },
            onAddToPlaylist = { /* full add-to-playlist sheet is on Now Playing; here we just close */ optionsSong = null },
            onToggleFavorite = { playerVm.toggleFavorite(song.id) },
        )
    }
}

@Composable
private fun PillButton(
    text: String,
    gradient: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = Color.White,
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(gradient))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = textColor)
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, color = textColor)
    }
}
