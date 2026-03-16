package com.personal.callscribe.data.mapper

import com.personal.callscribe.data.local.db.entity.DiagnosticLogEntity
import com.personal.callscribe.domain.model.DiagnosticLevel
import com.personal.callscribe.domain.model.DiagnosticLog

/**
 * Maps diagnostics entities to domain models and back.
 */
object DiagnosticMapper {
    fun toDomain(entity: DiagnosticLogEntity): DiagnosticLog = DiagnosticLog(
        id = entity.id,
        timestampMillis = entity.timestampMillis,
        level = entity.level.toDiagnosticLevel(),
        tag = entity.tag,
        message = entity.message,
        details = entity.details,
    )

    fun toEntity(model: DiagnosticLog): DiagnosticLogEntity = DiagnosticLogEntity(
        id = model.id,
        timestampMillis = model.timestampMillis,
        level = model.level.name,
        tag = model.tag,
        message = model.message,
        details = model.details,
    )

    private fun String.toDiagnosticLevel(): DiagnosticLevel =
        runCatching { DiagnosticLevel.valueOf(this) }.getOrDefault(DiagnosticLevel.INFO)
}
