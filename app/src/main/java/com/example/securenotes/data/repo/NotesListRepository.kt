package com.example.securenotes.data.repo

import com.example.securenotes.UiState
import com.example.securenotes.data.db.entities.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class NotesListRepository(
    private val coreRepository: CoreNotesRepository
) {

    fun getAllNotes(): Flow<UiState<List<Note>>> =
        coreRepository.getAllNotes()
            .map<List<Note>, UiState<List<Note>>> { notes ->
                if (notes.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(notes)
                }
            }
            .catch { throwable ->
                emit(UiState.Error("Failed to load notes: ${throwable.message}", throwable))
            }

    suspend fun deleteNote(note: Note): UiState<Unit> = try {
        coreRepository.deleteNote(note)
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error("Failed to delete note: ${e.message}", e)
    }

    fun getPrivateNotesProjection(): Flow<UiState<List<Note>>> =
        coreRepository.getPrivateNotesProjection()
            .map<List<Note>, UiState<List<Note>>> { notes ->
                if (notes.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(notes)
                }
            }
            .catch { throwable ->
                emit(UiState.Error("Failed to load private notes: ${throwable.message}", throwable))
            }

    fun searchNotes(query: String): Flow<UiState<List<Note>>> = flow {
        emit(UiState.Loading)
        try {
            coreRepository.getAllNotes().collect { allNotes ->
                val filteredNotes = allNotes.filter { note ->
                    note.title.contains(query, ignoreCase = true) ||
                            note.body.contains(query, ignoreCase = true)
                }
                if (filteredNotes.isEmpty()) {
                    emit(UiState.Empty)
                } else {
                    emit(UiState.Success(filteredNotes))
                }
            }
        } catch (e: Exception) {
            emit(UiState.Error("Search failed: ${e.message}", e))
        }
    }
}