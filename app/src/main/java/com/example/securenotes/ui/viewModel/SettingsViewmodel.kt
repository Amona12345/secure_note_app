package com.example.securenotes.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.securenotes.UiState
import com.example.securenotes.data.db.entities.UserPreferences
import com.example.securenotes.data.repo.CoreNotesRepository
import com.example.securenotes.data.repo.PreferencesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch




class SettingsViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val coreRepository: CoreNotesRepository
) : ViewModel() {

    val settingsState: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .distinctUntilChanged() // Prevent duplicate emissions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    private val _updateState = MutableStateFlow<UiState<Unit>?>(null)
    val updateState: StateFlow<UiState<Unit>?> = _updateState.asStateFlow()

    private val _passwordDialogState = MutableStateFlow<PasswordDialogState>(PasswordDialogState.Hidden)
    val passwordDialogState: StateFlow<PasswordDialogState> = _passwordDialogState.asStateFlow()

    private var fontSizeUpdateJob: Job? = null

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateDarkMode(enabled)
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.message ?: "Failed to update dark mode")
                delay(3000)
                _updateState.value = null
            }
        }
    }

    fun updateFontSize(size: Int) {
        fontSizeUpdateJob?.cancel()
        fontSizeUpdateJob = viewModelScope.launch {
            delay(150)
            try {
                val validSize = size.coerceIn(12, 24)
                preferencesRepository.updateFontSize(validSize)
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.message ?: "Failed to update font size")
                delay(3000)
                _updateState.value = null
            }
        }
    }

    fun updateAutoSave(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesRepository.updateAutoSave(enabled)
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.message ?: "Failed to update auto save")
                delay(3000)
                _updateState.value = null
            }
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
            try {
                coreRepository.setPassword(password)
                _passwordDialogState.value = PasswordDialogState.Hidden
                _updateState.value = UiState.Success(Unit)
                delay(2000)
                _updateState.value = null
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.message ?: "Failed to set password")
                delay(3000)
                _updateState.value = null
            }
        }
    }

    fun verifyPassword(password: String): Boolean = coreRepository.verifyPassword(password)


    fun migratePreferences(context: Context) {
        viewModelScope.launch {
            _updateState.value = UiState.Loading
            try {
                val result = preferencesRepository.migrateFromSharedPreferences(context)
                _updateState.value = result
                if (result is UiState.Success) {
                    delay(2000)
                    _updateState.value = null
                } else {
                    delay(3000)
                    _updateState.value = null
                }
            } catch (e: Exception) {
                _updateState.value = UiState.Error(e.message ?: "Migration failed")
                delay(3000)
                _updateState.value = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fontSizeUpdateJob?.cancel()
    }
}