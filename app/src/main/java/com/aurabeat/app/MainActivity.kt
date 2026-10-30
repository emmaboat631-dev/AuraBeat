package com.aurabeat.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.aurabeat.app.navigation.AppNavigation
import com.aurabeat.app.spotify.SpotifyRepository
import com.aurabeat.app.ui.theme.AuraBackground
import com.aurabeat.app.ui.theme.AuraBeatTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var spotify: SpotifyRepository

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* silently ignored — playback still works */ }

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* Device library shows a prompt if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        maybeRequestNotificationPermission()
        maybeRequestAudioPermission()
        handleSpotifyRedirect(intent)
        setContent {
            AuraBeatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().background(AuraBackground),
                    color = AuraBackground,
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun maybeRequestAudioPermission() {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val granted = ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
        if (!granted) requestAudioPermission.launch(perm)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleSpotifyRedirect(intent)
    }

    private fun handleSpotifyRedirect(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme == "com.aurabeat.app" && data.host == "spotify-callback") {
            lifecycleScope.launch { spotify.handleRedirect(data) }
        }
    }
}
