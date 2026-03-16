package com.personal.callscribe.domain.phone

import com.personal.callscribe.domain.error.AppResult

/**
 * Controls speakerphone mode and exposes the actual resulting state.
 */
interface ISpeakerphoneController {
    suspend fun requestSpeakerphone(enabled: Boolean): AppResult<Boolean>
    fun isSpeakerphoneOn(): Boolean
}
