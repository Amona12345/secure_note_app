//package com.example.securenotes
//
//import com.example.securenotes.data.repo.CoreNotesRepository
//import com.example.securenotes.data.repo.NoteDetailRepository
//import com.example.securenotes.data.repo.NoteEditRepository
//import com.example.securenotes.data.repo.NotesListRepository
//import com.example.securenotes.data.repo.PreferencesRepository
//import dagger.Binds
//import dagger.Module
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//
//@Module
//@InstallIn(SingletonComponent::class)
//abstract class RepositoryModule {
//
//    @Binds
//    abstract fun bindCoreNotesRepository(impl: CoreNotesRepository): CoreNotesRepository
//
//    @Binds
//    abstract fun bindNotesListRepository(impl: NotesListRepository): NotesListRepository
//
//    @Binds
//    abstract fun bindNoteDetailRepository(impl: NoteDetailRepository): NoteDetailRepository
//
//    @Binds
//    abstract fun bindNoteEditRepository(impl: NoteEditRepository): NoteEditRepository
//
//    @Binds
//    abstract fun bindPreferencesRepository(impl: PreferencesRepository): PreferencesRepository
//}
