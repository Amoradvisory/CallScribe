package com.personal.callscribe.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.callscribe.di.AppContainer
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.AppSettings
import com.personal.callscribe.util.toUserMessage
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/**
 * ViewModel for persisted app settings.
 */
@OptIn(FlowPreview::class)
class SettingsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(SettingsUiState())
    private val pendingSaves = MutableSharedFlow<AppSettings>(extraBufferCapacity = 1)

    val uiState: StateFlow<SettingsUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.observeSettingsUseCase().collectLatest { settings ->
                mutableUiState.value = mutableUiState.value.copy(
                    settings = settings,
                    hasLoaded = true,
                )
            }
        }
        viewModelScope.launch {
            pendingSaves.debounce(300L).collectLatest { settings ->
                mutableUiState.value = mutableUiState.value.copy(
                    isSaving = true,
                    statusMessage = "Enregistrement...",
                )
                when (val result = container.saveSettingsUseCase(settings)) {
                    is AppResult.Success -> {
                        mutableUiState.value = mutableUiState.value.copy(
                            isSaving = false,
                            statusMessage = "Preferences enregistrees.",
                        )
                    }

                    is AppResult.Failure -> {
                        mutableUiState.value = mutableUiState.value.copy(
                            isSaving = false,
                            statusMessage = result.error.toUserMessage(),
                        )
                    }
                }
            }
        }
    }

    fun updateSpeakerphone(enabled: Boolean) {
        updateSettings { copy(speakerphoneEnabled = enabled) }
    }

    fun updateAutoStart(enabled: Boolean) {
        updateSettings { copy(autoStartOnCall = enabled) }
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        updateSettings { copy(keepScreenOn = enabled) }
    }

    fun updateDiagnostics(enabled: Boolean) {
        updateSettings { copy(enableDiagnostics = enabled) }
    }

    fun updateGainPercent(gain: Int) {
        updateSettings { copy(inputGainPercent = gain.coerceIn(50, 200)) }
    }

    fun updateFilePrefix(prefix: String) {
        updateSettings { copy(preferredFilePrefix = prefix) }
    }

    fun save() {
        pendingSaves.tryEmit(mutableUiState.value.settings)
    }

    private fun updateSettings(transform: AppSettings.() -> AppSettings) {
        val updatedSettings = mutableUiState.value.settings.transform()
        mutableUiState.value = mutableUiState.value.copy(
            settings = updatedSettings,
            statusMessage = if (mutableUiState.value.hasLoaded) "Modification en attente..." else null,
        )
        if (mutableUiState.value.hasLoaded) {
            pendingSaves.tryEmit(updatedSettings)
        }
    }
}

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val hasLoaded: Boolean = false,
    val isSaving: Boolean = false,
    val statusMessage: String? = null,
)
