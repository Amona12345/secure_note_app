package com.example.securenotes.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securenotes.UiState
import com.example.securenotes.data.db.entities.Note
import com.example.securenotes.data.repo.NoteDetailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    private val noteDetailRepository: NoteDetailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long = savedStateHandle.get<Long>("noteId")
        ?: throw IllegalArgumentException("noteId is required")

    private val _uiState = MutableStateFlow<UiState<Note?>>(UiState.Loading)
    val uiState: StateFlow<UiState<Note?>> = _uiState.asStateFlow()

    private val _deleteState = MutableStateFlow<UiState<Unit>?>(null)
    val deleteState: StateFlow<UiState<Unit>?> = _deleteState.asStateFlow()

    private val _exportState = MutableStateFlow<UiState<Unit>?>(null)
    val exportState: StateFlow<UiState<Unit>?> = _exportState.asStateFlow()

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            noteDetailRepository.getNoteById(noteId).collect { state ->
                _uiState.value = state
            }
        }
    }

    fun deleteNote() {
        viewModelScope.launch {
            val currentNote = (_uiState.value as? UiState.Success)?.data
            if (currentNote != null) {
                _deleteState.value = UiState.Loading
                _deleteState.value = noteDetailRepository.deleteNote(currentNote)
            }
        }
    }

    fun exportNote(uri: android.net.Uri) {
        viewModelScope.launch {
            val currentNote = (_uiState.value as? UiState.Success)?.data
            if (currentNote != null) {
                _exportState.value = UiState.Loading
                _exportState.value = noteDetailRepository.exportNoteToFile(uri, currentNote)
            }
        }
    }

    fun unlockNote(password: String): Boolean {
        return if (noteDetailRepository.verifyPassword(password)) {
            loadNote()
            true
        } else {
            false
        }
    }

    fun isPasswordProtected(): Boolean {
        val note = (_uiState.value as? UiState.Success)?.data
        return note?.let { noteDetailRepository.isPasswordProtected(it) } ?: false
    }

    fun clearDeleteState() {
        _deleteState.value = null
    }

    fun clearExportState() {
        _exportState.value = null
    }
}