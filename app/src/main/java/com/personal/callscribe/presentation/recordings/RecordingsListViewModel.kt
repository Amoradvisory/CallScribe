package com.personal.callscribe.presentation.recordings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.callscribe.di.AppContainer
import com.personal.callscribe.domain.model.Recording
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel for the recordings history screen.
 */
class RecordingsListViewModel(
    container: AppContainer,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(RecordingsListUiState(isLoading = true))
    val uiState: StateFlow<RecordingsListUiState> = mutableUiState.asStateFlow()

    init {
        viewModelScope.launch {
            container.getRecordingsUseCase().collectLatest { recordings ->
                mutableUiState.value = RecordingsListUiState(
                    isLoading = false,
                    recordings = recordings,
                )
            }
        }
    }
}

data class RecordingsListUiState(
    val isLoading: Boolean = false,
    val recordings: List<Recording> = emptyList(),
)
