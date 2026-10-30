package com.aurabeat.app.presentation.favorites

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
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aurabeat.app.data.model.Song
import com.aurabeat.app.data.repository.MusicRepository
import com.aurabeat.app.presentation.PlayerViewModel
import com.aurabeat.app.ui.components.SongRow
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraPrimaryLight
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraTextMuted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(repo: MusicRepository) : ViewModel() {
    val favorites: StateFlow<List<Song>> = repo.favoriteSongs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun FavoritesScreen(
    nav: NavHostController,
    playerVm: PlayerViewModel,
    vm: FavoritesViewModel = hiltViewModel(),
) {
    val favorites by vm.favorites.collectAsStateWithLifecycle()
    val state by playerVm.state.collectAsStateWithLifecycle()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 220.dp),
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Brush.verticalGradient(listOf(AuraPrimaryLight.copy(alpha = 0.6f), Color(0xFF09070D)))),
            ) {
                IconButton(onClick = { nav.popBackStack() }, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(
                    Modifier.align(Alignment.BottomStart).padding(AuraSpacing.md),
                ) {
                    Text("Liked Songs", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text("${favorites.size} tracks", style = MaterialTheme.typography.bodyMedium, color = AuraTextMuted)
                }
            }
        }
        if (favorites.isNotEmpty()) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(AuraSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm),
                ) {
                    Row(
                        Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Brush.linearGradient(listOf(AuraPrimaryLight, AuraPrimary)))
                            .clickable { playerVm.playQueue(favorites) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Play All", style = MaterialTheme.typography.labelLarge, color = Color.White)
                    }
                }
            }
        }
        if (favorites.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(AuraSpacing.xl), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No liked songs yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(4.dp))
                        Text("Tap the heart on Now Playing to save a track.", style = MaterialTheme.typography.bodyMedium, color = AuraTextMuted)
                    }
                }
            }
        } else {
            items(favorites, key = { it.id }) { song ->
                SongRow(
                    song = song,
                    isPlaying = state.currentSong?.id == song.id,
                    onClick = { playerVm.play(song, favorites) },
                )
            }
        }
    }
}
