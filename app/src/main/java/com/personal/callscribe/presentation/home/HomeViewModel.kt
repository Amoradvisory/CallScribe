package com.personal.callscribe.presentation.home

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.callscribe.di.AppContainer
import com.personal.callscribe.domain.model.AppSettings
import com.personal.callscribe.domain.model.CallState
import com.personal.callscribe.domain.model.Recording
import com.personal.callscribe.domain.model.SessionState
import com.personal.callscribe.service.RecordingForegroundService
import com.personal.callscribe.util.hasAllRequiredPermissions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for the main dashboard screen.
 */
class HomeViewModel(
    context: Context,
    private val container: AppContainer,
) : ViewModel() {
    private val appContext = context.applicationContext
    private val mutableUiState = MutableStateFlow(
        HomeUiState(
            allPermissionsGranted = appContext.hasAllRequiredPermissions(),
        ),
    )

    val uiState: StateFlow<HomeUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.sessionController.sessionState.collectLatest { state ->
                mutableUiState.value = mutableUiState.value.copy(sessionState = state)
            }
        }
        viewModelScope.launch {
            container.sessionController.callState.collectLatest { state ->
                mutableUiState.value = mutableUiState.value.copy(callState = state)
            }
        }
        viewModelScope.launch {
            container.observeSettingsUseCase().collectLatest { settings ->
                mutableUiState.value = mutableUiState.value.copy(settings = settings)
            }
        }
        viewModelScope.launch {
            container.getRecordingsUseCase().collectLatest { recordings ->
                mutableUiState.value = mutableUiState.value.copy(recentRecordings = recordings.take(3))
            }
        }
    }

    fun refreshPermissions() {
        mutableUiState.value = mutableUiState.value.copy(
            allPermissionsGranted = appContext.hasAllRequiredPermissions(),
        )
    }

    fun startRecording() {
        refreshPermissions()
        if (!mutableUiState.value.allPermissionsGranted) {
            return
        }
        ContextCompat.startForegroundService(
            appContext,
            RecordingForegroundService.createStartIntent(appContext),
        )
    }

    fun stopRecording() {
        appContext.startService(RecordingForegroundService.createStopIntent(appContext))
    }
}

data class HomeUiState(
    val sessionState: SessionState = SessionState.Idle,
    val callState: CallState = CallState.UNKNOWN,
    val settings: AppSettings = AppSettings(),
    val allPermissionsGranted: Boolean = false,
    val recentRecordings: List<Recording> = emptyList(),
)
