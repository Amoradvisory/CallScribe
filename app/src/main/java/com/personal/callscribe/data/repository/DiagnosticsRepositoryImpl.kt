package com.personal.callscribe.data.repository

import com.personal.callscribe.data.local.db.DiagnosticLogDao
import com.personal.callscribe.data.local.db.entity.DiagnosticLogEntity
import com.personal.callscribe.data.mapper.DiagnosticMapper
import com.personal.callscribe.domain.error.AppError
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.DiagnosticLevel
import com.personal.callscribe.domain.model.DiagnosticLog
import com.personal.callscribe.domain.repository.IDiagnosticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed diagnostics repository.
 */
class DiagnosticsRepositoryImpl(
    private val diagnosticLogDao: DiagnosticLogDao,
) : IDiagnosticsRepository {

    override fun observeLogs(limit: Int): Flow<List<DiagnosticLog>> =
        diagnosticLogDao.observeLatest(limit).map { entities ->
            entities.map(DiagnosticMapper::toDomain)
        }

    override suspend fun log(
        level: DiagnosticLevel,
        tag: String,
        message: String,
        details: String?,
    ): AppResult<Unit> = try {
        diagnosticLogDao.insert(
            DiagnosticLogEntity(
                timestampMillis = System.currentTimeMillis(),
                level = level.name,
                tag = tag,
                message = message,
                details = details,
            ),
        )
        AppResult.Success(Unit)
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Unexpected(throwable))
    }

    override suspend fun clear(): AppResult<Unit> = try {
        diagnosticLogDao.clear()
        AppResult.Success(Unit)
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Unexpected(throwable))
    }
}
