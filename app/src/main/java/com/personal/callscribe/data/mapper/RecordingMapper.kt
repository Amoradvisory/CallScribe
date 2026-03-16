package com.personal.callscribe.data.mapper

import com.personal.callscribe.data.local.db.entity.RecordingEntity
import com.personal.callscribe.domain.model.Recording

/**
 * Maps recording entities to domain models and back.
 */
object RecordingMapper {
    fun toDomain(entity: RecordingEntity): Recording = Recording(
        id = entity.id,
        title = entity.title,
        fileName = entity.fileName,
        filePath = entity.filePath,
        durationMs = entity.durationMs,
        sizeBytes = entity.sizeBytes,
        createdAtMillis = entity.createdAtMillis,
        sampleRateHz = entity.sampleRateHz,
        channelCount = entity.channelCount,
        speakerphoneRequested = entity.speakerphoneRequested,
        speakerphoneActivated = entity.speakerphoneActivated,
    )

    fun toEntity(model: Recording): RecordingEntity = RecordingEntity(
        id = model.id,
        title = model.title,
        fileName = model.fileName,
        filePath = model.filePath,
        durationMs = model.durationMs,
        sizeBytes = model.sizeBytes,
        createdAtMillis = model.createdAtMillis,
        sampleRateHz = model.sampleRateHz,
        channelCount = model.channelCount,
        speakerphoneRequested = model.speakerphoneRequested,
        speakerphoneActivated = model.speakerphoneActivated,
    )
}
