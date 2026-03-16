package com.personal.callscribe.domain.model

/**
 * Normalized call state exposed to the presentation layer.
 */
enum class CallState {
    IDLE,
    RINGING,
    OFF_HOOK,
    UNKNOWN,
}
