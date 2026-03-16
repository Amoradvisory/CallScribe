package com.personal.callscribe.presentation.calibration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.callscribe.presentation.common.InlineBanner
import com.personal.callscribe.presentation.common.InfoRow
import com.personal.callscribe.presentation.common.ScreenScaffold
import com.personal.callscribe.presentation.common.SectionCard
import com.personal.callscribe.presentation.common.screenContentPadding
import com.personal.callscribe.presentation.theme.Clay
import com.personal.callscribe.presentation.theme.Danger
import com.personal.callscribe.presentation.theme.Moss

/**
 * Calibration assistant screen.
 */
@Composable
fun CalibrationScreen(
    state: CalibrationUiState,
    onBack: () -> Unit,
    onRunCalibration: () -> Unit,
) {
    ScreenScaffold(
        title = "Calibration",
        subtitle = "Mesurez rapidement l'environnement avant un vrai appel",
        onBack = onBack,
    ) { paddingValues ->
        LazyColumn(
            contentPadding = screenContentPadding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                InlineBanner(
                    title = "Conseil de test",
                    body = "Placez le telephone comme pendant un vrai appel haut-parleur. Evitez la piece trop bruyante.",
                    accentColor = if (state.isRunning) Clay else Moss,
                )
            }

            item {
                SectionCard(
                    title = "Assistant micro",
                    subtitle = "Une courte mesure suffit pour estimer le bon niveau",
                    accentColor = Clay,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "La calibration lit le niveau du micro pendant quelques instants et propose un gain logiciel adapte.",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onRunCalibration,
                            enabled = !state.isRunning,
                        ) {
                            Text(text = if (state.isRunning) "Analyse en cours..." else "Demarrer la calibration")
                        }
                    }
                }
            }

            state.result?.let { result ->
                item {
                    SectionCard(
                        title = "Resultat",
                        subtitle = "Base de depart pour votre prochain essai",
                        accentColor = Moss,
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            InfoRow(label = "Niveau moyen", value = "${"%.1f".format(result.averageDecibels)} dB")
                            InfoRow(label = "Gain conseille", value = "${result.recommendedGainPercent}%")
                            InfoRow(label = "Volume HP conseille", value = "${result.recommendedSpeakerVolumePercent}%")
                            Text(
                                text = result.recommendation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            state.errorMessage?.let { message ->
                item {
                    InlineBanner(
                        title = "Calibration interrompue",
                        body = message,
                        accentColor = Danger,
                    )
                }
            }
        }
    }
}
