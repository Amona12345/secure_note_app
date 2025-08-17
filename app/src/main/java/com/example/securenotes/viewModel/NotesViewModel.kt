package com.example.securenotes.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securenotes.UiState
import com.example.securenotes.data.db.entities.Note
import com.example.securenotes.data.repo.NotesRepository
import com.example.securenotes.security.NoteCipher
import com.example.securenotes.security.SecurePrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Base64

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
    private val _saveNoteState = MutableStateFlow<UiState< Long>?>(null)
    val saveNoteState: StateFlow<UiState<Long>?> = _saveNoteState.asStateFlow()

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
            _saveNoteState.value = notesRepository.insertNote(note)
            loadNotes() // Refresh the list
        }
    }

    // Update existing note
    fun updateNote(note: Note) {
        viewModelScope.launch {
            _updateNoteState.value = UiState.Loading
            _updateNoteState.value = notesRepository.updateNote(note)
            loadNotes() // Refresh the list
        }
    }

    // Delete note
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            _deleteNoteState.value = UiState.Loading
            _deleteNoteState.value = notesRepository.deleteNote(note)
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
    // Encrypt note body قبل الحفظ لو هي Private
    fun createPrivateNote(title: String, body: String, password: String): Note {
        val encryptedBody = NoteCipher.encrypt(body, NoteCipher.generateKey(password))
        return Note(
            title = title,
            body = encryptedBody.joinToString(separator = ",") { it.toString() }, // نخزن كـ String
            timestamp = System.currentTimeMillis(),
            isPrivate = true
        )
    }

    // فك تشفير نص الملاحظة الخاصة
    fun decryptPrivateNoteBody(note: Note, password: String): String? {
        return try {
            val bytes = note.body.split(",").map { it.toByte() }.toByteArray()
            NoteCipher.decrypt(bytes, NoteCipher.generateKey(password))
        } catch (e: Exception) {
            null // لو الباسورد غلط أو أي مشكلة
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
            null // كلمة السر غلط
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