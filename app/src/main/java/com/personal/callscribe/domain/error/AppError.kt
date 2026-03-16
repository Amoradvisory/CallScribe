package com.personal.callscribe.domain.error

/**
 * Exhaustive error taxonomy used by the application.
 */
sealed class AppError {
    data class PermissionDenied(val permission: String) : AppError()
    data class PermissionPermanentlyDenied(val permission: String) : AppError()
    data object MicrophoneUnavailable : AppError()
    data object AudioRecordInitFailed : AppError()
    data class AudioRecordError(val errorCode: Int) : AppError()
    data object AudioSessionAlreadyActive : AppError()
    data object SpeakerphoneActivationFailed : AppError()
    data object StorageFull : AppError()
    data object StorageUnavailable : AppError()
    data class FileWriteError(val cause: Throwable) : AppError()
    data class FileReadError(val path: String) : AppError()
    data object FileNotFound : AppError()
    data object CallStateUnavailable : AppError()
    data class Unexpected(val cause: Throwable) : AppError()
}
