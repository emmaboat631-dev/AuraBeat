package com.aurabeat.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraPrimaryLight
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * Custom waveform bars. [progress] drives which portion is "active".
 * When [isPlaying] is true the bars gently breathe with a per-bar phase.
 */
@Composable
fun Waveform(
    progress: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 56,
    height: Dp = 72.dp,
    onSeek: ((Float) -> Unit)? = null,
) {
    val seed = remember { Random(1337) }
    val amplitudes = remember {
        FloatArray(barCount) { 0.28f + seed.nextFloat() * 0.72f }
    }
    val transition = rememberInfiniteTransition(label = "waveform")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .let { m ->
                if (onSeek != null) m.pointerInput(Unit) {
                    detectTapGestures { offset ->
                        onSeek((offset.x / size.width).coerceIn(0f, 1f))
                    }
                } else m
            }
    ) {
        drawWaveform(
            amplitudes = amplitudes,
            progress = progress.coerceIn(0f, 1f),
            phase = if (isPlaying) phase else 0f,
            isPlaying = isPlaying,
            canvasSize = size,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawWaveform(
    amplitudes: FloatArray,
    progress: Float,
    phase: Float,
    isPlaying: Boolean,
    canvasSize: Size,
) {
    val count = amplitudes.size
    val gap = 4f
    val barWidth = ((canvasSize.width - gap * (count - 1)) / count).coerceAtLeast(2f)
    val centerY = canvasSize.height / 2f
    val activeIdx = (progress * count).toInt().coerceIn(0, count)

    for (i in 0 until count) {
        val base = amplitudes[i]
        val wobble = if (isPlaying) 0.15f * sin(phase + i * 0.35f) else 0f
        val amp = (base + wobble).coerceIn(0.15f, 1f)
        val barH = amp * canvasSize.height
        val x = i * (barWidth + gap)
        val color = if (i < activeIdx) AuraPrimary
        else if (i == activeIdx) AuraPrimaryLight
        else Color.White.copy(alpha = 0.18f)

        drawLine(
            color = color,
            start = Offset(x + barWidth / 2f, centerY - barH / 2f),
            end   = Offset(x + barWidth / 2f, centerY + barH / 2f),
            strokeWidth = barWidth,
            cap = StrokeCap.Round,
        )
    }
}
