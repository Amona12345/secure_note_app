package com.example.securenotes.data.repo

import com.example.securenotes.UiState
import com.example.securenotes.services.FileExportManager
import com.example.securenotes.data.db.entities.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class NoteDetailRepository(
    private val coreRepository: CoreNotesRepository,
    private val fileExportManager: FileExportManager
) {

    fun getNoteById(id: Long): Flow<UiState<Note?>> = flow {
        emit(UiState.Loading)
        try {
            val note = coreRepository.getNoteById(id)
            if (note != null) {
                emit(UiState.Success(note))
            } else {
                emit(UiState.Error("Note not found"))
            }
        } catch (e: Exception) {
            emit(UiState.Error("Failed to load note: ${e.message}", e))
        }
    }

    suspend fun deleteNote(note: Note): UiState<Unit> = try {
        coreRepository.deleteNote(note)
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error("Failed to delete note: ${e.message}", e)
    }

    suspend fun exportNoteToFile(uri: android.net.Uri, note: Note): UiState<Unit> = try {
        fileExportManager.exportNoteToFile(uri, note)
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error("Failed to export note: ${e.message}", e)
    }

    // Security methods for private notes
    fun isPasswordProtected(note: Note): Boolean = coreRepository.isPasswordProtected(note)

    fun verifyPassword(password: String): Boolean = coreRepository.verifyPassword(password)

    fun hasPassword(): Boolean = coreRepository.hasPassword()
}

