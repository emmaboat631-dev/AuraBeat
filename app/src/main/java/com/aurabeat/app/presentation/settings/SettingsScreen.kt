package com.aurabeat.app.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.aurabeat.app.BuildConfig
import com.aurabeat.app.data.preferences.AudioQuality
import com.aurabeat.app.data.preferences.AuraPrefs
import com.aurabeat.app.data.preferences.PreferencesRepository
import com.aurabeat.app.ui.components.GlassCard
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraPrimaryLight
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

private enum class SettingId(val icon: ImageVector, val title: String) {
    THEME(Icons.Rounded.Palette,            "Theme"),
    AUDIO_QUALITY(Icons.Rounded.HighQuality, "Audio Quality"),
    EQUALIZER(Icons.Rounded.Equalizer,       "Equalizer"),
    DOWNLOADS(Icons.Rounded.CloudDownload,   "Download Settings"),
    NOTIFICATIONS(Icons.Rounded.Notifications,"Notifications"),
    STORAGE(Icons.Rounded.Storage,           "Data & Storage"),
    HELP(Icons.Rounded.HelpOutline,          "Help & Support"),
    ABOUT(Icons.Rounded.Info,                "About"),
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepo: PreferencesRepository,
) : ViewModel() {
    val prefs: StateFlow<AuraPrefs> =
        prefsRepo.prefs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuraPrefs())

    fun setAudioQuality(q: AudioQuality) { viewModelScope.launch { prefsRepo.setAudioQuality(q) } }
    fun setWifiOnly(enabled: Boolean) { viewModelScope.launch { prefsRepo.setDownloadWifiOnly(enabled) } }
    fun setNotifications(enabled: Boolean) { viewModelScope.launch { prefsRepo.setNotificationsEnabled(enabled) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    nav: NavHostController,
    vm: SettingsViewModel = hiltViewModel(),
) {
    val prefs by vm.prefs.collectAsStateWithLifecycle()
    var openSheet by remember { mutableStateOf<SettingId?>(null) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 220.dp, top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.xs),
    ) {
        item { ProfileHeader() }
        item { Spacer(Modifier.height(AuraSpacing.md)) }
        item { com.aurabeat.app.presentation.spotify.SpotifyConnectSection() }
        item { Spacer(Modifier.height(AuraSpacing.md)) }
        items(SettingId.entries) { item ->
            SettingRow(
                icon = item.icon,
                title = item.title,
                subtitle = subtitleFor(item, prefs),
                onClick = { openSheet = item },
            )
        }
    }

    openSheet?.let { id ->
        ModalBottomSheet(
            onDismissRequest = { openSheet = null },
            containerColor = AuraSurfaceElev,
        ) {
            SettingSheetContent(
                id = id,
                prefs = prefs,
                onAudioQuality = vm::setAudioQuality,
                onWifiOnly = vm::setWifiOnly,
                onNotifications = vm::setNotifications,
            )
            Spacer(Modifier.height(AuraSpacing.xl))
        }
    }
}

private fun subtitleFor(id: SettingId, prefs: AuraPrefs): String = when (id) {
    SettingId.THEME         -> "AuraBeat Dark"
    SettingId.AUDIO_QUALITY -> "${prefs.audioQuality.label} · ${prefs.audioQuality.bitrate}"
    SettingId.EQUALIZER     -> "Off"
    SettingId.DOWNLOADS     -> if (prefs.downloadWifiOnly) "Wi-Fi only" else "Wi-Fi & mobile"
    SettingId.NOTIFICATIONS -> if (prefs.notificationsEnabled) "Enabled" else "Disabled"
    SettingId.STORAGE       -> "Manage cache"
    SettingId.HELP          -> "Support & feedback"
    SettingId.ABOUT         -> "AuraBeat ${BuildConfig.VERSION_NAME}"
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(AuraSurfaceElev),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, tint = AuraPrimaryLight) }
        Spacer(Modifier.width(AuraSpacing.md))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = AuraTextMuted)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = AuraTextMuted)
    }
}

