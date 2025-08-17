package com.example.securenotes.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securenotes.UiState
import com.example.securenotes.data.db.entities.Note
import com.example.securenotes.data.repo.NotesRepository

import com.example.securenotes.security.NoteCipher
import com.example.securenotes.security.SecurePrefs

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import android.util.Base64

class NotesViewModel(
    private val notesRepository: NotesRepository
) : ViewModel() {

    private val _notesState = MutableStateFlow<UiState<List<Note>>>(UiState.Loading)
    val notesState: StateFlow<UiState<List<Note>>> = _notesState.asStateFlow()

    private val _currentNoteState = MutableStateFlow<UiState<Note?>>(UiState.Loading)
    val currentNoteState: StateFlow<UiState<Note?>> = _currentNoteState.asStateFlow()

    private val _saveNoteState = MutableStateFlow<UiState< Long>?>(null)
    val saveNoteState: StateFlow<UiState<Long>?> = _saveNoteState.asStateFlow()

    private val _deleteNoteState = MutableStateFlow<UiState<Unit>?>(null)
    val deleteNoteState: StateFlow<UiState<Unit>?> = _deleteNoteState.asStateFlow()

    private val _updateNoteState = MutableStateFlow<UiState<Unit>?>(null)
    val updateNoteState: StateFlow<UiState<Unit>?> = _updateNoteState.asStateFlow()

    private val _searchState = MutableStateFlow<UiState<List<Note>>>(UiState.Empty)
    val searchState: StateFlow<UiState<List<Note>>> = _searchState.asStateFlow()

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _notesState.value = UiState.Loading
            notesRepository.getAllNotes().collect { state ->
                _notesState.value = state
            }
        }
    }

    fun loadNote(id: Long) {
        viewModelScope.launch {
            _currentNoteState.value = UiState.Loading
            notesRepository.getNoteById(id).collect { state ->
                _currentNoteState.value = state
            }
        }
    }

    fun saveNote(note: Note) {
        viewModelScope.launch {
            _saveNoteState.value = UiState.Loading
            val result = notesRepository.insertNote(note)
            _saveNoteState.value = when (result) {
                is UiState.Success -> UiState.Success(result.data)
                is UiState.Error -> UiState.Error(result.message)
                UiState.Loading -> UiState.Loading
                UiState.Empty -> UiState.Empty
            }
            loadNotes()
        }
    }

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
            loadNotes()
        }
    }

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
            loadNotes()
        }
    }

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

    fun loadPrivateNotes() {
        viewModelScope.launch {
            _notesState.value = UiState.Loading
            notesRepository.getPrivateNotesProjection().collect { state ->
                _notesState.value = state
            }
        }
    }

    fun clearSearch() {
        _searchState.value = UiState.Empty
    }

    fun clearSaveNoteState() {
        _saveNoteState.value = null
    }

    fun clearDeleteNoteState() {
        _deleteNoteState.value = null
    }

    fun clearUpdateNoteState() {
        _updateNoteState.value = null
    }

    fun createNote(title: String, body: String, isPrivate: Boolean = false): Note {
        return Note(
            title = title,
            body = body,
            timestamp = System.currentTimeMillis(),
            isPrivate = isPrivate
        )
    }

    fun updateExistingNote(note: Note, title: String, body: String, isPrivate: Boolean? = null): Note {
        return note.copy(
            title = title,
            body = body,
            isPrivate = isPrivate ?: note.isPrivate,
            timestamp = System.currentTimeMillis() // Update timestamp on edit
        )
    }

    fun createPrivateNote(title: String, body: String, password: String): Note {
        val encryptedBody = NoteCipher.encrypt(body, NoteCipher.generateKey(password))
        return Note(
            title = title,
            body = encryptedBody.joinToString(separator = ",") { it.toString() }, // نخزن كـ String
            timestamp = System.currentTimeMillis(),
            isPrivate = true
        )
    }

    fun decryptPrivateNoteBody(note: Note, password: String): String? {
        return try {
            val bytes = note.body.split(",").map { it.toByte() }.toByteArray()
            NoteCipher.decrypt(bytes, NoteCipher.generateKey(password))
        } catch (e: Exception) {
            null
        }
    }
    fun saveNotePassword(password: String, context: Context) {
        SecurePrefs.savePassword(context, password)
    }

    fun getSavedPassword(context: Context): String? {
        return SecurePrefs.getPassword(context)
    }
    fun openPrivateNote(context: Context, note: Note, inputPassword: String): String? {
        val prefs = SecurePrefs.getPrefs(context)
        val savedPassword = prefs.getString("note_password", null) ?: return null

        return if (inputPassword == savedPassword) {
            val key = NoteCipher.generateKey(inputPassword)
            val encryptedData = Base64.decode(note.body, Base64.DEFAULT)
            NoteCipher.decrypt(encryptedData, key)
        } else {
            null
        }
    }
    suspend fun savePrivateNote(context: Context, note: Note, repository: NotesRepository) {
        val prefs = SecurePrefs.getPrefs(context)
        val password = prefs.getString("note_password", null) ?: return

        val key = NoteCipher.generateKey(password)
        val encryptedBody = NoteCipher.encrypt(note.body, key)

        val encryptedNote = note.copy(body = Base64.encodeToString(encryptedBody, Base64.DEFAULT))
        repository.insertNote(encryptedNote)
    }
}
