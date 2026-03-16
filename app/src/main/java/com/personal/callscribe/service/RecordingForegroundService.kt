package com.personal.callscribe.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.personal.callscribe.CallScribeApp
import com.personal.callscribe.R
import com.personal.callscribe.domain.error.AppResult
import com.personal.callscribe.domain.model.SessionState
import com.personal.callscribe.domain.usecase.StartRecordingSessionUseCase
import com.personal.callscribe.domain.usecase.StopRecordingSessionUseCase
import com.personal.callscribe.presentation.MainActivity
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Foreground service hosting the active recording session notification.
 */
class RecordingForegroundService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var startRecordingSessionUseCase: StartRecordingSessionUseCase
    private lateinit var stopRecordingSessionUseCase: StopRecordingSessionUseCase
    private lateinit var callScribeApp: CallScribeApp

    override fun onCreate() {
        super.onCreate()
        callScribeApp = application as CallScribeApp
        startRecordingSessionUseCase = callScribeApp.container.startRecordingSessionUseCase
        stopRecordingSessionUseCase = callScribeApp.container.stopRecordingSessionUseCase
        createNotificationChannel()
        serviceScope.launch {
            callScribeApp.container.sessionController.sessionState.collectLatest { state ->
                when (state) {
                    is SessionState.Recording,
                    is SessionState.Preparing,
                    is SessionState.EnablingSpeaker,
                    is SessionState.Finalizing,
                    -> notifyState(state)

                    is SessionState.Completed,
                    is SessionState.Error,
                    SessionState.Idle,
                    -> {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(
                    NOTIFICATION_ID,
                    buildNotification(callScribeApp.container.sessionController.sessionState.value),
                )
                serviceScope.launch {
                    when (startRecordingSessionUseCase()) {
                        is AppResult.Success -> Unit
                        is AppResult.Failure -> {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                            stopSelf()
                        }
                    }
                }
            }

            ACTION_STOP -> {
                serviceScope.launch {
                    stopRecordingSessionUseCase()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun notifyState(state: SessionState) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun buildNotification(state: SessionState): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, RecordingForegroundService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val contentText = when (state) {
            is SessionState.Preparing -> "Préparation du microphone"
            is SessionState.EnablingSpeaker -> "Tentative d'activation du haut-parleur"
            is SessionState.Recording -> {
                val seconds = state.elapsedMs / 1000L
                "Enregistrement en cours • ${seconds}s • ${"%.1f".format(Locale.US, state.audioLevel.decibels)} dB"
            }

            is SessionState.Finalizing -> "Finalisation du fichier WAV"
            is SessionState.Completed -> "Enregistrement finalisé"
            is SessionState.Error -> "Erreur d'enregistrement"
            SessionState.Idle -> getString(R.string.notification_text_recording)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle(getString(R.string.notification_title_recording))
            .setContentText(contentText)
            .setContentIntent(openAppIntent)
            .setOngoing(
                state is SessionState.Recording ||
                    state is SessionState.Preparing ||
                    state is SessionState.EnablingSpeaker,
            )
            .addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.notification_action_stop),
                stopIntent,
            )
            .build()
    }

    private fun createNotificationChannel() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "callscribe_recording_channel"
        private const val NOTIFICATION_ID = 4001
        const val ACTION_START = "com.personal.callscribe.action.START_RECORDING"
        const val ACTION_STOP = "com.personal.callscribe.action.STOP_RECORDING"

        fun createStartIntent(context: Context): Intent =
            Intent(context, RecordingForegroundService::class.java).setAction(ACTION_START)

        fun createStopIntent(context: Context): Intent =
            Intent(context, RecordingForegroundService::class.java).setAction(ACTION_STOP)
    }
}
