package com.example.securenotes

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.SavedStateHandle
import com.example.securenotes.services.EncryptionManager
import com.example.securenotes.services.FileExportManager
import com.example.securenotes.data.db.AppDatabase
import com.example.securenotes.data.db.dao.NoteDao
import com.example.securenotes.data.repo.CoreNotesRepository
import com.example.securenotes.data.repo.NoteDetailRepository
import com.example.securenotes.data.repo.NoteEditRepository
import com.example.securenotes.data.repo.NotesListRepository
import com.example.securenotes.data.repo.PreferencesRepository
import com.example.securenotes.viewModel.NoteDetailViewModel
import com.example.securenotes.viewModel.NoteEditViewModel
import com.example.securenotes.viewModel.NotesListViewModel
import com.example.securenotes.viewModel.SettingsViewModel

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class DependencyContainer(private val context: Context) {

    private val noteDao: NoteDao by lazy {
        return@lazy AppDatabase.get(context).noteDao()
    }

    private val encryptionManager: EncryptionManager by lazy {
        EncryptionManager(context)
    }

    private val fileExportManager: FileExportManager by lazy {
        FileExportManager(context)
    }

    private val dataStore: DataStore<Preferences> by lazy {
        context.dataStore
    }

    private val coreNotesRepository: CoreNotesRepository by lazy {
        CoreNotesRepository(noteDao, encryptionManager)
    }

    private val noteEditRepository: NoteEditRepository by lazy {
        NoteEditRepository(coreNotesRepository)
    }

    private val noteDetailRepository: NoteDetailRepository by lazy {
        NoteDetailRepository(coreNotesRepository, fileExportManager)
    }

    private val notesListRepository: NotesListRepository by lazy {
        NotesListRepository(coreNotesRepository)
    }

    private val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(dataStore)
    }

    val settingsViewModel: SettingsViewModel by lazy {
        SettingsViewModel(preferencesRepository, coreNotesRepository)
    }

    fun createNoteEditViewModel(savedStateHandle: SavedStateHandle): NoteEditViewModel {
        return NoteEditViewModel(noteEditRepository, preferencesRepository, savedStateHandle)
    }

    fun createNoteDetailViewModel(savedStateHandle: SavedStateHandle): NoteDetailViewModel {
        return NoteDetailViewModel(noteDetailRepository, savedStateHandle)
    }

    fun createNotesListViewModel(): NotesListViewModel {
        return NotesListViewModel(notesListRepository, coreNotesRepository)
    }
}