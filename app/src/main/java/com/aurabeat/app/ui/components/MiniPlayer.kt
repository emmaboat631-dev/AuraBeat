package com.aurabeat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aurabeat.app.player.PlayerState
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraSurfaceElev
import com.aurabeat.app.ui.theme.AuraTextSecondary

@Composable
fun MiniPlayer(
    state: PlayerState,
    onTogglePlay: () -> Unit,
    onClick: () -> Unit,
    onNext: () -> Unit = {},
    onPrevious: () -> Unit = {},
) {
    val song = state.currentSong ?: return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AuraSpacing.sm)
            .padding(bottom = AuraSpacing.xs)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AuraSurfaceElev)
                .clickable(onClick = onClick)
                .padding(AuraSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SongArtwork(
                song = song,
                modifier = Modifier.size(48.dp),
                corner = 10.dp,
            )
            Spacer(Modifier.width(AuraSpacing.sm))
            Column(Modifier.weight(1f)) {
                Text(song.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                Text(song.artistName, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = AuraTextSecondary)
            }
            IconButton(onClick = onPrevious) {
                Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onTogglePlay) {
                Icon(
                    if (state.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AuraSpacing.md)
                .height(2.dp),
            color = AuraPrimary,
            trackColor = Color.White.copy(alpha = 0.08f),
        )
    }
}
