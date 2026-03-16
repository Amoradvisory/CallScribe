package com.personal.callscribe.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.personal.callscribe.di.AppContainer
import com.personal.callscribe.presentation.calibration.CalibrationScreen
import com.personal.callscribe.presentation.calibration.CalibrationViewModel
import com.personal.callscribe.presentation.common.CallScribeViewModelFactory
import com.personal.callscribe.presentation.detail.RecordingDetailScreen
import com.personal.callscribe.presentation.detail.RecordingDetailViewModel
import com.personal.callscribe.presentation.diagnostics.DiagnosticsScreen
import com.personal.callscribe.presentation.diagnostics.DiagnosticsViewModel
import com.personal.callscribe.presentation.home.HomeScreen
import com.personal.callscribe.presentation.home.HomeViewModel
import com.personal.callscribe.presentation.permissions.PermissionsScreen
import com.personal.callscribe.presentation.permissions.PermissionsViewModel
import com.personal.callscribe.presentation.recordings.RecordingsListScreen
import com.personal.callscribe.presentation.recordings.RecordingsListViewModel
import com.personal.callscribe.presentation.settings.SettingsScreen
import com.personal.callscribe.presentation.settings.SettingsViewModel

/**
 * Navigation graph for all app screens.
 */
@Composable
fun CallScribeNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Routes.Home,
    ) {
        composable(Routes.Home) {
            val viewModel: HomeViewModel = viewModel(
                factory = CallScribeViewModelFactory { HomeViewModel(container.applicationContext, container) },
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            HomeScreen(
                state = state,
                onRefreshPermissions = viewModel::refreshPermissions,
                onStartRecording = viewModel::startRecording,
                onStopRecording = viewModel::stopRecording,
                onOpenPermissions = { navController.navigate(Routes.Permissions) },
                onOpenRecordings = { navController.navigate(Routes.Recordings) },
                onOpenSettings = { navController.navigate(Routes.Settings) },
                onOpenCalibration = { navController.navigate(Routes.Calibration) },
                onOpenDiagnostics = { navController.navigate(Routes.Diagnostics) },
                onOpenRecordingDetail = { recordingId -> navController.navigate(Routes.recordingDetail(recordingId)) },
            )
        }

        composable(Routes.Permissions) {
            val viewModel: PermissionsViewModel = viewModel(
                factory = CallScribeViewModelFactory { PermissionsViewModel(container.applicationContext) },
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            PermissionsScreen(
                state = state,
                onPermissionsResult = viewModel::refreshPermissions,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.Recordings) {
            val viewModel: RecordingsListViewModel = viewModel(
                factory = CallScribeViewModelFactory { RecordingsListViewModel(container) },
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            RecordingsListScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onOpenDetail = { recordingId -> navController.navigate(Routes.recordingDetail(recordingId)) },
            )
        }

        composable(
            route = Routes.RecordingDetail,
            arguments = listOf(navArgument("recordingId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val recordingId = backStackEntry.arguments?.getLong("recordingId") ?: 0L
            val viewModel: RecordingDetailViewModel = viewModel(
                factory = CallScribeViewModelFactory { RecordingDetailViewModel(recordingId, container) },
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            RecordingDetailScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onRename = viewModel::renameRecording,
                onDelete = viewModel::deleteRecording,
                onExportResolved = viewModel::consumeExportPath,
                onRequestExport = viewModel::requestExport,
            )
            if (state.deleted) {
                navController.popBackStack()
            }
        }

        composable(Routes.Settings) {
            val viewModel: SettingsViewModel = viewModel(
                factory = CallScribeViewModelFactory { SettingsViewModel(container) },
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            SettingsScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onSpeakerphoneChanged = viewModel::updateSpeakerphone,
                onAutoStartChanged = viewModel::updateAutoStart,
                onKeepScreenOnChanged = viewModel::updateKeepScreenOn,
                onDiagnosticsChanged = viewModel::updateDiagnostics,
                onGainChanged = viewModel::updateGainPercent,
                onFilePrefixChanged = viewModel::updateFilePrefix,
            )
        }

        composable(Routes.Calibration) {
            val viewModel: CalibrationViewModel = viewModel(
                factory = CallScribeViewModelFactory { CalibrationViewModel(container) },
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            CalibrationScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onRunCalibration = viewModel::runCalibration,
            )
        }

        composable(Routes.Diagnostics) {
            val viewModel: DiagnosticsViewModel = viewModel(
                factory = CallScribeViewModelFactory { DiagnosticsViewModel(container) },
            )
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            DiagnosticsScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onClear = viewModel::clearLogs,
            )
        }
    }
}

/**
 * Navigation routes used by the app.
 */
object Routes {
    const val Home = "home"
    const val Permissions = "permissions"
    const val Recordings = "recordings"
    const val RecordingDetail = "recording/{recordingId}"
    const val Settings = "settings"
    const val Calibration = "calibration"
    const val Diagnostics = "diagnostics"

    fun recordingDetail(recordingId: Long): String = "recording/$recordingId"
}
