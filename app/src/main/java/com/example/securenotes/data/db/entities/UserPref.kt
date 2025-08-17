package com.example.securenotes.data.db.entities


import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
    val darkMode: Boolean = false,
    val fontSize: Int = 16,
    val autoSave: Boolean = true
)