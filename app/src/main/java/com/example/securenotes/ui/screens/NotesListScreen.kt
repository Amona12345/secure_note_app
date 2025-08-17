package com.example.securenotes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.securenotes.UiState
import com.example.securenotes.data.db.entities.Note
import com.example.securenotes.ui.theme.EmptyNotesView
import com.example.securenotes.ui.theme.EmptySearchView
import com.example.securenotes.ui.theme.ErrorView
import com.example.securenotes.ui.theme.LoadingView
import com.example.securenotes.ui.theme.NoteItem
import com.example.securenotes.ui.theme.PasswordDialog
import com.example.securenotes.viewModel.NotesListViewModel
import com.example.securenotes.viewModel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesListScreen(
    viewModel: NotesListViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToNote: (Long) -> Unit,
    onNavigateToAddNote: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val notesState by viewModel.notesState.collectAsState()
    val searchState by viewModel.searchState.collectAsState()
    val passwordDialogState by viewModel.passwordDialogState.collectAsState()
    val settingsState by settingsViewModel.settingsState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Note?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                if (showSearch) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.searchNotes(it)
                        },
                        placeholder = { Text("Search notes...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                } else {
                    Text("Secure Notes")
                }
            },
            actions = {
                if (showSearch) {
                    IconButton(
                        onClick = {
                            showSearch = false
                            searchQuery = ""
                            viewModel.clearSearch()
                        }
                    ) {
                        Icon(Icons.Default.Close, "Close Search")
                    }
                } else {
                    IconButton(onClick = { showSearch = true }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            }
        )

        Box(modifier = Modifier.weight(1f)) {
            val displayState = if (searchQuery.isNotEmpty()) searchState else notesState

            when (displayState) {
                is UiState.Loading -> {
                    LoadingView()
                }

                is UiState.Empty -> {
                    if (searchQuery.isNotEmpty()) {
                        EmptySearchView()
                    } else {
                        EmptyNotesView(onAddNote = onNavigateToAddNote)
                    }
                }

                is UiState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayState.data) { note ->
                            NoteItem(
                                note = note,
                                fontSize = settingsState.fontSize,
                                onClick = { onNavigateToNote(note.id) },
                                onDelete = { showDeleteDialog = note }
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    ErrorView(
                        message = displayState.message,
                        onRetry = { viewModel.loadNotes() }
                    )
                }
            }

            FloatingActionButton(
                onClick = onNavigateToAddNote,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, "Add Note")
            }
        }
    }

    showDeleteDialog?.let { note ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Note") },
            text = { Text("Are you sure you want to delete '${note.title}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNote(note)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    PasswordDialog(
        state = passwordDialogState,
        onPasswordSet = { password -> viewModel.setPassword(password) },
        onPasswordVerified = { password -> viewModel.verifyPassword(password) },
        onDismiss = { viewModel.hidePasswordDialog() }
    )
}