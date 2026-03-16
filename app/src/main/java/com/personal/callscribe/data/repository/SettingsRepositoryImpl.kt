package com.personal.callscribe.data.repository

import com.personal.callscribe.data.local.datastore.SettingsDataStore
import com.personal.callscribe.domain.error.AppError
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.AppSettings
import com.personal.callscribe.domain.repository.ISettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * DataStore-backed settings repository.
 */
class SettingsRepositoryImpl(
    private val settingsDataStore: SettingsDataStore,
) : ISettingsRepository {

    override fun observeSettings(): Flow<AppSettings> = settingsDataStore.observeSettings()

    override suspend fun getSettings(): AppResult<AppSettings> = try {
        AppResult.Success(settingsDataStore.observeSettings().first())
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Unexpected(throwable))
    }

    override suspend fun saveSettings(settings: AppSettings): AppResult<Unit> = try {
        settingsDataStore.save(settings)
        AppResult.Success(Unit)
    } catch (throwable: Throwable) {
        AppResult.Failure(AppError.Unexpected(throwable))
    }
}
