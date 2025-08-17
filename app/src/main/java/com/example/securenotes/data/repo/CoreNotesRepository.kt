package com.example.securenotes.data.repo

import com.example.securenotes.data.EncryptionManager
import com.example.securenotes.data.db.dao.NoteDao
import com.example.securenotes.data.db.entities.Note
import kotlinx.coroutines.flow.Flow

class CoreNotesRepository(
    private val noteDao: NoteDao,
    private val encryptionManager: EncryptionManager
) {

    fun getAllNotes(): Flow<List<Note>> = noteDao.getAll()

    suspend fun getNoteById(id: Long): Note? = noteDao.getById(id)

    suspend fun insertNote(note: Note): Long = noteDao.insert(note)

    suspend fun deleteNote(note: Note) = noteDao.delete(note)

    fun getPrivateNotesProjection(): Flow<List<Note>> = noteDao.getPrivateListProjection()

    // Security methods
    fun isPasswordProtected(note: Note): Boolean = note.isPrivate && encryptionManager.hasPassword()

    fun verifyPassword(password: String): Boolean = encryptionManager.verifyPassword(password)

    fun setPassword(password: String) = encryptionManager.savePassword(password)

    suspend fun updateNote(note: Note) = noteDao.update(note)

    fun hasPassword(): Boolean = encryptionManager.hasPassword()
}