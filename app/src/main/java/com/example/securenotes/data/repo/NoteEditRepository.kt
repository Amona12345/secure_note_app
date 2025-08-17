package com.example.securenotes.data.repo

import com.example.securenotes.UiState
import com.example.securenotes.data.db.entities.Note

class NoteEditRepository(
    private val coreRepository: CoreNotesRepository
) {

    suspend fun getNoteById(id: Long): UiState<Note?> = try {
        val note = coreRepository.getNoteById(id)
        if (note != null) {
            UiState.Success(note)
        } else {
            UiState.Error("Note not found")
        }
    } catch (e: Exception) {
        UiState.Error("Failed to load note: ${e.message}", e)
    }

    suspend fun saveNote(note: Note): UiState<Unit> = try {
        if (note.id == 0L) {
            // New note
            coreRepository.insertNote(note)
        } else {
            coreRepository.updateNote(note)
        }
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error("Failed to save note: ${e.message}", e)
    }

    suspend fun insertNote(note: Note): UiState<Long> = try {
        val id = coreRepository.insertNote(note)
        UiState.Success(id)
    } catch (e: Exception) {
        UiState.Error("Failed to save note: ${e.message}", e)
    }

    suspend fun updateNote(note: Note): UiState<Unit> = try {
        coreRepository.updateNote(note)
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error("Failed to update note: ${e.message}", e)
    }

    // Security methods for private notes
    fun setPassword(password: String) = coreRepository.setPassword(password)

    fun hasPassword(): Boolean = coreRepository.hasPassword()

    fun verifyPassword(password: String): Boolean = coreRepository.verifyPassword(password)

    // Auto-save functionality
    suspend fun autoSaveNote(note: Note, autoSaveEnabled: Boolean): UiState<Unit> {
        return if (autoSaveEnabled && note.title.isNotBlank()) {
            saveNote(note)
        } else {
            UiState.Success(Unit) // Skip auto-save
        }
    }
}