//package com.example.securenotes.data.repo
//
//import com.example.securenotes.UiState
//import com.example.securenotes.data.db.dao.NoteDao
//import com.example.securenotes.data.db.entities.Note
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.catch
//import kotlinx.coroutines.flow.flow
//import kotlinx.coroutines.flow.map
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class NotesRepository @Inject constructor(
//    private val noteDao: NoteDao
//) {
//
//    fun getAllNotes(): Flow<UiState<List<Note>>> =
//        noteDao.getAll()
//            .map<List<Note>, UiState<List<Note>>> { notes ->
//                if (notes.isEmpty()) {
//                    UiState.Empty
//                } else {
//                    UiState.Success(notes)
//                }
//            }
//            .catch { throwable ->
//                emit(UiState.Error("Failed to load notes: ${throwable.message}", throwable))
//            }
//
//    fun getNoteById(id: Long): Flow<UiState<Note?>> = flow {
//        emit(UiState.Loading)
//        try {
//            val note = noteDao.getById(id)
//            if (note != null) {
//                emit(UiState.Success(note))
//            } else {
//                emit(UiState.Error("Note not found"))
//            }
//        } catch (e: Exception) {
//            emit(UiState.Error("Failed to load note: ${e.message}", e))
//        }
//    }
//
//    suspend fun insertNote(note: Note): UiState<Long> = try {
//        val id = noteDao.insert(note)
//        UiState.Success(id)
//    } catch (e: Exception) {
//        UiState.Error("Failed to save note: ${e.message}", e)
//    }
//
//    suspend fun deleteNote(note: Note): UiState<Unit> = try {
//        noteDao.delete(note)
//        UiState.Success(Unit)
//    } catch (e: Exception) {
//        UiState.Error("Failed to delete note: ${e.message}", e)
//    }
//
//    suspend fun updateNote(note: Note): UiState<Unit> = try {
//        noteDao.insert(note) // Using insert with REPLACE strategy
//        UiState.Success(Unit)
//    } catch (e: Exception) {
//        UiState.Error("Failed to update note: ${e.message}", e)
//    }
//
//    fun getPrivateNotesProjection(): Flow<UiState<List<Note>>> =
//        noteDao.getPrivateListProjection()
//            .map<List<Note>, UiState<List<Note>>> { notes ->
//                if (notes.isEmpty()) {
//                    UiState.Empty
//                } else {
//                    UiState.Success(notes)
//                }
//            }
//            .catch { throwable ->
//                emit(UiState.Error("Failed to load private notes: ${throwable.message}", throwable))
//            }
//
//    fun searchNotes(query: String): Flow<UiState<List<Note>>> = flow {
//        emit(UiState.Loading)
//        try {
//            noteDao.getAll().collect { allNotes ->
//                val filteredNotes = allNotes.filter { note ->
//                    note.title.contains(query, ignoreCase = true) ||
//                            note.body.contains(query, ignoreCase = true)
//                }
//                if (filteredNotes.isEmpty()) {
//                    emit(UiState.Empty)
//                } else {
//                    emit(UiState.Success(filteredNotes))
//                }
//            }
//        } catch (e: Exception) {
//            emit(UiState.Error("Search failed: ${e.message}", e))
//        }
//    }
//}
