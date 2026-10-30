package com.aurabeat.app.presentation.spotify

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.aurabeat.app.spotify.SpotifyPlaybackController
import com.aurabeat.app.spotify.SpotifyPlaybackSnapshot
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraTextSecondary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val SpotifyGreen = Color(0xFF1DB954)

@HiltViewModel
class SpotifyMiniBarViewModel @Inject constructor(
    private val controller: SpotifyPlaybackController,
) : ViewModel() {
    val state: StateFlow<SpotifyPlaybackSnapshot> =
        controller.state.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SpotifyPlaybackSnapshot())

    fun togglePlay() { viewModelScope.launch { controller.togglePlay() } }
    fun skipNext()   { viewModelScope.launch { controller.skipNext() } }
}

@Composable
fun SpotifyMiniBar(vm: SpotifyMiniBarViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    AnimatedVisibility(
        visible = state.active && state.trackName != null,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AuraSpacing.sm)
                .padding(bottom = AuraSpacing.xs)
                .clip(RoundedCornerShape(16.dp))
                .background(SpotifyGreen)
                .padding(horizontal = AuraSpacing.sm, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.layout.Box(
                Modifier.size(32.dp).clip(CircleShape).background(Color(0x33000000)),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp)) }
            Spacer(Modifier.width(AuraSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text("Playing on Spotify", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                Text(
                    state.trackName.orEmpty() + (state.artistName?.let { " — $it" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium, color = Color.White,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = vm::togglePlay) {
                Icon(
                    if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = Color.White,
                )
            }
            IconButton(onClick = vm::skipNext) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = Color.White)
            }
        }
    }
}
