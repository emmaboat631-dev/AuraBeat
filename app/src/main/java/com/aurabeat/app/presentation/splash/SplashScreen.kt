package com.aurabeat.app.presentation.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aurabeat.app.ui.theme.AuraBackground
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraPrimaryLight
import com.aurabeat.app.ui.theme.AuraTextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1600)
        onFinished()
    }
    val transition = rememberInfiniteTransition(label = "splash")
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1E1B4B), AuraBackground),
                    radius = 900f,
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulse)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AuraPrimaryLight, AuraPrimary))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(72.dp),
                )
            }
            Spacer(Modifier.height(28.dp))
            Text("AuraBeat", style = MaterialTheme.typography.displayMedium, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text("Feel the sound", style = MaterialTheme.typography.bodyLarge, color = AuraTextSecondary)
            Spacer(Modifier.height(36.dp))
            CircularProgressIndicator(color = AuraPrimaryLight, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
        }
    }
}
