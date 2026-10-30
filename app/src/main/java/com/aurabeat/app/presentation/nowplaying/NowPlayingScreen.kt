package com.aurabeat.app.presentation.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aurabeat.app.navigation.Screen
import com.aurabeat.app.player.RepeatMode
import com.aurabeat.app.presentation.PlayerViewModel
import com.aurabeat.app.ui.components.AlbumArtwork
import com.aurabeat.app.ui.components.PlayButton
import com.aurabeat.app.ui.components.Waveform
import com.aurabeat.app.ui.components.formatDuration
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraTextMuted
import com.aurabeat.app.ui.theme.AuraTextSecondary

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(nav: NavHostController, playerVm: PlayerViewModel) {
    val state by playerVm.state.collectAsStateWithLifecycle()
    val song = state.currentSong
    val haptics = LocalHapticFeedback.current
    fun buzz() = haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    val favoriteFlow = remember(song?.id) {
        song?.id?.let { playerVm.isFavorite(it) } ?: kotlinx.coroutines.flow.flowOf(false)
    }
    val favorite by favoriteFlow.collectAsStateWithLifecycle(initialValue = false)
    var showAddSheet by remember { mutableStateOf(false) }
    val userPlaylists by playerVm.userPlaylists()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val gradient = song?.artworkGradient ?: listOf(Color(0xFF7C3AED), Color(0xFF09070D))

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(gradient.first().copy(alpha = 0.35f), Color(0xFF09070D), Color(0xFF09070D))
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = AuraSpacing.md)
                .padding(top = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.Rounded.ExpandMore, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Now Playing", style = MaterialTheme.typography.labelLarge, color = AuraTextSecondary)
                    Text(song?.albumName.orEmpty(), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = { nav.navigate(Screen.Queue.route) }) {
                    Icon(Icons.Rounded.QueueMusic, contentDescription = "Queue", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(Modifier.height(AuraSpacing.lg))

            com.aurabeat.app.ui.components.SongArtwork(
                song = song,
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .aspectRatio(1f),
                corner = 32.dp,
            )

            Spacer(Modifier.height(AuraSpacing.xl))

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(song?.title ?: "Nothing playing", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text(song?.artistName.orEmpty(), style = MaterialTheme.typography.bodyLarge, color = AuraTextSecondary)
                }
                IconButton(onClick = { song?.id?.let(playerVm::toggleFavorite) }) {
                    Icon(
                        if (favorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (favorite) "Unfavorite" else "Favorite",
                        tint = if (favorite) AuraPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton(onClick = { if (song != null) showAddSheet = true }) {
                    Icon(
                        Icons.Rounded.PlaylistAdd,
                        contentDescription = "Add to playlist",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(Modifier.height(AuraSpacing.lg))

            state.error?.let { message ->
                androidx.compose.foundation.layout.Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = AuraSpacing.sm)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(Color(0x33FF7A00))
                        .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs),
                ) {
                    Text(
                        text = "Playback error: $message",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFFB673),
                    )
                }
            }

            Waveform(
                progress = state.progress,
                isPlaying = state.isPlaying,
                onSeek = playerVm::seekFraction,
                height = 80.dp,
            )

            Spacer(Modifier.height(AuraSpacing.xs))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDuration(state.positionMs), style = MaterialTheme.typography.labelMedium, color = AuraTextMuted)
                Text(formatDuration(state.durationMs.takeIf { it > 0 } ?: (song?.durationMs ?: 0)),
                    style = MaterialTheme.typography.labelMedium, color = AuraTextMuted)
            }

            Spacer(Modifier.height(AuraSpacing.lg))

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                IconButton(onClick = { buzz(); playerVm.toggleShuffle() }) {
                    Icon(
                        Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (state.shuffleEnabled) AuraPrimary else AuraTextSecondary,
                    )
                }
                IconButton(onClick = { buzz(); playerVm.previous() }) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(36.dp))
                }
                PlayButton(
                    isPlaying = state.isPlaying,
                    onClick = { buzz(); playerVm.togglePlay() },
                    isBuffering = state.isBuffering,
                )
                IconButton(onClick = { buzz(); playerVm.next() }) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { buzz(); playerVm.cycleRepeat() }) {
                    Icon(
                        if (state.repeatMode == RepeatMode.ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = "Repeat",
                        tint = if (state.repeatMode != RepeatMode.OFF) AuraPrimary else AuraTextSecondary,
                    )
                }
            }

            Spacer(Modifier.height(AuraSpacing.lg))

            androidx.compose.material3.TextButton(onClick = { nav.navigate(Screen.Lyrics.route) }) {
                Icon(Icons.Rounded.Lyrics, contentDescription = null, tint = AuraTextSecondary)
                Spacer(Modifier.width(6.dp))
                Text("Lyrics", style = MaterialTheme.typography.labelLarge, color = AuraTextSecondary)
            }
        }
    }

    val sheetSong = song
    if (showAddSheet && sheetSong != null) {
        AddToPlaylistSheet(
            playlists = userPlaylists,
            onDismiss = { showAddSheet = false },
            onPick = { playlistId ->
                playerVm.addSongToPlaylist(playlistId, sheetSong)
                showAddSheet = false
            },
            onCreateNew = { title ->
                playerVm.createPlaylistAndAdd(title, sheetSong)
                showAddSheet = false
            },
        )
    }
}

