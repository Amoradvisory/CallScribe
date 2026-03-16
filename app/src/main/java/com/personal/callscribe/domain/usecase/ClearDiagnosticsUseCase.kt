package com.personal.callscribe.domain.usecase

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.repository.IDiagnosticsRepository

/**
 * Clears stored diagnostic logs.
 */
class ClearDiagnosticsUseCase(
    private val diagnosticsRepository: IDiagnosticsRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = diagnosticsRepository.clear()
}
