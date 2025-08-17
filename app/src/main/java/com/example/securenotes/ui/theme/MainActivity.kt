package com.example.securenotes.ui.theme

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.securenotes.DependencyContainer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // You'll need to create and pass dependencies here
            val dependencyContainer = DependencyContainer(applicationContext)
            val settingsViewModel = dependencyContainer.settingsViewModel
            val settingsState by settingsViewModel.settingsState.collectAsState()

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
