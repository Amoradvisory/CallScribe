package com.personal.callscribe.service

import android.content.Context
import androidx.core.content.ContextCompat
import com.personal.callscribe.domain.model.CallState
import com.personal.callscribe.domain.model.DiagnosticLevel
import com.personal.callscribe.domain.model.SessionState
import com.personal.callscribe.domain.repository.IDiagnosticsRepository
import com.personal.callscribe.domain.repository.ISettingsRepository
import com.personal.callscribe.domain.phone.IRecordingSessionController
import com.personal.callscribe.util.hasAllRequiredPermissions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Observes call-state and settings changes to optionally auto-start or auto-stop the recording service.
 *
 * This coordinator only works while the process is alive and required permissions are granted.
 */
class AutoRecordingCoordinator(
    private val appContext: Context,
    private val settingsRepository: ISettingsRepository,
    private val diagnosticsRepository: IDiagnosticsRepository,
    private val sessionController: IRecordingSessionController,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var started = false
    private var autoManagedActiveSession = false

    fun start() {
        if (started) {
            return
        }
        started = true
        scope.launch {
            combine(
                settingsRepository.observeSettings(),
                sessionController.callState,
                sessionController.sessionState,
            ) { settings, callState, sessionState ->
                Triple(settings, callState, sessionState)
            }.collect { (settings, callState, sessionState) ->
                if (!settings.autoStartOnCall) {
                    autoManagedActiveSession = false
                    return@collect
                }
                if (!appContext.hasAllRequiredPermissions()) {
                    log(
                        DiagnosticLevel.WARNING,
                        "AutoRecording",
                        "Auto-start skipped because required permissions are missing.",
                    )
                    return@collect
                }
                when {
                    callState == CallState.OFF_HOOK && sessionState == SessionState.Idle -> {
                        try {
                            ContextCompat.startForegroundService(
                                appContext,
                                RecordingForegroundService.createStartIntent(appContext),
                            )
                            autoManagedActiveSession = true
                            log(
                                DiagnosticLevel.INFO,
                                "AutoRecording",
                                "Foreground service auto-start requested on OFF_HOOK.",
                            )
                        } catch (throwable: Throwable) {
                            log(
                                DiagnosticLevel.ERROR,
                                "AutoRecording",
                                "Auto-start request failed.",
                                throwable.message,
                            )
                        }
                    }

                    autoManagedActiveSession &&
                        callState == CallState.IDLE &&
                        sessionState is SessionState.Recording -> {
                        try {
                            appContext.startService(RecordingForegroundService.createStopIntent(appContext))
                            autoManagedActiveSession = false
                            log(
                                DiagnosticLevel.INFO,
                                "AutoRecording",
                                "Foreground service auto-stop requested on CALL_STATE_IDLE.",
                            )
                        } catch (throwable: Throwable) {
                            log(
                                DiagnosticLevel.ERROR,
                                "AutoRecording",
                                "Auto-stop request failed.",
                                throwable.message,
                            )
                        }
                    }

                    sessionState is SessionState.Completed || sessionState is SessionState.Error -> {
                        autoManagedActiveSession = false
                    }
                }
            }
        }
    }

    private suspend fun log(
        level: DiagnosticLevel,
        tag: String,
        message: String,
        details: String? = null,
    ) {
        diagnosticsRepository.log(level, tag, message, details)
    }
}
