package com.aurabeat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.aurabeat.app.data.model.Song
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraPrimaryLight
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraSurfaceElev
import com.aurabeat.app.ui.theme.AuraTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongOptionsSheet(
    song: Song,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AuraSurfaceElev,
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = AuraSpacing.xl)) {
            Row(
                Modifier.fillMaxWidth().padding(AuraSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SongArtwork(song, Modifier.size(52.dp), corner = 10.dp)
                Spacer(Modifier.width(AuraSpacing.md))
                Column(Modifier.weight(1f)) {
                    Text(song.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                    Text(song.artistName, style = MaterialTheme.typography.bodySmall, color = AuraTextSecondary, maxLines = 1)
                }
            }
            OptionRow(
                icon = Icons.Rounded.PlayArrow,
                label = "Play now",
                onClick = { onPlay(); onDismiss() },
            )
            OptionRow(
                icon = Icons.Rounded.QueueMusic,
                label = "Add to queue",
                onClick = { onAddToQueue(); onDismiss() },
            )
            OptionRow(
                icon = Icons.Rounded.PlaylistAdd,
                label = "Add to playlist",
                onClick = { onAddToPlaylist() /* dismiss handled by outer sheet swap */ },
            )
            OptionRow(
                icon = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                label = if (isFavorite) "Remove from Liked Songs" else "Add to Liked Songs",
                onClick = { onToggleFavorite(); onDismiss() },
                accentTint = isFavorite,
            )
        }
    }
}

@Composable
private fun OptionRow(icon: ImageVector, label: String, onClick: () -> Unit, accentTint: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(AuraSurfaceElev),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = if (accentTint) AuraPrimary else AuraPrimaryLight)
        }
        Spacer(Modifier.width(AuraSpacing.md))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
