package com.aurabeat.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraPrimaryLight

@Composable
fun PlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    size: Dp = 76.dp,
    isBuffering: Boolean = false,
) {
    val scale by animateFloatAsState(if (isPlaying) 1f else 0.96f, label = "playScale")
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (isBuffering) {
            CircularProgressIndicator(
                modifier = Modifier.size(size),
                color = Color.White.copy(alpha = 0.9f),
                strokeWidth = 2.dp,
            )
        }
        Box(
            modifier = Modifier
                .size(size * 0.86f)
                .scale(scale)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(AuraPrimaryLight, AuraPrimary)))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(size / 2.6f),
            )
        }
    }
}
