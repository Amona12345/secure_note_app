package com.example.securenotes.data.repo

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.securenotes.UiState
import com.example.securenotes.data.db.entities.UserPreferences
import com.example.securenotes.services.UserPreferencesSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException


class PreferencesRepository  (private val dataStore: DataStore<Preferences>) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

        fun create(context: Context): PreferencesRepository {
            return PreferencesRepository(context.dataStore)
        }
    }

    private object PreferencesKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val FONT_SIZE = intPreferencesKey("font_size")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                darkMode = preferences[PreferencesKeys.DARK_MODE] ?: false,
                fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 16,
                autoSave = preferences[PreferencesKeys.AUTO_SAVE] ?: true
            )
        }

    suspend fun updateDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_MODE] = enabled
        }
    }

    suspend fun updateFontSize(size: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = size
        }
    }

    suspend fun updateAutoSave(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SAVE] = enabled
        }
    }

    suspend fun migrateFromSharedPreferences(context: Context): UiState<Unit> {
        return try {
            val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)

            if (sharedPreferences.all.isEmpty()) {
                return UiState.Success(Unit)
            }

            dataStore.edit { preferences ->
                if (sharedPreferences.contains("dark_mode")) {
                    preferences[PreferencesKeys.DARK_MODE] = sharedPreferences.getBoolean("dark_mode", false)
                }
                if (sharedPreferences.contains("font_size")) {
                    preferences[PreferencesKeys.FONT_SIZE] = sharedPreferences.getInt("font_size", 16)
                }
                if (sharedPreferences.contains("auto_save")) {
                    preferences[PreferencesKeys.AUTO_SAVE] = sharedPreferences.getBoolean("auto_save", true)
                }
            }

            sharedPreferences.edit().clear().apply()
            UiState.Success(Unit)
        } catch (e: Exception) {
            UiState.Error(e.message ?: "Migration failed")
        }
    }
}