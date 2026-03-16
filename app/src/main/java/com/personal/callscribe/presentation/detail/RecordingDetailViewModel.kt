package com.personal.callscribe.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.callscribe.di.AppContainer
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.Recording
import com.personal.callscribe.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for one recording detail screen.
 */
class RecordingDetailViewModel(
    private val recordingId: Long,
    private val container: AppContainer,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(RecordingDetailUiState(isLoading = true))
    val uiState: StateFlow<RecordingDetailUiState> = mutableUiState.asStateFlow()

    init {
        load()
    }

    fun renameRecording(newTitle: String) {
        val sanitized = newTitle.trim()
        if (sanitized.isBlank()) {
            mutableUiState.value = mutableUiState.value.copy(errorMessage = "Le titre ne peut pas être vide.")
            return
        }
        viewModelScope.launch {
            when (val result = container.renameRecordingUseCase(recordingId, sanitized)) {
                is AppResult.Success -> load()
                is AppResult.Failure -> {
                    mutableUiState.value = mutableUiState.value.copy(errorMessage = result.error.toUserMessage())
                }
            }
        }
    }

    fun deleteRecording() {
        viewModelScope.launch {
            when (val result = container.deleteRecordingUseCase(recordingId)) {
                is AppResult.Success -> {
                    mutableUiState.value = mutableUiState.value.copy(deleted = true)
                }

                is AppResult.Failure -> {
                    mutableUiState.value = mutableUiState.value.copy(errorMessage = result.error.toUserMessage())
                }
            }
        }
    }

    fun requestExport() {
        viewModelScope.launch {
            when (val result = container.exportRecordingUseCase(recordingId)) {
                is AppResult.Success -> {
                    mutableUiState.value = mutableUiState.value.copy(pendingExportPath = result.data)
                }

                is AppResult.Failure -> {
                    mutableUiState.value = mutableUiState.value.copy(errorMessage = result.error.toUserMessage())
                }
            }
        }
    }

    fun consumeExportPath() {
        mutableUiState.value = mutableUiState.value.copy(pendingExportPath = null)
    }

    private fun load() {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = container.getRecordingDetailsUseCase(recordingId)) {
                is AppResult.Success -> {
                    mutableUiState.value = mutableUiState.value.copy(
                        isLoading = false,
                        recording = result.data,
                    )
                }

                is AppResult.Failure -> {
                    mutableUiState.value = mutableUiState.value.copy(
                        isLoading = false,
                        errorMessage = result.error.toUserMessage(),
                    )
                }
            }
        }
    }
}

data class RecordingDetailUiState(
    val isLoading: Boolean = false,
    val recording: Recording? = null,
    val errorMessage: String? = null,
    val pendingExportPath: String? = null,
    val deleted: Boolean = false,
)
