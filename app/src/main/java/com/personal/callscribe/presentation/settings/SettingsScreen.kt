package com.personal.callscribe.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.callscribe.presentation.common.InlineBanner
import com.personal.callscribe.presentation.common.ScreenScaffold
import com.personal.callscribe.presentation.common.SectionCard
import com.personal.callscribe.presentation.common.SettingToggleRow
import com.personal.callscribe.presentation.common.screenContentPadding
import com.personal.callscribe.presentation.theme.Clay
import com.personal.callscribe.presentation.theme.DeepTeal
import com.personal.callscribe.presentation.theme.Danger
import com.personal.callscribe.presentation.theme.Moss

/**
 * Settings screen.
 */
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onSpeakerphoneChanged: (Boolean) -> Unit,
    onAutoStartChanged: (Boolean) -> Unit,
    onKeepScreenOnChanged: (Boolean) -> Unit,
    onDiagnosticsChanged: (Boolean) -> Unit,
    onGainChanged: (Int) -> Unit,
    onFilePrefixChanged: (String) -> Unit,
) {
    ScreenScaffold(
        title = "Reglages",
        subtitle = "Les changements sont enregistres automatiquement",
        onBack = onBack,
    ) { paddingValues ->
        LazyColumn(
            contentPadding = screenContentPadding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                val accent = when {
                    state.isSaving -> Clay
                    state.statusMessage?.contains("impossible", ignoreCase = true) == true ||
                        state.statusMessage?.contains("erreur", ignoreCase = true) == true -> Danger

                    else -> Moss
                }
                InlineBanner(
                    title = if (state.isSaving) "Synchronisation locale en cours" else "Preferences locales",
                    body = state.statusMessage ?: "Chaque changement est memorise sans bouton Save obligatoire.",
                    accentColor = accent,
                )
            }

            item {
                SectionCard(
                    title = "Capture pendant l'appel",
                    subtitle = "Ce qui influence directement le resultat audio",
                    accentColor = DeepTeal,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        SettingToggleRow(
                            title = "Tenter le haut-parleur",
                            body = "Essaye d'amplifier la voix distante avant la capture micro.",
                            checked = state.settings.speakerphoneEnabled,
                            onCheckedChange = onSpeakerphoneChanged,
                        )
                        SettingToggleRow(
                            title = "Auto-demarrage sur appel",
                            body = "Fonctionne seulement tant que le process reste vivant et que les permissions sont accordees.",
                            checked = state.settings.autoStartOnCall,
                            onCheckedChange = onAutoStartChanged,
                        )
                    }
                }
            }

            item {
                SectionCard(
                    title = "Confort d'usage",
                    subtitle = "Rendre la session plus lisible et plus stable pendant un appel",
                    accentColor = Moss,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                        SettingToggleRow(
                            title = "Garder l'ecran actif",
                            body = "Evite que l'ecran se verrouille pendant un enregistrement manuel.",
                            checked = state.settings.keepScreenOn,
                            onCheckedChange = onKeepScreenOnChanged,
                        )
                        SettingToggleRow(
                            title = "Conserver les diagnostics",
                            body = "Enregistre les evenements techniques pour comprendre un comportement anormal.",
                            checked = state.settings.enableDiagnostics,
                            onCheckedChange = onDiagnosticsChanged,
                        )
                    }
                }
            }

            item {
                SectionCard(
                    title = "Signal et fichiers",
                    subtitle = "Ce qui impacte le volume percu et la lisibilite des exports",
                    accentColor = Clay,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "Gain logiciel: ${state.settings.inputGainPercent}%",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Slider(
                            value = state.settings.inputGainPercent.toFloat(),
                            onValueChange = { onGainChanged(it.toInt()) },
                            valueRange = 50f..200f,
                        )
                        Text(
                            text = "Laissez 100% par defaut. Montez seulement si les captures restent trop faibles.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.settings.preferredFilePrefix,
                            onValueChange = onFilePrefixChanged,
                            label = { Text(text = "Prefixe de fichier") },
                            supportingText = {
                                Text(text = "Exemple: CallScribe, ClientA, ReunionTel")
                            },
                            singleLine = true,
                        )
                    }
                }
            }

            item {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBack,
                ) {
                    Text(text = "Retour au tableau de bord")
                }
            }
        }
    }
}
