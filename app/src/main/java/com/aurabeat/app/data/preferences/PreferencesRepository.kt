package com.aurabeat.app.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aurabeat.app.player.RepeatMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "aurabeat_prefs")

enum class AudioQuality(val label: String, val bitrate: String) {
    LOW("Low", "96 kbps"),
    NORMAL("Normal", "160 kbps"),
    HIGH("High", "320 kbps"),
    AUTO("Auto", "Adapts to network"),
}

data class AuraPrefs(
    val shuffle: Boolean = false,
    val repeat: RepeatMode = RepeatMode.OFF,
    val audioQuality: AudioQuality = AudioQuality.HIGH,
    val downloadWifiOnly: Boolean = true,
    val notificationsEnabled: Boolean = true,
)

@Singleton
class PreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val SHUFFLE       = booleanPreferencesKey("shuffle")
        val REPEAT        = intPreferencesKey("repeat")
        val AUDIO_QUALITY = intPreferencesKey("audio_quality")
        val WIFI_ONLY     = booleanPreferencesKey("wifi_only")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
    }

    val prefs: Flow<AuraPrefs> = context.dataStore.data.map { store ->
        AuraPrefs(
            shuffle = store[Keys.SHUFFLE] ?: false,
            repeat = RepeatMode.entries.getOrNull(store[Keys.REPEAT] ?: 0) ?: RepeatMode.OFF,
            audioQuality = AudioQuality.entries.getOrNull(store[Keys.AUDIO_QUALITY] ?: AudioQuality.HIGH.ordinal) ?: AudioQuality.HIGH,
            downloadWifiOnly = store[Keys.WIFI_ONLY] ?: true,
            notificationsEnabled = store[Keys.NOTIFICATIONS] ?: true,
        )
    }

    suspend fun setShuffle(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHUFFLE] = enabled }
    }

    suspend fun setRepeat(mode: RepeatMode) {
        context.dataStore.edit { it[Keys.REPEAT] = mode.ordinal }
    }

    suspend fun setAudioQuality(quality: AudioQuality) {
        context.dataStore.edit { it[Keys.AUDIO_QUALITY] = quality.ordinal }
    }

    suspend fun setDownloadWifiOnly(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WIFI_ONLY] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    }
}
