package com.example.securenotes.ui.theme

import android.widget.Switch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.jarjarred.org.antlr.v4.codegen.model.Sync
import com.example.securenotes.viewModel.SettingsViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val settingsState by viewModel.settingsState.collectAsState()
    val passwordDialogState by viewModel.passwordDialogState.collectAsState()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSection(title = "Appearance") {
                    SettingItem(
                        title = "Dark Mode",
                        subtitle = "Enable dark theme",
                        trailing = {
                            Switch(
                                checked = settingsState.darkMode,
                                onCheckedChange = { viewModel.updateDarkMode(it) }
                            )
                        }
                    )

                    HorizontalDivider()

                    SettingItem(
                        title = "Font Size",
                        subtitle = "Adjust text size: ${settingsState.fontSize}sp",
                        trailing = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (settingsState.fontSize > 12) {
                                            viewModel.updateFontSize(settingsState.fontSize - 2)
                                        }
                                    },
                                    enabled = settingsState.fontSize > 12
                                ) {
                                    Icon(Icons.Default.Clear, "Decrease")
                                }

                                Text("${settingsState.fontSize}")

                                IconButton(
                                    onClick = {
                                        if (settingsState.fontSize < 24) {
                                            viewModel.updateFontSize(settingsState.fontSize + 2)
                                        }
                                    },
                                    enabled = settingsState.fontSize < 24
                                ) {
                                    Icon(Icons.Default.Add, "Increase")
                                }
                            }
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Behavior") {
                    SettingItem(
                        title = "Auto Save",
                        subtitle = "Automatically save changes while editing",
                        trailing = {
                            Switch(
                                checked = settingsState.autoSave,
                                onCheckedChange = { viewModel.updateAutoSave(it) }
                            )
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Security") {
                    SettingItem(
                        title = "Change Password",
                        subtitle = "Update password for private notes",
                        onClick = { viewModel.showPasswordDialog() },
                        trailing = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Change password"
                            )
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "Data") {
                    SettingItem(
                        title = "Migrate Preferences",
                        subtitle = "Migrate from old SharedPreferences",
                        onClick = { viewModel.migratePreferences(context) },
                        trailing = {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Migrate"
                            )
                        }
                    )
                }
            }

            item {
                SettingsSection(title = "About") {
                    SettingItem(
                        title = "Version",
                        subtitle = "1.0.0"
                    )

                    HorizontalDivider()

                    SettingItem(
                        title = "Database Version",
                        subtitle = "Schema version 2"
                    )
                }
            }
        }
    }

    // Password Dialog
    PasswordDialog(
        state = passwordDialogState,
        onPasswordSet = { password -> viewModel.setPassword(password) },
        onPasswordVerified = { password -> viewModel.verifyPassword(password) },
        onDismiss = { viewModel.hidePasswordDialog() }
    )
}