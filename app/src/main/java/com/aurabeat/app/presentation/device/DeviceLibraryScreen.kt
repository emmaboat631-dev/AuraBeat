package com.aurabeat.app.presentation.device

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.aurabeat.app.data.model.Song
import com.aurabeat.app.data.repository.DeviceMusicRepository
import com.aurabeat.app.presentation.PlayerViewModel
import com.aurabeat.app.ui.components.SectionHeader
import com.aurabeat.app.ui.components.SongRow
import com.aurabeat.app.ui.theme.AuraPrimary
import com.aurabeat.app.ui.theme.AuraPrimaryLight
import com.aurabeat.app.ui.theme.AuraSpacing
import com.aurabeat.app.ui.theme.AuraSurfaceElev
import com.aurabeat.app.ui.theme.AuraTextMuted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DeviceLibraryViewModel @Inject constructor(
    private val repo: DeviceMusicRepository,
) : ViewModel() {
    val songs: StateFlow<List<Song>> = repo.deviceLibrary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun importFiles(uris: List<Uri>) {
        viewModelScope.launch { uris.forEach { repo.importFile(it) } }
    }

    fun importFolder(uri: Uri) {
        viewModelScope.launch { repo.importFolder(uri) }
    }
}

@Composable
fun DeviceLibraryScreen(
    nav: NavHostController,
    playerVm: PlayerViewModel,
    vm: DeviceLibraryViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val songs by vm.songs.collectAsStateWithLifecycle()
    val playerState by playerVm.state.collectAsStateWithLifecycle()

    val audioPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
    }
    var permGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
        )
    }
    val requestPerm = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permGranted = it
    }

    val pickFiles = rememberLauncherForActivityResult(PersistableOpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) vm.importFiles(uris)
    }
    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let(vm::importFolder)
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 220.dp),
    ) {
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Brush.verticalGradient(listOf(AuraPrimaryLight.copy(alpha = 0.6f), Color(0xFF09070D)))),
            ) {
                IconButton(onClick = { nav.popBackStack() }, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(
                    Modifier.align(Alignment.BottomStart).padding(AuraSpacing.md),
                ) {
                    Text("Device Music", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Text(
                        if (permGranted) "${songs.size} tracks on this device"
                        else "Permission needed to scan your library",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AuraTextMuted,
                    )
                }
            }
        }

        if (!permGranted) {
            item {
                Column(
                    Modifier.fillMaxWidth().padding(AuraSpacing.xl),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Grant audio access to scan the music files on your phone.",
                        style = MaterialTheme.typography.bodyMedium, color = AuraTextMuted,
                    )
                    Spacer(Modifier.height(AuraSpacing.md))
                    PillButton("Grant access", AuraPrimary) { requestPerm.launch(audioPermission) }
                    Spacer(Modifier.height(AuraSpacing.sm))
                    Text("— or —", style = MaterialTheme.typography.labelSmall, color = AuraTextMuted)
                    Spacer(Modifier.height(AuraSpacing.sm))
                    Text(
                        "Pick specific files or a folder without granting broad access:",
                        style = MaterialTheme.typography.bodySmall, color = AuraTextMuted,
                    )
                }
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = AuraSpacing.md, vertical = AuraSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm),
            ) {
                ImportTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.NoteAdd,
                    title = "Add files",
                    onClick = { pickFiles.launch(arrayOf("audio/*")) },
                )
                ImportTile(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Rounded.CreateNewFolder,
                    title = "Add folder",
                    onClick = { pickFolder.launch(null) },
                )
            }
        }

        if (songs.isEmpty() && permGranted) {
            item {
                Box(Modifier.fillMaxWidth().padding(AuraSpacing.xl), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.LibraryMusic, contentDescription = null, tint = AuraTextMuted, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No music found yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Drop files into your Music folder, or add specific files above.", style = MaterialTheme.typography.bodySmall, color = AuraTextMuted)
                    }
                }
            }
        } else if (songs.isNotEmpty()) {
            item { SectionHeader("All device tracks") }
            items(songs, key = { it.id }) { song ->
                SongRow(
                    song = song,
                    isPlaying = playerState.currentSong?.id == song.id,
                    onClick = { playerVm.play(song, songs) },
                )
            }
        }
    }
}

@Composable
private fun ImportTile(modifier: Modifier, icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AuraSurfaceElev)
            .clickable(onClick = onClick)
            .padding(horizontal = AuraSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = AuraPrimaryLight)
        Spacer(Modifier.width(AuraSpacing.sm))
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

/**
 * Same as [ActivityResultContracts.OpenMultipleDocuments], but the intent
 * asks the SAF picker for persistable read permission — so the URIs the
 * caller receives can be re-opened after the app restarts.
 */
private class PersistableOpenMultipleDocuments :
    ActivityResultContract<Array<String>, List<Uri>>() {
    override fun createIntent(context: Context, input: Array<String>): Intent =
        Intent(Intent.ACTION_OPEN_DOCUMENT)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            .setType("*/*")
            .putExtra(Intent.EXTRA_MIME_TYPES, input)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        if (intent == null || resultCode != android.app.Activity.RESULT_OK) return emptyList()
        val out = mutableListOf<Uri>()
        intent.clipData?.let { clip ->
            for (i in 0 until clip.itemCount) clip.getItemAt(i).uri?.let(out::add)
        } ?: intent.data?.let(out::add)
        return out
    }
}

@Composable
private fun PillButton(text: String, color: Color, onClick: () -> Unit) {
    Row(
        Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
    }
}
