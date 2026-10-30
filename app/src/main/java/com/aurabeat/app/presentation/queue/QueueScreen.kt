package com.aurabeat.app.presentation.queue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aurabeat.app.presentation.PlayerViewModel
import com.aurabeat.app.ui.components.AlbumArtwork
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraSurfaceElev
import com.aurabeat.app.ui.theme.AuraTextMuted
import com.aurabeat.app.ui.theme.AuraTextSecondary
import com.aurabeat.app.ui.components.formatDuration

@Composable
fun QueueScreen(nav: NavHostController, playerVm: PlayerViewModel) {
    val state by playerVm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(AuraSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { nav.popBackStack() }) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }
            Text("Queue", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 4.dp).weight(1f))
        }

        val queue = state.queue
        val currentIndex = state.queueIndex

        if (queue.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                Modifier.fillMaxSize().padding(AuraSpacing.xl),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your queue is empty", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.size(6.dp))
                    Text("Play a song from Home or a playlist to fill it.",
                        style = MaterialTheme.typography.bodyMedium, color = AuraTextMuted)
                }
            }
            return@Column
        }

        LazyColumn(
            contentPadding = PaddingValues(bottom = 220.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (state.currentSong != null && currentIndex >= 0) {
                item {
                    Text("Now Playing", style = MaterialTheme.typography.labelLarge, color = AuraTextSecondary,
                        modifier = Modifier.padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs))
                }
                item {
                    QueueRow(
                        song = state.currentSong!!,
                        duration = formatDuration(state.currentSong!!.durationMs),
                        highlight = true,
                        onClick = {},
                        onRemove = null,
                    )
                }
            }

            if (currentIndex + 1 < queue.size) {
                item {
                    Text("Up Next", style = MaterialTheme.typography.labelLarge, color = AuraTextSecondary,
                        modifier = Modifier.padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs))
                }
                itemsIndexed(queue.subList(currentIndex + 1, queue.size)) { relIdx, song ->
                    val absIdx = currentIndex + 1 + relIdx
                    val canMoveUp = absIdx > currentIndex + 1
                    val canMoveDown = absIdx < queue.size - 1
                    QueueRow(
                        song = song,
                        duration = formatDuration(song.durationMs),
                        highlight = false,
                        onClick = { playerVm.playAtQueueIndex(absIdx) },
                        onRemove = { playerVm.removeFromQueue(absIdx) },
                        onMoveUp = if (canMoveUp) { { playerVm.moveInQueue(absIdx, absIdx - 1) } } else null,
                        onMoveDown = if (canMoveDown) { { playerVm.moveInQueue(absIdx, absIdx + 1) } } else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    song: com.aurabeat.app.data.model.Song,
    duration: String,
    highlight: Boolean,
    onClick: () -> Unit,
    onRemove: (() -> Unit)?,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        com.aurabeat.app.ui.components.SongArtwork(song, Modifier.size(48.dp), corner = 10.dp)
        Spacer(Modifier.size(AuraSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(song.title, style = MaterialTheme.typography.titleSmall,
                color = if (highlight) AuraPrimary else MaterialTheme.colorScheme.onSurface)
            Text(song.artistName, style = MaterialTheme.typography.bodySmall, color = AuraTextSecondary)
        }
        Text(duration, style = MaterialTheme.typography.labelSmall, color = AuraTextMuted)
        if (onRemove != null) {
            Column {
                IconButton(onClick = onMoveUp ?: {}, enabled = onMoveUp != null, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Rounded.ArrowDropUp, contentDescription = "Move up",
                        tint = if (onMoveUp != null) MaterialTheme.colorScheme.onSurface else AuraTextMuted.copy(alpha = 0.3f))
                }
                IconButton(onClick = onMoveDown ?: {}, enabled = onMoveDown != null, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = "Move down",
                        tint = if (onMoveDown != null) MaterialTheme.colorScheme.onSurface else AuraTextMuted.copy(alpha = 0.3f))
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = AuraTextMuted)
            }
        } else {
            Spacer(Modifier.size(AuraSpacing.md))
            Icon(Icons.Rounded.DragHandle, contentDescription = null, tint = AuraTextMuted)
        }
    }
}
