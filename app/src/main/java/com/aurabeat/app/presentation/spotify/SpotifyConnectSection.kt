package com.aurabeat.app.presentation.spotify

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.aurabeat.app.spotify.SpotifyRepository
import com.aurabeat.app.spotify.UserProfile
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraSurfaceElev
import com.aurabeat.app.ui.theme.AuraTextMuted
import com.aurabeat.app.ui.theme.AuraTextSecondary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private val SpotifyGreen = Color(0xFF1DB954)

@HiltViewModel
class SpotifyConnectViewModel @Inject constructor(
    private val repo: SpotifyRepository,
) : ViewModel() {
    val auth: StateFlow<com.aurabeat.app.spotify.SpotifyAuthState> =
        repo.authState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), com.aurabeat.app.spotify.SpotifyAuthState())

    var profile by mutableStateOf<UserProfile?>(null)
        private set
    var loadingProfile by mutableStateOf(false)
        private set

    fun connect() = viewModelScope.launch { repo.startAuthorization() }
    fun disconnect() = viewModelScope.launch {
        repo.disconnect()
        profile = null
    }
    fun refreshProfile() = viewModelScope.launch {
        loadingProfile = true
        profile = repo.profile()
        loadingProfile = false
    }
}

@Composable
fun SpotifyConnectSection(vm: SpotifyConnectViewModel = hiltViewModel()) {
    val auth by vm.auth.collectAsStateWithLifecycle()

    // Whenever we become connected, fetch the profile once.
    LaunchedEffect(auth.connected) {
        if (auth.connected && vm.profile == null) vm.refreshProfile()
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = AuraSpacing.md)
            .clip(RoundedCornerShape(20.dp))
            .background(AuraSurfaceElev)
            .padding(AuraSpacing.md),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(40.dp).clip(CircleShape).background(SpotifyGreen),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(AuraSpacing.md))
                Column(Modifier.weight(1f)) {
                    Text("Spotify", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                    val sub = when {
                        auth.connected && vm.profile != null -> "Connected as ${vm.profile?.displayName ?: vm.profile?.id}"
                        auth.connected -> "Connected"
                        else -> "Not connected"
                    }
                    Text(sub, style = MaterialTheme.typography.bodySmall, color = AuraTextSecondary)
                }
                if (vm.loadingProfile) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = SpotifyGreen)
                } else if (auth.connected) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = "Connected", tint = SpotifyGreen)
                }
            }

            Spacer(Modifier.height(AuraSpacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
                if (auth.connected) {
                    PillButton("Disconnect", background = Color.White.copy(alpha = 0.08f), textColor = Color.White) { vm.disconnect() }
                } else {
                    PillButton("Connect to Spotify", background = SpotifyGreen, textColor = Color.White) { vm.connect() }
                }
            }

            vm.profile?.product?.let { product ->
                Spacer(Modifier.height(AuraSpacing.xs))
                Text(
                    "Account: $product${vm.profile?.country?.let { " · $it" }.orEmpty()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AuraTextMuted,
                )
            }
        }
    }
}

@Composable
private fun PillButton(text: String, background: Color, textColor: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = textColor)
    }
}
