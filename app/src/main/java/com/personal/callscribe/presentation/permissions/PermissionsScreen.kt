package com.personal.callscribe.presentation.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.personal.callscribe.presentation.common.InlineBanner
import com.personal.callscribe.presentation.common.ScreenScaffold
import com.personal.callscribe.presentation.common.SectionCard
import com.personal.callscribe.presentation.common.StatusBadge
import com.personal.callscribe.presentation.common.screenContentPadding
import com.personal.callscribe.presentation.theme.Danger
import com.personal.callscribe.presentation.theme.Moss
import com.personal.callscribe.util.permissionDescription
import com.personal.callscribe.util.permissionLabel

/**
 * Permissions onboarding screen.
 */
@Composable
fun PermissionsScreen(
    state: PermissionsUiState,
    onPermissionsResult: () -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        onPermissionsResult()
    }

    LifecycleResumeEffect(Unit) {
        onPermissionsResult()
        onPauseOrDispose { }
    }

    ScreenScaffold(
        title = "Permissions",
        subtitle = "Rendez le premier demarrage clair et sans blocage",
        onBack = onBack,
    ) { paddingValues ->
        LazyColumn(
            contentPadding = screenContentPadding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                if (state.allGranted) {
                    InlineBanner(
                        title = "Tout est pret",
                        body = "Les permissions critiques sont deja accordees. Vous pouvez revenir au tableau de bord.",
                        accentColor = Moss,
                    )
                } else {
                    InlineBanner(
                        title = "Action requise avant la premiere capture",
                        body = "Accordez les permissions ci-dessous. Si Android les bloque, ouvrez directement les parametres de l'application.",
                        accentColor = Danger,
                    )
                }
            }

            item {
                SectionCard(
                    title = "Ce que fait l'application",
                    subtitle = "Aucune API distante, aucun compte, aucun upload",
                ) {
                    Text(
                        text = "CallScribe enregistre localement le micro, surveille l'etat d'appel et maintient un service visible pendant la capture.",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            items(state.permissions, key = { permission -> permission.permission }) { permission ->
                SectionCard(
                    title = permissionLabel(permission.permission),
                    subtitle = if (permission.granted) "Accordee" else "Manquante",
                    accentColor = if (permission.granted) Moss else Danger,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = permissionDescription(permission.permission),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            StatusBadge(
                                text = if (permission.granted) "OK" else "A regler",
                                color = if (permission.granted) Moss else Danger,
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            launcher.launch(state.permissions.map(PermissionStatus::permission).toTypedArray())
                        },
                    ) {
                        Text(text = if (state.allGranted) "Reverifier" else "Demander maintenant")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                    ) {
                        Text(text = "Parametres Android")
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
