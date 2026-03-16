package com.personal.callscribe.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Returns the list of runtime permissions required by the app on the current API level.
 */
fun requiredRuntimePermissions(): List<String> = buildList {
    add(Manifest.permission.RECORD_AUDIO)
    add(Manifest.permission.READ_PHONE_STATE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        add(Manifest.permission.POST_NOTIFICATIONS)
    }
}

/**
 * Checks whether the given permission is granted.
 */
fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * Checks whether all required runtime permissions are granted.
 */
fun Context.hasAllRequiredPermissions(): Boolean =
    requiredRuntimePermissions().all { permission -> hasPermission(permission) }

/**
 * Maps runtime permissions to human-readable labels.
 */
fun permissionLabel(permission: String): String = when (permission) {
    Manifest.permission.RECORD_AUDIO -> "Microphone"
    Manifest.permission.READ_PHONE_STATE -> "Etat d'appel"
    Manifest.permission.POST_NOTIFICATIONS -> "Notifications"
    else -> permission.substringAfterLast('.')
}

/**
 * Explains why the app asks for a given permission.
 */
fun permissionDescription(permission: String): String = when (permission) {
    Manifest.permission.RECORD_AUDIO ->
        "Indispensable pour capter le son ambiant pendant l'appel."

    Manifest.permission.READ_PHONE_STATE ->
        "Permet d'afficher l'etat d'appel et de tenter l'auto-demarrage."

    Manifest.permission.POST_NOTIFICATIONS ->
        "Permet d'afficher la notification obligatoire du service d'enregistrement."

    else -> "Permission utile au fonctionnement de l'application."
}
