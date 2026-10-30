package com.aurabeat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aurabeat.app.ui.theme.AuraRadius

@Composable
fun AlbumArtwork(
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    corner: Dp = AuraRadius.md,
    iconSizeFactor: Float = 0.35f,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(Brush.linearGradient(gradient.ifEmpty { listOf(Color(0xFF7C3AED), Color(0xFF1E1B4B)) })),
        contentAlignment = Alignment.Center,
    ) {
        // subtle inner light
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.08f), Color.Transparent),
                    )
                )
        )
        Icon(
            imageVector = Icons.Rounded.MusicNote,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(80.dp),
        )
    }
}
