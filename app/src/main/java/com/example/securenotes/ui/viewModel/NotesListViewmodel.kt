package com.example.securenotes.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securenotes.UiState
import com.example.securenotes.data.db.entities.Note
import com.example.securenotes.data.repo.CoreNotesRepository
import com.example.securenotes.data.repo.NotesListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PasswordDialogState {
    data object Hidden : PasswordDialogState()
    data object SetupPassword : PasswordDialogState()
    data object EnterPassword : PasswordDialogState()
    data class Error(val message: String) : PasswordDialogState()
}

class NotesListViewModel(
    private val notesListRepository: NotesListRepository,
    private val coreRepository: CoreNotesRepository
) : ViewModel() {

    private val _notesState = MutableStateFlow<UiState<List<Note>>>(UiState.Loading)
    val notesState: StateFlow<UiState<List<Note>>> = _notesState.asStateFlow()

    private val _searchState = MutableStateFlow<UiState<List<Note>>>(UiState.Empty)
    val searchState: StateFlow<UiState<List<Note>>> = _searchState.asStateFlow()

    private val _deleteNoteState = MutableStateFlow<UiState<Unit>?>(null)
    val deleteNoteState: StateFlow<UiState<Unit>?> = _deleteNoteState.asStateFlow()

    private val _passwordDialogState = MutableStateFlow<PasswordDialogState>(PasswordDialogState.Hidden)
    val passwordDialogState: StateFlow<PasswordDialogState> = _passwordDialogState.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _notesState.value = UiState.Loading
            notesListRepository.getAllNotes().collect { state ->
                _notesState.value = state
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            _deleteNoteState.value = UiState.Loading
            _deleteNoteState.value = notesListRepository.deleteNote(note)
            if (_deleteNoteState.value is UiState.Success) {
                loadNotes()
            }
        }
    }

    fun searchNotes(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchState.value = UiState.Empty
                return@launch
            }
            notesListRepository.searchNotes(query).collect { state ->
                _searchState.value = state
            }
        }
    }



    fun clearSearch() {
        _searchState.value = UiState.Empty
    }



    fun setPassword(password: String) {
        viewModelScope.launch {
            coreRepository.setPassword(password)
            _passwordDialogState.value = PasswordDialogState.Hidden
        }
    }

    fun verifyPassword(password: String): Boolean = coreRepository.verifyPassword(password)



    fun hidePasswordDialog() {
        _passwordDialogState.value = PasswordDialogState.Hidden
    }
}