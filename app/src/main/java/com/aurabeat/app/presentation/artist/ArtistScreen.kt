package com.aurabeat.app.presentation.artist

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aurabeat.app.navigation.Screen
import com.aurabeat.app.presentation.PlayerViewModel
import com.aurabeat.app.presentation.playlist.PlaylistLookupViewModel
import com.aurabeat.app.ui.components.AlbumArtwork
import com.aurabeat.app.ui.components.SongRow
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraPrimaryLight
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraTextMuted
import com.aurabeat.app.ui.theme.AuraTextSecondary

@Composable
fun ArtistScreen(
    id: String,
    nav: NavHostController,
    playerVm: PlayerViewModel,
    lookup: PlaylistLookupViewModel = hiltViewModel(),
) {
    val artist = remember(id) { lookup.repo.artist(id) } ?: return
    val songs = remember(id) { lookup.repo.allSongs().filter { it.artistId == id } }
    val albums = remember(id) { lookup.repo.allAlbums().filter { it.artistId == id } }
    val state by playerVm.state.collectAsStateWithLifecycle()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 220.dp),
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Brush.verticalGradient(artist.gradient + Color(0xFF09070D))),
            ) {
                IconButton(onClick = { nav.popBackStack() }, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(
                    Modifier.align(Alignment.BottomCenter).padding(AuraSpacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(artist.gradient)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            artist.name.take(1),
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White,
                        )
                    }
                    Spacer(Modifier.height(AuraSpacing.sm))
                    Text(artist.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text(artist.followers, style = MaterialTheme.typography.bodyMedium, color = AuraTextMuted)
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth().padding(AuraSpacing.md),
                horizontalArrangement = Arrangement.Center,
            ) {
                Row(
                    Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(AuraPrimaryLight, AuraPrimary)))
                        .clickable { playerVm.playQueue(songs) }
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Play", style = MaterialTheme.typography.labelLarge, color = Color.White)
                }
            }
        }
        item {
            Text(
                "Popular",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs),
            )
        }
        items(songs.take(5), key = { it.id }) { song ->
            SongRow(
                song = song,
                isPlaying = state.currentSong?.id == song.id,
                onClick = { playerVm.play(song, songs) },
            )
        }
        if (albums.isNotEmpty()) {
            item {
                Text(
                    "Albums",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = AuraSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm),
                ) {
                    items(albums, key = { it.id }) { album ->
                        Column(
                            Modifier
                                .width(150.dp)
                                .clickable { nav.navigate(Screen.Album.of(album.id)) },
                        ) {
                            val cover = remember(album.id) { lookup.repo.songsIn(album.trackIds).firstOrNull() }
                            com.aurabeat.app.ui.components.SongArtwork(cover, Modifier.size(150.dp), corner = 18.dp)
                            Spacer(Modifier.height(6.dp))
                            Text(album.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                            Text("${album.year}", style = MaterialTheme.typography.bodySmall, color = AuraTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
