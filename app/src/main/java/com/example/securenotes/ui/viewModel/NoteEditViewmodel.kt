package com.example.securenotes.viewModel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securenotes.UiState
import com.example.securenotes.data.db.entities.Note
import com.example.securenotes.data.db.entities.UserPreferences
import com.example.securenotes.data.repo.NoteEditRepository
import com.example.securenotes.data.repo.PreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteEditViewModel(
    private val noteEditRepository: NoteEditRepository,
    private val preferencesRepository: PreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val noteId: Long? = savedStateHandle.get<Long>("noteId")

    private val _uiState = MutableStateFlow<UiState<Note?>>(UiState.Loading)
    val uiState: StateFlow<UiState<Note?>> = _uiState.asStateFlow()

    private val _saveState = MutableStateFlow<UiState<Unit>?>(null)
    val saveState: StateFlow<UiState<Unit>?> = _saveState.asStateFlow()

    private val _passwordDialogState = MutableStateFlow<PasswordDialogState>(PasswordDialogState.Hidden)
    val passwordDialogState: StateFlow<PasswordDialogState> = _passwordDialogState.asStateFlow()

    val settingsState: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private var autoSaveJob: Job? = null

    init {
        loadNote()
    }

    private fun loadNote() {
        viewModelScope.launch {
            if (noteId != null) {
                _uiState.value = noteEditRepository.getNoteById(noteId)
            } else {
                // New note
                _uiState.value = UiState.Success(createEmptyNote())
            }
        }
    }

    fun saveNote(title: String, body: String, isPrivate: Boolean, category: String = "General") {
        viewModelScope.launch {
            if (isPrivate && !noteEditRepository.hasPassword()) {
                _passwordDialogState.value = PasswordDialogState.SetupPassword
                return@launch
            }

            val currentNote = (_uiState.value as? UiState.Success)?.data
            val noteToSave = if (currentNote != null && currentNote.id != 0L) {
                currentNote.copy(
                    title = title,
                    body = body,
                    isPrivate = isPrivate,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                Note(
                    title = title,
                    body = body,
                    isPrivate = isPrivate,
                    timestamp = System.currentTimeMillis()
                )
            }

            _saveState.value = UiState.Loading
            _saveState.value = noteEditRepository.saveNote(noteToSave)
        }
    }

    fun autoSave(title: String, body: String, isPrivate: Boolean, category: String = "General") {
        val settings = settingsState.value
        if (!settings.autoSave) return

        autoSaveJob?.cancel()

        autoSaveJob = viewModelScope.launch {
            kotlinx.coroutines.delay(2000) // Wait 2 seconds before auto-saving

            val currentNote = (_uiState.value as? UiState.Success)?.data
            if (currentNote != null && title.isNotBlank()) {
                val noteToSave = currentNote.copy(
                    title = title,
                    body = body,
                    isPrivate = isPrivate,
                    timestamp = System.currentTimeMillis()
                )
                noteEditRepository.autoSaveNote(noteToSave, settings.autoSave)
            }
        }
    }

    fun setPassword(password: String) {
        viewModelScope.launch {
            noteEditRepository.setPassword(password)
            _passwordDialogState.value = PasswordDialogState.Hidden
        }
    }

    fun hidePasswordDialog() {
        _passwordDialogState.value = PasswordDialogState.Hidden
    }



    private fun createEmptyNote() = Note(
        id = 0,
        title = "",
        body = "",
        isPrivate = false,
        timestamp = System.currentTimeMillis()
    )

    override fun onCleared() {
        super.onCleared()
        autoSaveJob?.cancel()
    }
}
