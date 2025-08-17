package com.example.securenotes.ui.theme

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.securenotes.UiState
import com.example.securenotes.viewModel.NoteDetailViewModel
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit,
    viewModel: NoteDetailViewModel // Pass viewModel as parameter
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    val exportState by viewModel.exportState.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportNote(it) }
    }

    // Handle delete success
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
                IconButton(onClick = onNavigateToEdit) {
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
                    if (note.isPrivate && viewModel.isPasswordProtected()) {
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

    // Password Dialog
    if (showPasswordDialog) {
        PasswordEntryDialog(
            onPasswordEntered = { password ->
                if (viewModel.unlockNote(password)) {
                    showPasswordDialog = false
                } else {
                    // Show error - incorrect password
                }
            },
            onDismiss = {
                showPasswordDialog = false
                onNavigateBack()
            }
        )
    }

    // Delete Dialog
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

    // Export Dialog
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
