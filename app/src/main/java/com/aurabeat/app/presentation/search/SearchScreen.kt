package com.aurabeat.app.presentation.search

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aurabeat.app.navigation.Screen
import com.aurabeat.app.presentation.PlayerViewModel
import com.aurabeat.app.ui.components.AlbumArtwork
import com.aurabeat.app.ui.components.SectionHeader
import com.aurabeat.app.ui.components.SongRow
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraSurfaceElev
import com.aurabeat.app.ui.theme.AuraTextMuted

@Composable
fun SearchScreen(
    nav: NavHostController,
    playerVm: PlayerViewModel,
    vm: SearchViewModel = hiltViewModel(),
) {
    val ui = vm.state
    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        Text(
            "Search",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
        )
        TextField(
            value = ui.query,
            onValueChange = vm::onQueryChanged,
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = AuraTextMuted) },
            placeholder = { Text("Songs, artists, albums", color = AuraTextMuted) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = AuraSurfaceElev,
                unfocusedContainerColor = AuraSurfaceElev,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = AuraPrimary,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AuraSpacing.md),
        )
        Spacer(Modifier.height(AuraSpacing.sm))
        LazyRow(
            contentPadding = PaddingValues(horizontal = AuraSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AuraSpacing.xs),
        ) {
            items(SearchFilter.entries.toList()) { filter ->
                FilterChip(
                    selected = ui.filter == filter,
                    onClick = { vm.onFilterChanged(filter) },
                    label = { Text(filter.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AuraPrimary,
                        selectedLabelColor = Color.White,
                        containerColor = AuraSurfaceElev,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }
        Spacer(Modifier.height(AuraSpacing.sm))
        val nothingLocal = ui.results.isEmpty
        val nothingSpotify = ui.spotifyResults.isEmpty()
        if (ui.query.isBlank()) {
            EmptyState("Discover something new", "Search your library and Spotify.")
        } else if (nothingLocal && nothingSpotify && !ui.spotifyLoading) {
            EmptyState("No results", "Try a different search term.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 220.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (ui.filter in listOf(SearchFilter.ALL, SearchFilter.SONGS) && ui.results.songs.isNotEmpty()) {
                    item { SectionHeader("Songs") }
                    items(ui.results.songs) { song ->
                        SongRow(
                            song = song,
                            isPlaying = playerVm.state.value.currentSong?.id == song.id,
                            onClick = { playerVm.play(song, ui.results.songs) },
                        )
                    }
                }
                if (ui.filter in listOf(SearchFilter.ALL, SearchFilter.ARTISTS) && ui.results.artists.isNotEmpty()) {
                    item { SectionHeader("Artists") }
                    items(ui.results.artists) { artist ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AlbumArtwork(artist.gradient, Modifier.size(52.dp), corner = 26.dp)
                            Spacer(Modifier.padding(end = AuraSpacing.md))
                            Column {
                                Text(artist.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(artist.followers, style = MaterialTheme.typography.bodySmall, color = AuraTextMuted)
                            }
                        }
                    }
                }
                if (ui.filter in listOf(SearchFilter.ALL, SearchFilter.ALBUMS) && ui.results.albums.isNotEmpty()) {
                    item { SectionHeader("Albums") }
                    items(ui.results.albums) { album ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AlbumArtwork(album.gradient, Modifier.size(52.dp), corner = 12.dp)
                            Spacer(Modifier.padding(end = AuraSpacing.md))
                            Column(Modifier.weight(1f)) {
                                Text(album.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("${album.artistName} • ${album.year}", style = MaterialTheme.typography.bodySmall, color = AuraTextMuted)
                            }
                        }
                    }
                }
                if (ui.spotifyConnected && ui.filter in listOf(SearchFilter.ALL, SearchFilter.SONGS)) {
                    item {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("From Spotify", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.padding(end = AuraSpacing.xs))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(Color(0xFF1DB954))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("beta", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                    when {
                        ui.spotifyResults.isNotEmpty() -> {
                            items(ui.spotifyResults, key = { it.id }) { track ->
                                SpotifyTrackRow(track = track, onPlay = { vm.playOnSpotify(track, playerVm) })
                            }
                        }
                        ui.spotifyLoading -> item {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF1DB954),
                                )
                                Spacer(Modifier.padding(end = AuraSpacing.sm))
                                Text("Searching Spotify…", style = MaterialTheme.typography.labelMedium, color = AuraTextMuted)
                            }
                        }
                        ui.spotifyError != null -> item {
                            SpotifyFallbackRow(
                                reason = ui.spotifyError!!,
                                query = ui.query,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpotifyFallbackRow(reason: String, query: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x33FF7A00))
            .padding(AuraSpacing.md),
    ) {
        Text(
            reason,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFFFB673),
        )
        Spacer(Modifier.padding(top = 6.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Color(0xFF1DB954))
                .clickable {
                    val q = java.net.URLEncoder.encode(query, Charsets.UTF_8.name())
                    val app = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("spotify:search:$q"),
                    ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    val web = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://open.spotify.com/search/$q"),
                    ).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    val chosen = if (context.packageManager.resolveActivity(app, 0) != null) app else web
                    context.startActivity(chosen)
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Rounded.PlayArrow,
                contentDescription = null, tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.padding(end = 6.dp))
            Text("Search “$query” in Spotify", style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}

@Composable
private fun SpotifyTrackRow(track: com.aurabeat.app.spotify.SpotifyTrack, onPlay: () -> Unit) {
    val artwork = track.album?.images?.firstOrNull()?.url
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF191414)),
        ) {
            if (artwork != null) {
                coil.compose.AsyncImage(
                    model = artwork,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                )
            }
        }
        Spacer(Modifier.padding(end = AuraSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(track.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(
                track.artists.joinToString { it.name },
                style = MaterialTheme.typography.bodySmall, color = AuraTextMuted, maxLines = 1,
            )
        }
        Box(
            Modifier
                .size(28.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(Color(0xFF1DB954)),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Rounded.PlayArrow,
                contentDescription = "Open in Spotify",
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun EmptyState(title: String, message: String) {
    Box(Modifier.fillMaxSize().padding(AuraSpacing.xl), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(6.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = AuraTextMuted)
        }
    }
}
