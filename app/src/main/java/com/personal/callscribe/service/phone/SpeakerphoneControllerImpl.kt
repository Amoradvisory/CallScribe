package com.personal.callscribe.service.phone

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.personal.callscribe.domain.error.AppError
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.phone.ISpeakerphoneController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Speakerphone controller backed by Android AudioManager.
 */
@Suppress("DEPRECATION")
class SpeakerphoneControllerImpl(
    context: Context,
) : ISpeakerphoneController {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    override suspend fun requestSpeakerphone(enabled: Boolean): AppResult<Boolean> =
        withContext(Dispatchers.Main.immediate) {
            return@withContext try {
                audioManager.mode = if (enabled) {
                    AudioManager.MODE_IN_COMMUNICATION
                } else {
                    AudioManager.MODE_NORMAL
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (enabled) {
                        val speakerDevice = audioManager.availableCommunicationDevices.firstOrNull { device ->
                            device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                        }
                        if (speakerDevice != null) {
                            audioManager.setCommunicationDevice(speakerDevice)
                        }
                    } else {
                        audioManager.clearCommunicationDevice()
                    }
                } else {
                    audioManager.isSpeakerphoneOn = enabled
                }
                val actual = isSpeakerphoneOn()
                if (enabled && !actual) {
                    AppResult.Failure(AppError.SpeakerphoneActivationFailed)
                } else {
                    AppResult.Success(actual)
                }
            } catch (throwable: Throwable) {
                AppResult.Failure(AppError.Unexpected(throwable))
            }
        }

    override fun isSpeakerphoneOn(): Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        audioManager.communicationDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
    } else {
        audioManager.isSpeakerphoneOn
    }
}
