package com.personal.callscribe.presentation.diagnostics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.callscribe.domain.model.DiagnosticLevel
import com.personal.callscribe.presentation.common.EmptyState
import com.personal.callscribe.presentation.common.InlineBanner
import com.personal.callscribe.presentation.common.ScreenScaffold
import com.personal.callscribe.presentation.common.SectionCard
import com.personal.callscribe.presentation.common.screenContentPadding
import com.personal.callscribe.presentation.theme.Clay
import com.personal.callscribe.presentation.theme.Danger
import com.personal.callscribe.presentation.theme.DeepTeal
import com.personal.callscribe.presentation.theme.Moss
import com.personal.callscribe.util.formatDateTime

/**
 * Diagnostics logs screen.
 */
@Composable
fun DiagnosticsScreen(
    state: DiagnosticsUiState,
    onBack: () -> Unit,
    onClear: () -> Unit,
) {
    ScreenScaffold(
        title = "Diagnostics",
        subtitle = "Journal local pour debugger sans guesswork",
        onBack = onBack,
    ) { paddingValues ->
        LazyColumn(
            contentPadding = screenContentPadding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                InlineBanner(
                    title = "Bon usage",
                    body = "Consultez cet ecran juste apres un probleme de capture, puis partagez les informations pertinentes a l'agent qui reprend le projet.",
                    accentColor = DeepTeal,
                )
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClear,
                ) {
                    Text(text = "Effacer les logs")
                }
            }

            state.statusMessage?.let { message ->
                item {
                    InlineBanner(
                        title = "Statut",
                        body = message,
                        accentColor = Clay,
                    )
                }
            }

            if (state.logs.isEmpty()) {
                item {
                    EmptyState(
                        title = "Aucun log local",
                        body = "Les evenements de service et d'enregistrement apparaitront ici quand les diagnostics sont actives.",
                    )
                }
            } else {
                items(state.logs, key = { log -> log.id }) { log ->
                    val accentColor = when (log.level) {
                        DiagnosticLevel.DEBUG -> Clay
                        DiagnosticLevel.INFO -> DeepTeal
                        DiagnosticLevel.WARNING -> Moss
                        DiagnosticLevel.ERROR -> Danger
                    }
                    SectionCard(
                        title = "${log.level.name} - ${log.tag}",
                        subtitle = formatDateTime(log.timestampMillis),
                        accentColor = accentColor,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = log.message, style = MaterialTheme.typography.bodyLarge)
                            log.details?.let { details ->
                                Text(
                                    text = details,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBack,
                ) {
                    Text(text = "Retour")
                }
            }
        }
    }
}
