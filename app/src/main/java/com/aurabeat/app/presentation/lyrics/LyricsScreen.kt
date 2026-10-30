package com.aurabeat.app.presentation.lyrics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aurabeat.app.data.MockCatalog
import com.aurabeat.app.presentation.PlayerViewModel
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraTextSecondary

@Composable
fun LyricsScreen(nav: NavHostController, playerVm: PlayerViewModel) {
    val state by playerVm.state.collectAsStateWithLifecycle()
    val song = state.currentSong
    val gradient = song?.artworkGradient ?: listOf(Color(0xFF7C3AED), Color(0xFF09070D))

    Column(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(gradient.first().copy(alpha = 0.3f), Color(0xFF09070D))))
            .padding(horizontal = AuraSpacing.md),
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("Lyrics", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Spacer(Modifier.size(48.dp))
        }

        Row(Modifier.fillMaxWidth().padding(vertical = AuraSpacing.md), verticalAlignment = Alignment.CenterVertically) {
            com.aurabeat.app.ui.components.SongArtwork(song, Modifier.size(56.dp), corner = 12.dp)
            Spacer(Modifier.width(AuraSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(song?.title ?: "—", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(song?.artistName.orEmpty(), style = MaterialTheme.typography.bodySmall, color = AuraTextSecondary)
            }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            MockCatalog.demoLyrics.split("\n").forEach { line ->
                Text(
                    text = line.ifBlank { " " },
                    style = MaterialTheme.typography.titleMedium,
                    color = if (line.isBlank()) Color.Transparent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(120.dp))
        }

        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier.fillMaxWidth().height(3.dp),
            color = AuraPrimary,
            trackColor = Color.White.copy(alpha = 0.1f),
        )

        Row(
            Modifier.fillMaxWidth().padding(vertical = AuraSpacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = playerVm::previous) { Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(32.dp)) }
            IconButton(onClick = playerVm::togglePlay) {
                Icon(if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = null, tint = AuraPrimary, modifier = Modifier.size(44.dp))
            }
            IconButton(onClick = playerVm::next) { Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(32.dp)) }
        }
    }
}
