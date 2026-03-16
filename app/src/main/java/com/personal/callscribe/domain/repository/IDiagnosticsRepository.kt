package com.personal.callscribe.domain.repository

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.DiagnosticLevel
import com.personal.callscribe.domain.model.DiagnosticLog
import kotlinx.coroutines.flow.Flow

/**
 * Contract for structured diagnostics storage.
 */
interface IDiagnosticsRepository {
    fun observeLogs(limit: Int = 200): Flow<List<DiagnosticLog>>
    suspend fun log(
        level: DiagnosticLevel,
        tag: String,
        message: String,
        details: String? = null,
    ): AppResult<Unit>

    suspend fun clear(): AppResult<Unit>
}
