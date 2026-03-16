package com.personal.callscribe.domain.usecase

import com.personal.callscribe.domain.model.DiagnosticLog
import com.personal.callscribe.domain.repository.IDiagnosticsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes diagnostics logs for the diagnostics screen.
 */
class GetDiagnosticsUseCase(
    private val diagnosticsRepository: IDiagnosticsRepository,
) {
    operator fun invoke(limit: Int = 200): Flow<List<DiagnosticLog>> =
        diagnosticsRepository.observeLogs(limit)
}
