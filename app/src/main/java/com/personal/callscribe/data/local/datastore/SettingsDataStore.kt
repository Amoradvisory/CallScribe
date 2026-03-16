package com.personal.callscribe.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.personal.callscribe.domain.model.AppSettings
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "callscribe_settings",
)

/**
 * DataStore wrapper for application settings.
 */
class SettingsDataStore(
    private val context: Context,
) {
    private object Keys {
        val SpeakerphoneEnabled = booleanPreferencesKey("speakerphone_enabled")
        val AutoStartOnCall = booleanPreferencesKey("auto_start_on_call")
        val KeepScreenOn = booleanPreferencesKey("keep_screen_on")
        val PreferredSampleRateHz = intPreferencesKey("preferred_sample_rate_hz")
        val InputGainPercent = intPreferencesKey("input_gain_percent")
        val EnableDiagnostics = booleanPreferencesKey("enable_diagnostics")
        val PreferredFilePrefix = stringPreferencesKey("preferred_file_prefix")
    }

    fun observeSettings(): Flow<AppSettings> = context.settingsDataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            AppSettings(
                speakerphoneEnabled = preferences[Keys.SpeakerphoneEnabled] ?: true,
                autoStartOnCall = preferences[Keys.AutoStartOnCall] ?: false,
                keepScreenOn = preferences[Keys.KeepScreenOn] ?: true,
                preferredSampleRateHz = preferences[Keys.PreferredSampleRateHz] ?: 16_000,
                inputGainPercent = preferences[Keys.InputGainPercent] ?: 100,
                enableDiagnostics = preferences[Keys.EnableDiagnostics] ?: true,
                preferredFilePrefix = preferences[Keys.PreferredFilePrefix] ?: "CallScribe",
            )
        }

    suspend fun save(settings: AppSettings) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.SpeakerphoneEnabled] = settings.speakerphoneEnabled
            preferences[Keys.AutoStartOnCall] = settings.autoStartOnCall
            preferences[Keys.KeepScreenOn] = settings.keepScreenOn
            preferences[Keys.PreferredSampleRateHz] = settings.preferredSampleRateHz
            preferences[Keys.InputGainPercent] = settings.inputGainPercent.coerceIn(50, 200)
            preferences[Keys.EnableDiagnostics] = settings.enableDiagnostics
            preferences[Keys.PreferredFilePrefix] = settings.preferredFilePrefix.ifBlank { "CallScribe" }
        }
    }
}
