package com.aurabeat.app.presentation.library

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PhoneAndroid
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
import androidx.navigation.NavHostController
import com.aurabeat.app.navigation.Screen
import com.aurabeat.app.presentation.PlayerViewModel
import com.aurabeat.app.ui.components.AlbumArtwork
import com.aurabeat.app.ui.components.SectionHeader
import com.aurabeat.app.ui.theme.AuraAccent
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraPrimaryLight
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraSurfaceElev
import com.aurabeat.app.ui.theme.AuraTextSecondary

@Composable
fun LibraryScreen(
    nav: NavHostController,
    playerVm: PlayerViewModel,
    vm: LibraryViewModel = hiltViewModel(),
) {
    val ui = vm.state
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 220.dp, top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Your Library", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                IconButton(onClick = vm::openCreateDialog) {
                    Icon(Icons.Rounded.Add, contentDescription = "Create playlist", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm),
            ) {
                QuickTile(
                    modifier = Modifier.weight(1f),
                    gradient = listOf(AuraPrimaryLight, AuraPrimary),
                    icon = Icons.Rounded.Favorite,
                    title = "Liked Songs",
                    subtitle = "${ui.favorites.size} tracks",
                    onClick = { nav.navigate(Screen.Favorites.route) },
                )
                QuickTile(
                    modifier = Modifier.weight(1f),
                    gradient = listOf(AuraAccent, Color(0xFF3B0764)),
                    icon = Icons.Rounded.History,
                    title = "Recently Played",
                    subtitle = "${ui.recent.size} tracks",
                    onClick = { /* navigate */ },
                )
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.md),
            ) {
                QuickTile(
                    modifier = Modifier.fillMaxWidth(),
                    gradient = listOf(Color(0xFF06B6D4), Color(0xFF1E1B4B)),
                    icon = Icons.Rounded.PhoneAndroid,
                    title = "Device Music",
                    subtitle = "Your phone's local library",
                    onClick = { nav.navigate(Screen.Device.route) },
                )
            }
        }
        if (ui.userPlaylists.isNotEmpty()) {
            item { SectionHeader("Your Playlists") }
            items(ui.userPlaylists, key = { it.id }) { p -> PlaylistRow(p, nav) }
        }
        item { SectionHeader("Curated") }
        items(ui.curatedPlaylists, key = { it.id }) { p -> PlaylistRow(p, nav) }
    }

    if (ui.showCreateDialog) {
        CreatePlaylistDialog(
            onDismiss = vm::dismissCreateDialog,
            onCreate = vm::createPlaylist,
        )
    }
}

@Composable
private fun PlaylistRow(p: com.aurabeat.app.data.model.Playlist, nav: androidx.navigation.NavHostController) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { nav.navigate(com.aurabeat.app.navigation.Screen.Playlist.of(p.id)) }
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val cover = androidx.compose.runtime.remember(p.id) {
            com.aurabeat.app.data.MockCatalog.songById(p.trackIds.firstOrNull().orEmpty())
        }
        com.aurabeat.app.ui.components.SongArtwork(cover, Modifier.size(56.dp), corner = 12.dp)
        Spacer(Modifier.width(AuraSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(p.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text("${p.trackIds.size} songs • ${p.curator}", style = MaterialTheme.typography.bodySmall, color = AuraTextSecondary)
        }
    }
}

@Composable
private fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Playlist", style = MaterialTheme.typography.titleLarge) },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                placeholder = { Text("Playlist name") },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AuraPrimary,
                    cursorColor = AuraPrimary,
                ),
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = { onCreate(text) }) {
                Text("Create", color = AuraPrimaryLight)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel", color = com.aurabeat.app.ui.theme.AuraTextMuted)
            }
        },
        containerColor = com.aurabeat.app.ui.theme.AuraSurfaceElev,
    )
}

@Composable
private fun QuickTile(
    modifier: Modifier,
    gradient: List<Color>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(gradient))
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = Color.White) }
        Column(Modifier.align(Alignment.BottomStart)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
        }
    }
}
