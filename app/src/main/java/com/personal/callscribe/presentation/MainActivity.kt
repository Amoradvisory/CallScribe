package com.personal.callscribe.presentation

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.personal.callscribe.CallScribeApp
import com.personal.callscribe.presentation.navigation.CallScribeNavGraph
import com.personal.callscribe.presentation.theme.CallScribeTheme

/**
 * Single-activity host for the Compose navigation graph.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as CallScribeApp).container
        setContent {
            val settings by container.observeSettingsUseCase().collectAsState(
                initial = com.personal.callscribe.domain.model.AppSettings(),
            )
            LaunchedEffect(settings.keepScreenOn) {
                if (settings.keepScreenOn) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
            CallScribeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CallScribeNavGraph(container = container)
                }
            }
        }
    }
}
