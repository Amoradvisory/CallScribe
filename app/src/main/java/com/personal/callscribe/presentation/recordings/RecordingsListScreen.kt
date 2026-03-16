package com.personal.callscribe.presentation.recordings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.callscribe.presentation.common.EmptyState
import com.personal.callscribe.presentation.common.MetricPill
import com.personal.callscribe.presentation.common.ScreenScaffold
import com.personal.callscribe.presentation.common.SectionCard
import com.personal.callscribe.presentation.common.screenContentPadding
import com.personal.callscribe.presentation.theme.Clay
import com.personal.callscribe.presentation.theme.DeepTeal
import com.personal.callscribe.util.formatBytes
import com.personal.callscribe.util.formatDateTime
import com.personal.callscribe.util.formatDuration

/**
 * History list screen for saved recordings.
 */
@Composable
fun RecordingsListScreen(
    state: RecordingsListUiState,
    onBack: () -> Unit,
    onOpenDetail: (Long) -> Unit,
) {
    ScreenScaffold(
        title = "Historique",
        subtitle = "Tous les fichiers locaux, du plus recent au plus ancien",
        onBack = onBack,
    ) { paddingValues ->
        LazyColumn(
            contentPadding = screenContentPadding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricPill(
                        label = "Captures",
                        value = state.recordings.size.toString(),
                        modifier = Modifier.weight(1f),
                    )
                    MetricPill(
                        label = "Statut",
                        value = if (state.isLoading) "Chargement" else "Pret",
                        modifier = Modifier.weight(1f),
                        accentColor = Clay,
                    )
                }
            }

            if (state.recordings.isEmpty()) {
                item {
                    EmptyState(
                        title = "Aucune capture locale",
                        body = "Lancez un premier enregistrement depuis l'accueil pour faire apparaitre des WAV ici.",
                    )
                }
            } else {
                items(state.recordings, key = { recording -> recording.id }) { recording ->
                    SectionCard(
                        title = recording.title,
                        subtitle = formatDateTime(recording.createdAtMillis),
                        modifier = Modifier.clickable { onOpenDetail(recording.id) },
                        accentColor = if (recording.speakerphoneActivated) DeepTeal else Clay,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(text = "Duree: ${formatDuration(recording.durationMs)}")
                                Text(text = "Taille: ${formatBytes(recording.sizeBytes)}")
                            }
                            Text(
                                text = if (recording.speakerphoneActivated) {
                                    "Speakerphone actif pendant la capture"
                                } else if (recording.speakerphoneRequested) {
                                    "Speakerphone demande mais non confirme"
                                } else {
                                    "Capture sans demande de speakerphone"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
