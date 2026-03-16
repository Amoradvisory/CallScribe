package com.personal.callscribe.presentation.common

import com.personal.callscribe.domain.error.AppError

/**
 * Generic asynchronous UI state wrapper.
 */
sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val error: AppError) : UiState<Nothing>
}
