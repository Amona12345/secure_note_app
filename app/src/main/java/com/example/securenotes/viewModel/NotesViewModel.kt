package com.example.securenotes.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securenotes.UiState
import com.example.securenotes.data.db.entities.Note
import com.example.securenotes.data.repo.NotesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val notesRepository: NotesRepository
) : ViewModel() {

    // Notes List State
    private val _notesState = MutableStateFlow<UiState<List<Note>>>(UiState.Loading)
    val notesState: StateFlow<UiState<List<Note>>> = _notesState.asStateFlow()

    // Current Note State
    private val _currentNoteState = MutableStateFlow<UiState<Note?>>(UiState.Loading)
    val currentNoteState: StateFlow<UiState<Note?>> = _currentNoteState.asStateFlow()

    // Note Operations States
    private val _saveNoteState = MutableStateFlow<UiState<Unit>?>(null)
    val saveNoteState: StateFlow<UiState<Unit>?> = _saveNoteState.asStateFlow()

    private val _deleteNoteState = MutableStateFlow<UiState<Unit>?>(null)
    val deleteNoteState: StateFlow<UiState<Unit>?> = _deleteNoteState.asStateFlow()

    private val _updateNoteState = MutableStateFlow<UiState<Unit>?>(null)
    val updateNoteState: StateFlow<UiState<Unit>?> = _updateNoteState.asStateFlow()

    // Search State
    private val _searchState = MutableStateFlow<UiState<List<Note>>>(UiState.Empty)
    val searchState: StateFlow<UiState<List<Note>>> = _searchState.asStateFlow()

    init {
        loadNotes()
    }

    // Load all notes
    fun loadNotes() {
        viewModelScope.launch {
            _notesState.value = UiState.Loading
            notesRepository.getAllNotes().collect { state ->
                _notesState.value = state
            }
        }
    }

    // Load specific note by ID
    fun loadNote(id: Long) {
        viewModelScope.launch {
            _currentNoteState.value = UiState.Loading
            notesRepository.getNoteById(id).collect { state ->
                _currentNoteState.value = state
            }
        }
    }

    // Save new note
    fun saveNote(note: Note) {
        viewModelScope.launch {
            _saveNoteState.value = UiState.Loading
            val result = notesRepository.insertNote(note)
            _saveNoteState.value = when (result) {
                is UiState.Success -> UiState.Success(Unit)
                is UiState.Error -> UiState.Error(result.message)
                UiState.Loading -> UiState.Loading
                UiState.Empty -> UiState.Empty
            }
            loadNotes() // Refresh the list
        }
    }

    // Update existing note
    fun updateNote(note: Note) {
        viewModelScope.launch {
            _updateNoteState.value = UiState.Loading
            val result = notesRepository.updateNote(note)
            _updateNoteState.value = when (result) {
                is UiState.Success -> UiState.Success(Unit)
                is UiState.Error -> UiState.Error(result.message)
                UiState.Loading -> UiState.Loading
                UiState.Empty -> UiState.Empty
            }
            loadNotes() // Refresh the list
        }
    }

    // Delete note
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            _deleteNoteState.value = UiState.Loading
            val result = notesRepository.deleteNote(note)
            _deleteNoteState.value = when (result) {
                is UiState.Success -> UiState.Success(Unit)
                is UiState.Error -> UiState.Error(result.message)
                UiState.Loading -> UiState.Loading
                UiState.Empty -> UiState.Empty
            }
            loadNotes() // Refresh the list
        }
    }

    // Search notes
    fun searchNotes(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchState.value = UiState.Empty
                return@launch
            }

            notesRepository.searchNotes(query).collect { state ->
                _searchState.value = state
            }
        }
    }

    // Load private notes with projection
    fun loadPrivateNotes() {
        viewModelScope.launch {
            _notesState.value = UiState.Loading
            notesRepository.getPrivateNotesProjection().collect { state ->
                _notesState.value = state
            }
        }
    }

    // Clear search results
    fun clearSearch() {
        _searchState.value = UiState.Empty
    }

    // State reset functions
    fun clearSaveNoteState() {
        _saveNoteState.value = null
    }

    fun clearDeleteNoteState() {
        _deleteNoteState.value = null
    }

    fun clearUpdateNoteState() {
        _updateNoteState.value = null
    }

    // Helper function to create new note
    fun createNote(title: String, body: String, isPrivate: Boolean = false): Note {
        return Note(
            title = title,
            body = body,
            timestamp = System.currentTimeMillis(),
            isPrivate = isPrivate
        )
    }

    // Helper function to update existing note
    fun updateExistingNote(note: Note, title: String, body: String, isPrivate: Boolean? = null): Note {
        return note.copy(
            title = title,
            body = body,
            isPrivate = isPrivate ?: note.isPrivate,
            timestamp = System.currentTimeMillis() // Update timestamp on edit
        )
    }
}
