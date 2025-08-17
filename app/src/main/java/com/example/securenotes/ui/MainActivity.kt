package com.example.securenotes.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.example.securenotes.DependencyContainer
import com.example.securenotes.ui.theme.SecureNotesApp
import com.example.securenotes.ui.theme.SecureNotesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val dependencyContainer = remember { DependencyContainer(applicationContext) }
            val settingsViewModel = dependencyContainer.settingsViewModel
            val settingsState by settingsViewModel.settingsState.collectAsState()

            var previousDarkMode by remember { mutableStateOf(settingsState.darkMode) }

            LaunchedEffect(settingsState.darkMode) {
                if (previousDarkMode != settingsState.darkMode) {
                    previousDarkMode = settingsState.darkMode

                    val window = this@MainActivity.window
                    val decorView = window.decorView

                    window.statusBarColor = Color.Transparent.toArgb()
                    window.navigationBarColor = Color.Transparent.toArgb()

                    WindowCompat.getInsetsController(window, decorView).apply {
                        isAppearanceLightStatusBars = !settingsState.darkMode
                        isAppearanceLightNavigationBars = !settingsState.darkMode
                    }
                }
            }

            SecureNotesTheme(darkTheme = settingsState.darkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SecureNotesApp(dependencyContainer)
                }
            }
        }
    }
}