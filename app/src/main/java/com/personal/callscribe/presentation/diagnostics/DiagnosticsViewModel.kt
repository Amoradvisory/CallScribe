package com.personal.callscribe.presentation.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.callscribe.di.AppContainer
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.DiagnosticLog
import com.personal.callscribe.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for diagnostics logs.
 */
class DiagnosticsViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(DiagnosticsUiState())
    val uiState: StateFlow<DiagnosticsUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.getDiagnosticsUseCase().collectLatest { logs ->
                mutableUiState.value = mutableUiState.value.copy(logs = logs)
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            when (val result = container.clearDiagnosticsUseCase()) {
                is AppResult.Success -> {
                    mutableUiState.value = mutableUiState.value.copy(statusMessage = "Logs supprimés.")
                }

                is AppResult.Failure -> {
                    mutableUiState.value = mutableUiState.value.copy(statusMessage = result.error.toUserMessage())
                }
            }
        }
    }
}

data class DiagnosticsUiState(
    val logs: List<DiagnosticLog> = emptyList(),
    val statusMessage: String? = null,
)
