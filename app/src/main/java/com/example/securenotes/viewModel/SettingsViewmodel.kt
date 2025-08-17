package com.example.securenotes.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securenotes.UiState
import com.example.securenotes.data.db.entities.UserPreferences
import com.example.securenotes.data.repo.CoreNotesRepository
import com.example.securenotes.data.repo.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val coreRepository: CoreNotesRepository
) : ViewModel() {

    val settingsState: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _updateState = MutableStateFlow<UiState<Unit>?>(null)
    val updateState: StateFlow<UiState<Unit>?> = _updateState.asStateFlow()

    private val _passwordDialogState = MutableStateFlow<PasswordDialogState>(PasswordDialogState.Hidden)
    val passwordDialogState: StateFlow<PasswordDialogState> = _passwordDialogState.asStateFlow()

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            _updateState.value = preferencesRepository.updateDarkMode(enabled)
        }
    }

    fun updateFontSize(size: Int) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            _updateState.value = preferencesRepository.updateFontSize(size)
        }
    }

    fun updateAutoSave(enabled: Boolean) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            _updateState.value = preferencesRepository.updateAutoSave(enabled)
        }
    }

    fun showPasswordDialog() {
        _passwordDialogState.value = if (coreRepository.hasPassword()) {
            PasswordDialogState.EnterPassword
        } else {
            PasswordDialogState.SetupPassword
        }
    }

    fun hidePasswordDialog() {
        _passwordDialogState.value = PasswordDialogState.Hidden
    }

    fun setPassword(password: String) {
        viewModelScope.launch {
            coreRepository.setPassword(password)
            _passwordDialogState.value = PasswordDialogState.Hidden
        }
    }

    fun verifyPassword(password: String): Boolean = coreRepository.verifyPassword(password)

    fun clearUpdateState() {
        _updateState.value = null
    }

    // Migration method
    fun migratePreferences(context: Context) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            _updateState.value = preferencesRepository.migrateFromSharedPreferences(context)
        }
    }
}
