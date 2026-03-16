package com.personal.callscribe.presentation.calibration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.callscribe.di.AppContainer
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.CalibrationResult
import com.personal.callscribe.util.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the calibration assistant.
 */
class CalibrationViewModel(
    private val container: AppContainer,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(CalibrationUiState())
    val uiState: StateFlow<CalibrationUiState> = mutableUiState.asStateFlow()

    fun runCalibration() {
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(isRunning = true, errorMessage = null)
            when (val result = container.runCalibrationUseCase()) {
                is AppResult.Success -> {
                    mutableUiState.value = mutableUiState.value.copy(
                        isRunning = false,
                        result = result.data,
                    )
                }

                is AppResult.Failure -> {
                    mutableUiState.value = mutableUiState.value.copy(
                        isRunning = false,
                        errorMessage = result.error.toUserMessage(),
                    )
                }
            }
        }
    }
}

data class CalibrationUiState(
    val isRunning: Boolean = false,
    val result: CalibrationResult? = null,
    val errorMessage: String? = null,
)
