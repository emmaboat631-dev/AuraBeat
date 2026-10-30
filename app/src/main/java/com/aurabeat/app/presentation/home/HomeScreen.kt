package com.aurabeat.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.aurabeat.app.data.model.Album
import com.aurabeat.app.data.model.Artist
import com.aurabeat.app.data.model.Playlist
import com.aurabeat.app.navigation.Screen
import com.aurabeat.app.presentation.PlayerViewModel
import com.aurabeat.app.ui.components.AlbumArtwork
import com.aurabeat.app.ui.components.GenreChip
import com.aurabeat.app.ui.components.SectionHeader
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraSurfaceElev
import com.aurabeat.app.ui.theme.AuraTextMuted
import com.aurabeat.app.ui.theme.AuraTextSecondary

@Composable
fun HomeScreen(
    nav: NavHostController,
    playerVm: PlayerViewModel,
    vm: HomeViewModel = hiltViewModel(),
) {
    val ui = vm.state
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 220.dp, top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.md),
    ) {
        item { HomeHeader(onSearch = { nav.navigate(Screen.Search.route) }) }
        item { GenresRow(ui.genres) { nav.navigate(Screen.Search.route) } }
        item {
            SectionHeader("New Releases", "See all") { nav.navigate(Screen.Library.route) }
            AlbumsRow(ui.albums) { album -> nav.navigate(Screen.Album.of(album.id)) }
        }
        item {
            SectionHeader("Popular Artists", "See all") { nav.navigate(Screen.Library.route) }
            ArtistsRow(ui.artists) { artist -> nav.navigate(Screen.Artist.of(artist.id)) }
        }
        item {
            SectionHeader("Made For You", "See all") { nav.navigate(Screen.Library.route) }
            PlaylistsRow(ui.playlists) { p -> nav.navigate(Screen.Playlist.of(p.id)) }
        }
        item {
            SectionHeader("Recently Played")
            Column {
                ui.recent.take(4).forEach { song ->
                    com.aurabeat.app.ui.components.SongRow(
                        song = song,
                        isPlaying = playerVm.state.value.currentSong?.id == song.id,
                        onClick = { playerVm.play(song, ui.recent) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(onSearch: () -> Unit) {
    Column(Modifier.padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Good evening", style = MaterialTheme.typography.bodyMedium, color = AuraTextSecondary)
                Text("Ready to vibe?", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AuraSurfaceElev),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.Notifications, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurface) }
        }
        Spacer(Modifier.height(AuraSpacing.md))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(AuraSurfaceElev)
                .clickable(onClick = onSearch)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Rounded.Search, contentDescription = null, tint = AuraTextMuted)
            Spacer(Modifier.width(10.dp))
            Text("Search songs, artists, albums", color = AuraTextMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun GenresRow(genres: List<com.aurabeat.app.data.model.Genre>, onGenreClick: () -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = AuraSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm),
    ) {
        items(genres) { GenreChip(it, onClick = onGenreClick) }
    }
}

@Composable
private fun AlbumsRow(albums: List<Album>, onClick: (Album) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = AuraSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm),
    ) {
        items(albums) { album ->
            val cover = androidx.compose.runtime.remember(album.id) {
                com.aurabeat.app.data.MockCatalog.songById(album.trackIds.firstOrNull().orEmpty())
            }
            Column(
                Modifier
                    .width(160.dp)
                    .clickable { onClick(album) },
            ) {
                com.aurabeat.app.ui.components.SongArtwork(cover, Modifier.size(160.dp), corner = 20.dp)
                Spacer(Modifier.height(8.dp))
                Text(album.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                Text(album.artistName, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = AuraTextSecondary)
            }
        }
    }
}

@Composable
private fun ArtistsRow(artists: List<Artist>, onClick: (Artist) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = AuraSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AuraSpacing.md),
    ) {
        items(artists) { artist ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(88.dp).clickable { onClick(artist) },
            ) {
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(artist.gradient)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(artist.name.take(1), style = MaterialTheme.typography.headlineMedium, color = Color.White)
                }
                Spacer(Modifier.height(8.dp))
                Text(artist.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun PlaylistsRow(playlists: List<Playlist>, onClick: (Playlist) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = AuraSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm),
    ) {
        items(playlists) { p ->
            val cover = androidx.compose.runtime.remember(p.id) {
                com.aurabeat.app.data.MockCatalog.songById(p.trackIds.firstOrNull().orEmpty())
            }
            Column(Modifier.width(160.dp).clickable { onClick(p) }) {
                com.aurabeat.app.ui.components.SongArtwork(cover, Modifier.size(160.dp), corner = 20.dp)
                Spacer(Modifier.height(8.dp))
                Text(p.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                Text(p.curator, style = MaterialTheme.typography.bodySmall, color = AuraTextSecondary, maxLines = 1)
            }
        }
    }
}
