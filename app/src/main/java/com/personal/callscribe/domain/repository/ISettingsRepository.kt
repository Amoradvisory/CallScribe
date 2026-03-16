package com.personal.callscribe.domain.repository

import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Contract for persisted application settings.
 */
interface ISettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun getSettings(): AppResult<AppSettings>
    suspend fun saveSettings(settings: AppSettings): AppResult<Unit>
}
