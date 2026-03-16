package com.personal.callscribe.domain.phone

import com.personal.callscribe.domain.model.CallState
import kotlinx.coroutines.flow.StateFlow

/**
 * Monitors telephony state changes when the platform allows it.
 */
interface ICallStateMonitor {
    val callState: StateFlow<CallState>
    fun start()
    fun stop()
}
