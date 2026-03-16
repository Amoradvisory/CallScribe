package com.personal.callscribe.domain.usecase

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.AppSettings
import com.personal.callscribe.domain.repository.ISettingsRepository

/**
 * Persists the current settings.
 */
class SaveSettingsUseCase(
    private val settingsRepository: ISettingsRepository,
) {
    suspend operator fun invoke(settings: AppSettings): AppResult<Unit> =
        settingsRepository.saveSettings(settings)
}