@androidx.compose.runtime.Composable
@androidx.compose.material3.ExperimentalMaterial3Api
private fun AddToPlaylistSheet(
    playlists: List<com.aurabeat.app.data.model.Playlist>,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit,
    onCreateNew: (String) -> Unit,
) {
    var showCreateInline by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    androidx.compose.material3.ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = com.aurabeat.app.ui.theme.AuraSurfaceElev,
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = AuraSpacing.xl),
        ) {
            Text(
                "Add to playlist",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
            )

            if (playlists.isEmpty() && !showCreateInline) {
                Text(
                    "You don't have any playlists yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AuraTextMuted,
                    modifier = Modifier.padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs),
                )
            }

            playlists.forEach { p ->
                androidx.compose.foundation.layout.Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(p.id) }
                        .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    com.aurabeat.app.ui.components.AlbumArtwork(p.gradient, Modifier.size(44.dp), corner = 10.dp)
                    Spacer(Modifier.width(AuraSpacing.md))
                    androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                        Text(p.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("${p.trackIds.size} songs", style = MaterialTheme.typography.bodySmall, color = AuraTextMuted)
                    }
                }
            }

            androidx.compose.material3.HorizontalDivider(color = com.aurabeat.app.ui.theme.AuraDivider)

            if (showCreateInline) {
                androidx.compose.material3.OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true,
                    placeholder = { Text("Playlist name") },
                    modifier = Modifier.fillMaxWidth().padding(AuraSpacing.md),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AuraPrimary, cursorColor = AuraPrimary,
                    ),
                )
                androidx.compose.foundation.layout.Row(
                    Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.md),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                ) {
                    androidx.compose.material3.TextButton(onClick = onDismiss) {
                        Text("Cancel", color = AuraTextMuted)
                    }
                    androidx.compose.material3.TextButton(
                        onClick = { if (newName.isNotBlank()) onCreateNew(newName.trim()) }
                    ) {
                        Text("Create & Add", color = com.aurabeat.app.ui.theme.AuraPrimaryLight)
                    }
                }
            } else {
                androidx.compose.foundation.layout.Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { showCreateInline = true }
                        .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.md),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null,
                        tint = com.aurabeat.app.ui.theme.AuraPrimaryLight,
                    )
                    Spacer(Modifier.width(AuraSpacing.sm))
                    Text("New playlist", style = MaterialTheme.typography.titleMedium, color = com.aurabeat.app.ui.theme.AuraPrimaryLight)
                }
            }
        }
    }
}
