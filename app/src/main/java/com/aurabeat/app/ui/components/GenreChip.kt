package com.aurabeat.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aurabeat.app.data.model.Genre

@Composable
fun GenreChip(genre: Genre, selected: Boolean = false, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .size(width = 132.dp, height = 68.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(genre.gradient))
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            genre.name,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
        )
        if (selected) {
            Box(
                Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.12f))
            )
        }
    }
}
