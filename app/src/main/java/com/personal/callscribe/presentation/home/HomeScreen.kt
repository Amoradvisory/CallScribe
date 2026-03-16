package com.personal.callscribe.presentation.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.personal.callscribe.domain.model.CallState
import com.personal.callscribe.domain.model.Recording
import com.personal.callscribe.domain.model.SessionState
import com.personal.callscribe.presentation.common.ActionTile
import com.personal.callscribe.presentation.common.InlineBanner
import com.personal.callscribe.presentation.common.MetricPill
import com.personal.callscribe.presentation.common.ScreenScaffold
import com.personal.callscribe.presentation.common.SectionCard
import com.personal.callscribe.presentation.common.StatusBadge
import com.personal.callscribe.presentation.common.screenContentPadding
import com.personal.callscribe.presentation.theme.Clay
import com.personal.callscribe.presentation.theme.Danger
import com.personal.callscribe.presentation.theme.DeepTeal
import com.personal.callscribe.presentation.theme.Moss
import com.personal.callscribe.util.formatDateTime
import com.personal.callscribe.util.formatDuration
import com.personal.callscribe.util.toUserMessage

/**
 * Main dashboard screen.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onRefreshPermissions: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenRecordings: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCalibration: () -> Unit,
    onOpenDiagnostics: () -> Unit,
    onOpenRecordingDetail: (Long) -> Unit,
) {
    LifecycleResumeEffect(Unit) {
        onRefreshPermissions()
        onPauseOrDispose { }
    }

    val sessionPresentation = sessionPresentationOf(state.sessionState)
    val isBusy = state.sessionState is SessionState.Preparing ||
        state.sessionState is SessionState.EnablingSpeaker ||
        state.sessionState is SessionState.Finalizing
    val isRecording = state.sessionState is SessionState.Recording
    val primaryActionLabel = when {
        !state.allPermissionsGranted -> "Autoriser l'application"
        isRecording -> "Arreter la capture"
        isBusy -> "Preparation en cours"
        else -> "Demarrer une capture"
    }

    ScreenScaffold(
        title = "CallScribe",
        subtitle = "Capture locale, simple et lisible",
    ) { paddingValues ->
        LazyColumn(
            contentPadding = screenContentPadding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionCard(
                    title = "Tableau de bord",
                    subtitle = "Tout ce qu'il faut pour lancer ou verifier une capture",
                    accentColor = sessionPresentation.accentColor,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Text(
                                    text = sessionPresentation.title,
                                    style = MaterialTheme.typography.headlineMedium,
                                )
                                Text(
                                    text = sessionPresentation.body,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            StatusBadge(
                                text = callStateLabel(state.callState),
                                color = callStateColor(state.callState),
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricPill(
                                label = "Mode",
                                value = if (state.settings.autoStartOnCall) "Auto" else "Manuel",
                                modifier = Modifier.weight(1f),
                            )
                            MetricPill(
                                label = "Speakerphone",
                                value = if (state.settings.speakerphoneEnabled) "Demande" else "Off",
                                modifier = Modifier.weight(1f),
                            )
                        }

                        if (state.sessionState is SessionState.Recording) {
                            val levelProgress = ((state.sessionState.audioLevel.decibels + 60.0) / 60.0)
                                .toFloat()
                                .coerceIn(0f, 1f)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Niveau audio en direct",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                LinearProgressIndicator(
                                    progress = { levelProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp),
                                )
                                Text(
                                    text = "${"%.1f".format(state.sessionState.audioLevel.decibels)} dB | " +
                                        formatDuration(state.sessionState.elapsedMs),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = {
                                    when {
                                        !state.allPermissionsGranted -> onOpenPermissions()
                                        isRecording -> onStopRecording()
                                        else -> onStartRecording()
                                    }
                                },
                                enabled = !isBusy,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(text = primaryActionLabel)
                            }
                            OutlinedButton(
                                onClick = onOpenRecordings,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(text = "Historique")
                            }
                        }
                    }
                }
            }

            if (!state.allPermissionsGranted) {
                item {
                    InlineBanner(
                        title = "Permissions manquantes",
                        body = "Accordez les permissions audio et telephone avant de lancer une capture.",
                        accentColor = Danger,
                        actionLabel = "Ouvrir l'ecran des permissions",
                        onAction = onOpenPermissions,
                    )
                }
            }

            when (val sessionState = state.sessionState) {
                is SessionState.Completed -> {
                    item {
                        InlineBanner(
                            title = "Capture terminee",
                            body = "Le fichier ${sessionState.recording.fileName} est pret dans l'historique.",
                            accentColor = Moss,
                            actionLabel = "Ouvrir le detail",
                            onAction = { onOpenRecordingDetail(sessionState.recording.id) },
                        )
                    }
                }

                is SessionState.Error -> {
                    item {
                        InlineBanner(
                            title = "Derniere tentative interrompue",
                            body = sessionState.error.toUserMessage(),
                            accentColor = Danger,
                        )
                    }
                }

                else -> Unit
            }

            item {
                SectionCard(
                    title = "Raccourcis",
                    subtitle = "Les ecrans utiles sont regroupes ici",
                    accentColor = DeepTeal,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ActionTile(
                                title = "Historique",
                                body = "Consulter et partager les fichiers deja captures.",
                                icon = Icons.Filled.History,
                                onClick = onOpenRecordings,
                                modifier = Modifier.weight(1f),
                            )
                            ActionTile(
                                title = "Reglages",
                                body = "Adapter l'app au comportement de votre telephone.",
                                icon = Icons.Filled.Settings,
                                onClick = onOpenSettings,
                                modifier = Modifier.weight(1f),
                                accentColor = Clay,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ActionTile(
                                title = "Calibration",
                                body = "Verifier le niveau du micro avant un vrai appel.",
                                icon = Icons.Filled.GraphicEq,
                                onClick = onOpenCalibration,
                                modifier = Modifier.weight(1f),
                                accentColor = Moss,
                            )
                            ActionTile(
                                title = "Diagnostics",
                                body = "Lire les logs et comprendre un comportement anormal.",
                                icon = Icons.Filled.BugReport,
                                onClick = onOpenDiagnostics,
                                modifier = Modifier.weight(1f),
                                accentColor = Danger,
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(
                    title = "Configuration active",
                    subtitle = "Resume instantane de ce qui sera applique a la prochaine capture",
                    accentColor = Clay,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "Gain micro: ${state.settings.inputGainPercent}%")
                        Text(text = "Sample rate: ${state.settings.preferredSampleRateHz} Hz")
                        Text(text = "Prefixe fichier: ${state.settings.preferredFilePrefix}")
                        Text(text = "Garder l'ecran allume: ${if (state.settings.keepScreenOn) "Oui" else "Non"}")
                    }
                }
            }

            item {
                SectionCard(
                    title = "Recents",
                    subtitle = if (state.recentRecordings.isEmpty()) {
                        "Aucune capture locale pour l'instant"
                    } else {
                        "Les trois derniers fichiers sont accessibles en un geste"
                    },
                    accentColor = DeepTeal,
                ) {
                    if (state.recentRecordings.isEmpty()) {
                        Text(
                            text = "Demarrez un premier enregistrement pour remplir l'historique.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.recentRecordings.forEach { recording ->
                                RecentRecordingRow(
                                    recording = recording,
                                    onClick = { onOpenRecordingDetail(recording.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentRecordingRow(
    recording: Recording,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = recording.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = formatDateTime(recording.createdAtMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = formatDuration(recording.durationMs),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun callStateLabel(callState: CallState): String = when (callState) {
    CallState.IDLE -> "Aucun appel"
    CallState.RINGING -> "Sonnerie"
    CallState.OFF_HOOK -> "Appel actif"
    CallState.UNKNOWN -> "Etat inconnu"
}

private fun callStateColor(callState: CallState) = when (callState) {
    CallState.OFF_HOOK -> DeepTeal
    CallState.RINGING -> Clay
    CallState.IDLE -> Moss
    CallState.UNKNOWN -> Danger
}

private fun sessionPresentationOf(sessionState: SessionState): SessionPresentation = when (sessionState) {
    SessionState.Idle -> SessionPresentation(
        title = "Pret a enregistrer",
        body = "Lancez une capture manuelle ou laissez l'auto-start surveiller le prochain appel.",
        accentColor = DeepTeal,
    )

    is SessionState.Preparing -> SessionPresentation(
        title = "Preparation audio",
        body = sessionState.message,
        accentColor = Clay,
    )

    is SessionState.EnablingSpeaker -> SessionPresentation(
        title = "Activation du haut-parleur",
        body = "L'application tente de diffuser la voix distante sur le HP.",
        accentColor = Clay,
    )

    is SessionState.Recording -> SessionPresentation(
        title = "Capture en cours",
        body = "Le service enregistre le micro et ecrit un WAV local en temps reel.",
        accentColor = Moss,
    )

    is SessionState.Finalizing -> SessionPresentation(
        title = "Finalisation",
        body = "Le header WAV et les metadonnees sont en cours d'ecriture.",
        accentColor = Clay,
    )

    is SessionState.Completed -> SessionPresentation(
        title = "Capture sauvegardee",
        body = "Le dernier fichier est deja disponible dans l'historique.",
        accentColor = Moss,
    )

    is SessionState.Error -> SessionPresentation(
        title = "Action requise",
        body = sessionState.error.toUserMessage(),
        accentColor = Danger,
    )
}

private data class SessionPresentation(
    val title: String,
    val body: String,
    val accentColor: androidx.compose.ui.graphics.Color,
)
