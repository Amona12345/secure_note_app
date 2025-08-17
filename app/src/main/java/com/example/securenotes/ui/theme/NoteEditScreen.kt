package com.example.securenotes.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.securenotes.UiState
import com.example.securenotes.viewModel.NoteEditViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    noteId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: NoteEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val passwordDialogState by viewModel.passwordDialogState.collectAsState()
    val settingsState by viewModel.settingsState.collectAsState()

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var isPrivate by remember { mutableStateOf(false) }

    val isEditing = noteId != null

    // Load existing note data
    LaunchedEffect(uiState) {
        if (uiState is UiState.Success && (uiState as UiState.Success).data != null) {
            val note = (uiState as UiState.Success).data
            title = note?.title ?: ""
            body = note?.body ?: ""
            isPrivate = note?.isPrivate ?: false
        }
    }

    // Handle save success
    LaunchedEffect(saveState) {
        if (saveState is UiState.Success) {
            onNavigateBack()
        }
    }

    // Auto-save functionality
    LaunchedEffect(title, body, isPrivate, category) {
        if (isEditing && settingsState.autoSave) {
            viewModel.autoSave(title, body, isPrivate, category)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(if (isEditing) "Edit Note" else "Add Note") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        viewModel.saveNote(title, body, isPrivate, category)
                    },
                    enabled = title.isNotBlank() && body.isNotBlank() && saveState !is UiState.Loading
                ) {
                    if (saveState is UiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = settingsState.fontSize.sp)
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = settingsState.fontSize.sp)
            )

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = settingsState.fontSize.sp),
                maxLines = Int.MAX_VALUE
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPrivate,
                        onCheckedChange = { isPrivate = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Private Note")
                }

                if (isPrivate) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Will be password protected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isPrivate) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Text(
                        text = "This note will be password protected. You'll be prompted to set up a password if you haven't already.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Auto-save indicator
            if (settingsState.autoSave && isEditing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Auto-save enabled",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Auto-save enabled",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    // Password Dialog
    PasswordDialog(
        state = passwordDialogState,
        onPasswordSet = { password -> viewModel.setPassword(password) },
        onPasswordVerified = { },
        onDismiss = { viewModel.hidePasswordDialog() }
    )

    // Error handling
    if (saveState is UiState.Error) {
        LaunchedEffect(saveState) {
            // Show snackbar or error dialog
        }
    }
}
