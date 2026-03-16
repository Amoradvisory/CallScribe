package com.personal.callscribe.domain.usecase

import com.personal.callscribe.domain.model.AppSettings
import com.personal.callscribe.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observes settings changes for UI synchronization.
 */
class ObserveSettingsUseCase(
    private val settingsRepository: ISettingsRepository,
) {
    operator fun invoke(): Flow<AppSettings> = settingsRepository.observeSettings()
}
