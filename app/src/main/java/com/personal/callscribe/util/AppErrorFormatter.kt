package com.personal.callscribe.util

import com.personal.callscribe.domain.error.AppError

/**
 * Maps domain errors to short user-facing messages.
 */
fun AppError.toUserMessage(): String = when (this) {
    is AppError.PermissionDenied -> "${permissionLabel(permission)} refusee."
    is AppError.PermissionPermanentlyDenied -> "${permissionLabel(permission)} bloquee dans Android."
    AppError.MicrophoneUnavailable -> "Le microphone n'est pas disponible."
    AppError.AudioRecordInitFailed -> "Impossible d'initialiser la capture audio."
    is AppError.AudioRecordError -> "La capture audio a echoue (code $errorCode)."
    AppError.AudioSessionAlreadyActive -> "Une session d'enregistrement est deja en cours."
    AppError.SpeakerphoneActivationFailed -> "Le haut-parleur n'a pas pu etre active."
    AppError.StorageFull -> "L'espace de stockage semble plein."
    AppError.StorageUnavailable -> "Le stockage de l'application est indisponible."
    is AppError.FileWriteError -> "Impossible d'ecrire le fichier audio."
    is AppError.FileReadError -> "Impossible de relire le fichier enregistre."
    AppError.FileNotFound -> "Le fichier demande est introuvable."
    AppError.CallStateUnavailable -> "L'etat d'appel n'est pas disponible."
    is AppError.Unexpected -> cause.message ?: "Une erreur inattendue est survenue."
}
