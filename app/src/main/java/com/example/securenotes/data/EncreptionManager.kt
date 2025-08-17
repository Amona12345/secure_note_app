package com.example.securenotes.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

class EncryptionManager(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedSharedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "encrypted_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePassword(password: String) {
        encryptedSharedPrefs.edit {
            putString(PASSWORD_KEY, password)
        }
    }

    fun getPassword(): String? {
        return encryptedSharedPrefs.getString(PASSWORD_KEY, null)
    }

    fun verifyPassword(inputPassword: String): Boolean {
        val savedPassword = getPassword()
        return savedPassword == inputPassword
    }

    fun hasPassword(): Boolean {
        return getPassword() != null
    }

    companion object {
        private const val PASSWORD_KEY = "user_password"
    }
}