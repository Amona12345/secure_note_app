package com.example.securenotes.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey


object SecurePrefs {

    private const val FILE_NAME = "secure_prefs"

    fun getPrefs(context: Context): EncryptedSharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            FILE_NAME,
            masterKey, // بدل MasterKeys.getOrCreate
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun savePassword(context: Context, password: String) {
        val prefs = getPrefs(context)
        prefs.edit().putString("note_password", password).apply()
    }

    fun getPassword(context: Context): String? {
        return getPrefs(context).getString("note_password", null)
    }
}
