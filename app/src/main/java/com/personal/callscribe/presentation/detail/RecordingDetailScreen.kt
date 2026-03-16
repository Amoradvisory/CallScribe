package com.personal.callscribe.presentation.detail

import android.content.Intent
import android.media.MediaPlayer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.personal.callscribe.presentation.common.EmptyState
import com.personal.callscribe.presentation.common.InlineBanner
import com.personal.callscribe.presentation.common.InfoRow
import com.personal.callscribe.presentation.common.ScreenScaffold
import com.personal.callscribe.presentation.common.SectionCard
import com.personal.callscribe.presentation.common.screenContentPadding
import com.personal.callscribe.presentation.theme.Clay
import com.personal.callscribe.presentation.theme.Danger
import com.personal.callscribe.presentation.theme.DeepTeal
import com.personal.callscribe.presentation.theme.Moss
import com.personal.callscribe.util.formatBytes
import com.personal.callscribe.util.formatDateTime
import com.personal.callscribe.util.formatDuration
import java.io.File
import kotlinx.coroutines.delay

/**
 * Recording detail screen with playback, rename, delete and share actions.
 */
@Composable
fun RecordingDetailScreen(
    state: RecordingDetailUiState,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onRequestExport: () -> Unit,
    onExportResolved: () -> Unit,
) {
    val context = LocalContext.current
    val recording = state.recording
    var renameText by remember(recording?.title) { mutableStateOf(recording?.title.orEmpty()) }
    var isPlaying by remember(recording?.id) { mutableStateOf(false) }
    var sliderPosition by remember(recording?.id) { mutableFloatStateOf(0f) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val mediaPlayer = remember(recording?.filePath) {
        recording?.filePath?.let { path ->
            runCatching {
                MediaPlayer().apply {
                    setDataSource(path)
                    prepare()
                }
            }.getOrNull()
        }
    }

    DisposableEffect(mediaPlayer) {
        mediaPlayer?.setOnCompletionListener {
            isPlaying = false
            sliderPosition = 1f
        }
        onDispose {
            mediaPlayer?.release()
        }
    }

    LaunchedEffect(mediaPlayer, isPlaying) {
        val player = mediaPlayer ?: return@LaunchedEffect
        if (!isPlaying) {
            return@LaunchedEffect
        }
        val duration = player.duration.coerceAtLeast(1)
        while (isPlaying && player.isPlaying) {
            sliderPosition = player.currentPosition.toFloat() / duration.toFloat()
            delay(250L)
        }
    }

    LaunchedEffect(state.pendingExportPath) {
        val exportPath = state.pendingExportPath ?: return@LaunchedEffect
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(exportPath),
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/wav"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            context.startActivity(Intent.createChooser(shareIntent, "Partager l'enregistrement"))
        }
        onExportResolved()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Supprimer ce fichier ?") },
            text = { Text(text = "Le WAV et sa fiche d'historique seront supprimes localement.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) {
                    Text(text = "Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "Annuler")
                }
            },
        )
    }

    ScreenScaffold(
        title = "Detail",
        subtitle = "Lecture, partage et maintenance du fichier",
        onBack = onBack,
    ) { paddingValues ->
        LazyColumn(
            contentPadding = screenContentPadding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            when {
                state.isLoading -> {
                    item {
                        InlineBanner(
                            title = "Chargement",
                            body = "Les metadonnees et le lecteur se preparant...",
                            accentColor = Clay,
                        )
                    }
                }

                recording == null -> {
                    item {
                        EmptyState(
                            title = "Enregistrement introuvable",
                            body = state.errorMessage ?: "Impossible de charger ce fichier.",
                        )
                    }
                }

                else -> {
                    item {
                        SectionCard(
                            title = "Vue d'ensemble",
                            subtitle = recording.fileName,
                            accentColor = DeepTeal,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                InfoRow(label = "Titre", value = recording.title)
                                InfoRow(label = "Cree le", value = formatDateTime(recording.createdAtMillis))
                                InfoRow(label = "Duree", value = formatDuration(recording.durationMs))
                                InfoRow(label = "Taille", value = formatBytes(recording.sizeBytes))
                                InfoRow(label = "Sample rate", value = "${recording.sampleRateHz} Hz")
                            }
                        }
                    }

                    item {
                        SectionCard(
                            title = "Lecture",
                            subtitle = "Controle rapide de l'audio exporte",
                            accentColor = Moss,
                        ) {
                            if (mediaPlayer == null) {
                                Text(
                                    text = "Lecture indisponible pour ce fichier sur cet appareil.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            } else {
                                val totalDurationMs = mediaPlayer.duration.toLong().coerceAtLeast(recording.durationMs)
                                val currentDurationMs = (totalDurationMs * sliderPosition).toLong()
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Slider(
                                        value = sliderPosition,
                                        onValueChange = { value ->
                                            sliderPosition = value
                                            mediaPlayer.seekTo((totalDurationMs * value).toInt())
                                        },
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = formatDuration(currentDurationMs),
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Text(
                                            text = formatDuration(totalDurationMs),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Button(
                                            onClick = {
                                                if (mediaPlayer.isPlaying) {
                                                    mediaPlayer.pause()
                                                    isPlaying = false
                                                } else {
                                                    if (sliderPosition >= 1f) {
                                                        mediaPlayer.seekTo(0)
                                                        sliderPosition = 0f
                                                    }
                                                    mediaPlayer.start()
                                                    isPlaying = true
                                                }
                                            },
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Text(text = if (isPlaying) "Pause" else "Lecture")
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                mediaPlayer.seekTo(0)
                                                mediaPlayer.pause()
                                                isPlaying = false
                                                sliderPosition = 0f
                                            },
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Text(text = "Revenir au debut")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        SectionCard(
                            title = "Renommer",
                            subtitle = "Gardez un historique facile a relire",
                            accentColor = Clay,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = renameText,
                                    onValueChange = { renameText = it },
                                    label = { Text(text = "Titre visible dans l'historique") },
                                    singleLine = true,
                                )
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { onRename(renameText) },
                                ) {
                                    Text(text = "Enregistrer le nouveau titre")
                                }
                            }
                        }
                    }

                    item {
                        SectionCard(
                            title = "Actions",
                            subtitle = "Partager le WAV ou nettoyer l'historique",
                            accentColor = DeepTeal,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = onRequestExport,
                                ) {
                                    Text(text = "Partager le fichier WAV")
                                }
                                OutlinedButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { showDeleteDialog = true },
                                ) {
                                    Text(text = "Supprimer ce fichier")
                                }
                            }
                        }
                    }
                }
            }

            state.errorMessage?.takeIf { it.isNotBlank() }?.let { message ->
                item {
                    InlineBanner(
                        title = "Information",
                        body = message,
                        accentColor = Danger,
                    )
                }
            }
        }
    }
}
