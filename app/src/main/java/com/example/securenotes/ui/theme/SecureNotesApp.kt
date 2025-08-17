package com.example.securenotes.ui.theme

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.securenotes.DependencyContainer
import com.example.securenotes.viewModel.NoteEditViewModel
import com.example.securenotes.viewModel.NotesListViewModel
@Composable
fun SecureNotesApp(dependencyContainer: DependencyContainer) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "notes_list"
    ) {
        composable("notes_list") {
            val viewModel: NotesListViewModel = viewModel {
                dependencyContainer.createNotesListViewModel()
            }
            val settingsViewModel=viewModel{
                dependencyContainer.settingsViewModel
            }
            NotesListScreen(
                viewModel = viewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToNote = { noteId ->
                    navController.navigate("note_detail/$noteId")
                },
                onNavigateToAddNote = {
                    navController.navigate("note_edit")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        composable(
            "note_detail/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            val savedStateHandle = SavedStateHandle(mapOf("noteId" to noteId))

            val viewModel = viewModel {
                dependencyContainer.createNoteDetailViewModel(savedStateHandle)
            }

            NoteDetailScreen(
                viewModel=viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { navController.navigate("note_edit/$noteId") }
            )
        }

        composable("note_edit") {
            val savedStateHandle = SavedStateHandle()
            val viewModel: NoteEditViewModel = viewModel {
                dependencyContainer.createNoteEditViewModel(savedStateHandle)
            }
            NoteEditScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            "note_edit/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            val savedStateHandle = SavedStateHandle(mapOf("noteId" to noteId))

            val viewModel = viewModel {
                dependencyContainer.createNoteEditViewModel(savedStateHandle)
            }

            NoteEditScreen(
                noteId = noteId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            val viewModel = dependencyContainer.settingsViewModel
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}