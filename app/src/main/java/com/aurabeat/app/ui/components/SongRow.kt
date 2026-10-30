package com.aurabeat.app.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aurabeat.app.data.model.Song
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraTextMuted
import com.aurabeat.app.ui.theme.AuraTextSecondary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongRow(
    song: Song,
    isPlaying: Boolean = false,
    showArtwork: Boolean = true,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onMore: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showArtwork) {
            SongArtwork(
                song = song,
                modifier = Modifier.size(52.dp),
                corner = 12.dp,
            )
            Spacer(Modifier.width(AuraSpacing.md))
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isPlaying) AuraPrimary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = song.artistName,
                style = MaterialTheme.typography.bodyMedium,
                color = AuraTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(AuraSpacing.sm))
        Text(
            text = formatDuration(song.durationMs),
            style = MaterialTheme.typography.labelMedium,
            color = AuraTextMuted,
        )
        if (isPlaying) {
            Spacer(Modifier.width(AuraSpacing.xs))
            Icon(Icons.Rounded.Equalizer, contentDescription = "Playing", tint = AuraPrimary, modifier = Modifier.size(18.dp))
        }
        if (onMore != null) {
            IconButton(onClick = onMore) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = AuraTextMuted)
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val m = total / 60
    val s = total % 60
    return "%d:%02d".format(m, s)
}