@Composable
private fun SettingSheetContent(
    id: SettingId,
    prefs: AuraPrefs,
    onAudioQuality: (AudioQuality) -> Unit,
    onWifiOnly: (Boolean) -> Unit,
    onNotifications: (Boolean) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.md)) {
        Text(id.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = AuraSpacing.sm))
        when (id) {
            SettingId.THEME -> RadioList(
                options = listOf("System", "Light", "Dark"),
                selected = "Dark",
                enabledIndex = 2,
                onSelect = { /* dark-only for now */ },
                footnote = "Light and system themes are coming in a future release.",
            )
            SettingId.AUDIO_QUALITY -> RadioList(
                options = AudioQuality.entries.map { "${it.label} · ${it.bitrate}" },
                selected = "${prefs.audioQuality.label} · ${prefs.audioQuality.bitrate}",
                onSelect = { label ->
                    AudioQuality.entries.firstOrNull { "${it.label} · ${it.bitrate}" == label }
                        ?.let(onAudioQuality)
                },
                footnote = "Higher quality uses more data. Local files always play at source quality.",
            )
            SettingId.EQUALIZER -> ComingSoon(
                "A 5-band EQ with presets is on the roadmap — currently the app plays audio at source with the system's built-in effects.",
            )
            SettingId.DOWNLOADS -> ToggleRow(
                label = "Download over Wi-Fi only",
                checked = prefs.downloadWifiOnly,
                onCheckedChange = onWifiOnly,
                footnote = "Prevents streaming and downloads over cellular. Local files are unaffected.",
            )
            SettingId.NOTIFICATIONS -> ToggleRow(
                label = "Show media notification while playing",
                checked = prefs.notificationsEnabled,
                onCheckedChange = onNotifications,
                footnote = "Applies on next track change. Android also lets you silence it from the system notification settings.",
            )
            SettingId.STORAGE -> StoragePanel()
            SettingId.HELP -> Column {
                Text(
                    "AuraBeat is a portfolio project. Reach out at emmaboat631@gmail.com for feedback or bug reports.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface,
                )
            }
            SettingId.ABOUT -> AboutPanel()
        }
    }
}

@Composable
private fun RadioList(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    enabledIndex: Int? = null,
    footnote: String? = null,
) {
    Column {
        options.forEachIndexed { index, opt ->
            val isEnabled = enabledIndex == null || index == enabledIndex
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isEnabled) { onSelect(opt) }
                    .padding(vertical = AuraSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = opt == selected,
                    onClick = { if (isEnabled) onSelect(opt) },
                    enabled = isEnabled,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AuraPrimary,
                        unselectedColor = AuraTextSecondary,
                    ),
                )
                Spacer(Modifier.width(AuraSpacing.sm))
                Text(
                    opt,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface else AuraTextMuted,
                )
            }
        }
        footnote?.let {
            Spacer(Modifier.height(AuraSpacing.xs))
            Text(it, style = MaterialTheme.typography.bodySmall, color = AuraTextMuted)
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, footnote: String? = null) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(vertical = AuraSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AuraPrimary,
                    uncheckedTrackColor = AuraSurfaceElev,
                ),
            )
        }
        footnote?.let {
            Spacer(Modifier.height(AuraSpacing.xs))
            Text(it, style = MaterialTheme.typography.bodySmall, color = AuraTextMuted)
        }
    }
}

@Composable
private fun ComingSoon(text: String) {
    Column {
        Text("Coming soon", style = MaterialTheme.typography.titleMedium, color = AuraPrimaryLight)
        Spacer(Modifier.height(AuraSpacing.xs))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun StoragePanel() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cacheBytes by remember {
        mutableStateOf(context.cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() })
    }
    val mb = String.format("%.1f", cacheBytes / 1024.0 / 1024.0)
    Column {
        Text("Cache: $mb MB", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(AuraSpacing.xs))
        Text("Includes decoded artwork and buffered audio segments.", style = MaterialTheme.typography.bodySmall, color = AuraTextMuted)
        Spacer(Modifier.height(AuraSpacing.md))
        Row(
            Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(AuraPrimaryLight, AuraPrimary)))
                .clickable {
                    scope.launch {
                        context.cacheDir.deleteRecursively()
                        context.cacheDir.mkdirs()
                        cacheBytes = 0
                    }
                }
                .padding(horizontal = 24.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) { Text("Clear cache", style = MaterialTheme.typography.labelLarge, color = Color.White) }
    }
}

@Composable
private fun AboutPanel() {
    Column {
        Text("AuraBeat ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(AuraSpacing.xs))
        Text("Native Android · Kotlin · Jetpack Compose · Media3 · Hilt · Room",
            style = MaterialTheme.typography.bodySmall, color = AuraTextMuted)
        Spacer(Modifier.height(AuraSpacing.md))
        Text("Bundled tracks are the property of their respective rights holders. Spotify metadata and playback control © Spotify AB.",
            style = MaterialTheme.typography.bodySmall, color = AuraTextMuted)
    }
}

@Composable
private fun ProfileHeader() {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.md),
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(AuraSpacing.md))
        GlassCard(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(AuraSpacing.md), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(64.dp).clip(CircleShape)
                        .background(Brush.linearGradient(listOf(AuraPrimaryLight, AuraPrimary))),
                    contentAlignment = Alignment.Center,
                ) { Text("A", style = MaterialTheme.typography.headlineMedium, color = Color.White) }
                Spacer(Modifier.width(AuraSpacing.md))
                Column(Modifier.weight(1f)) {
                    Text("AuraBeat User", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text("Premium · Since 2025", style = MaterialTheme.typography.bodyMedium, color = AuraTextSecondary)
                }
            }
        }
    }
}
