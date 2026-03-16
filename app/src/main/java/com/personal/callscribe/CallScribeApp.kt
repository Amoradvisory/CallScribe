package com.personal.callscribe

import android.app.Application
import com.personal.callscribe.di.AppContainer

/**
 * Application entry point creating the manual dependency container.
 */
class CallScribeApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.callStateMonitor.start()
        container.autoRecordingCoordinator.start()
    }
}
