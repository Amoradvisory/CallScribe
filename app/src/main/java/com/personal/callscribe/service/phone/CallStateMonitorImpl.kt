package com.personal.callscribe.service.phone

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import com.personal.callscribe.domain.model.CallState
import com.personal.callscribe.domain.phone.ICallStateMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * TelephonyManager-backed call-state monitor.
 */
@Suppress("DEPRECATION")
class CallStateMonitorImpl(
    context: Context,
) : ICallStateMonitor {
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private val appContext = context.applicationContext
    private val mutableCallState = MutableStateFlow(CallState.UNKNOWN)
    private var isStarted = false

    @Suppress("DEPRECATION")
    private val legacyListener = object : PhoneStateListener() {
        @Suppress("DEPRECATION")
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            mutableCallState.value = state.toCallState()
        }
    }

    private val telephonyCallback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        object : TelephonyCallback(), TelephonyCallback.CallStateListener {
            override fun onCallStateChanged(state: Int) {
                mutableCallState.value = state.toCallState()
            }
        }
    } else {
        null
    }

    override val callState: StateFlow<CallState> = mutableCallState.asStateFlow()

    @SuppressLint("MissingPermission")
    override fun start() {
        if (isStarted) {
            return
        }
        try {
            mutableCallState.value = telephonyManager.callState.toCallState()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && telephonyCallback != null) {
                telephonyManager.registerTelephonyCallback(appContext.mainExecutor, telephonyCallback)
            } else {
                @Suppress("DEPRECATION")
                telephonyManager.listen(legacyListener, PhoneStateListener.LISTEN_CALL_STATE)
            }
            isStarted = true
        } catch (_: SecurityException) {
            mutableCallState.value = CallState.UNKNOWN
        }
    }

    override fun stop() {
        if (!isStarted) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && telephonyCallback != null) {
            telephonyManager.unregisterTelephonyCallback(telephonyCallback)
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.listen(legacyListener, PhoneStateListener.LISTEN_NONE)
        }
        isStarted = false
    }

    private fun Int.toCallState(): CallState = when (this) {
        TelephonyManager.CALL_STATE_IDLE -> CallState.IDLE
        TelephonyManager.CALL_STATE_RINGING -> CallState.RINGING
        TelephonyManager.CALL_STATE_OFFHOOK -> CallState.OFF_HOOK
        else -> CallState.UNKNOWN
    }
}
