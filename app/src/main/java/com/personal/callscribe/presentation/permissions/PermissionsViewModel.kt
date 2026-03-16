package com.personal.callscribe.presentation.permissions

import android.content.Context
import androidx.lifecycle.ViewModel
import com.personal.callscribe.util.hasPermission
import com.personal.callscribe.util.requiredRuntimePermissions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for runtime permissions onboarding.
 */
class PermissionsViewModel(
    context: Context,
) : ViewModel() {
    private val appContext = context.applicationContext
    private val mutableUiState = MutableStateFlow(buildState())

    val uiState: StateFlow<PermissionsUiState> = mutableUiState.asStateFlow()

    fun refreshPermissions() {
        mutableUiState.value = buildState()
    }

    private fun buildState(): PermissionsUiState {
        val permissions = requiredRuntimePermissions().map { permission ->
            PermissionStatus(
                permission = permission,
                granted = appContext.hasPermission(permission),
            )
        }
        return PermissionsUiState(
            permissions = permissions,
            allGranted = permissions.all(PermissionStatus::granted),
        )
    }
}

data class PermissionStatus(
    val permission: String,
    val granted: Boolean,
)

data class PermissionsUiState(
    val permissions: List<PermissionStatus> = emptyList(),
    val allGranted: Boolean = false,
)
