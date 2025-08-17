package com.example.securenotes.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.securenotes.UiState
import com.example.securenotes.ui.items.PasswordEntryDialog

import com.example.securenotes.ui.theme.ErrorView
import com.example.securenotes.ui.theme.LoadingView
import com.example.securenotes.ui.theme.LockedNoteView
import com.example.securenotes.ui.theme.NoteContent
import com.example.securenotes.viewModel.NoteDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: NoteDetailViewModel // Pass viewModel as parameter
) {

    val uiState by viewModel.uiState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val isUnlocked by viewModel.isUnlocked.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showEditPasswordDialog by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportNote(it) }
    }

    LaunchedEffect(deleteState) {
        if (deleteState is UiState.Success) {
            onNavigateBack()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Note Details") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        if (viewModel.canEdit()) {
                            onNavigateToEdit()
                        } else {
                            showEditPasswordDialog = true
                        }
                    }
                ) {
                    Icon(Icons.Default.Edit, "Edit")
                }
                IconButton(onClick = { showExportDialog = true }) {
                    Icon(Icons.Default.Share, "Export")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        )

        when (uiState) {
            is UiState.Loading -> {
                LoadingView()
            }

            is UiState.Success -> {
                val note = (uiState as UiState.Success).data
                if (note != null) {
                    if (note.isPrivate && !isUnlocked) {
                        LockedNoteView(
                            onUnlock = { showPasswordDialog = true }
                        )
                    } else {
                        NoteContent(note = note)
                    }
                } else {
                    ErrorView(
                        message = "Note not found",
                        onRetry = onNavigateBack
                    )
                }
            }

            is UiState.Error -> {
                ErrorView(
                    message = (uiState as UiState.Error).message,
                    onRetry = onNavigateBack
                )
            }

            else -> {}
        }
    }

    if (showPasswordDialog) {
        PasswordEntryDialog(
            title = "Enter Password to View Note",
            errorMessage = if (passwordError) "Incorrect password" else null,
            onPasswordEntered = { password ->
                if (viewModel.unlockNote(password)) {
                    showPasswordDialog = false
                    passwordError = false
                } else {
                    passwordError = true
                }
            },
            onDismiss = {
                showPasswordDialog = false
                passwordError = false
                onNavigateBack()
            }
        )
    }

    if (showEditPasswordDialog) {
        PasswordEntryDialog(
            title = "Enter Password to Edit Note",
            errorMessage = if (passwordError) "Incorrect password" else null,
            onPasswordEntered = { password ->
                if (viewModel.unlockNote(password)) {
                    showEditPasswordDialog = false
                    passwordError = false
                    onNavigateToEdit()
                } else {
                    passwordError = true
                }
            },
            onDismiss = {
                showEditPasswordDialog = false
                passwordError = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete this note?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNote()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Note") },
            text = { Text("Export this note as a text file?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportDialog = false
                        val note = (uiState as? UiState.Success)?.data
                        if (note != null) {
                            exportLauncher.launch("${note.title}.txt")
                        }
                    }
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
