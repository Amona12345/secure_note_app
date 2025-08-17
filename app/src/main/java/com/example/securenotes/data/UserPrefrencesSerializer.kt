// UserPreferencesSerializer.kt
package com.example.securenotes.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.securenotes.data.db.entities.UserPreferences
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object UserPreferencesSerializer : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences = UserPreferences()

    override suspend fun readFrom(input: InputStream): UserPreferences {
        return try {
            val jsonString = input.bufferedReader().use { it.readText() }
            if (jsonString.isBlank()) {
                defaultValue
            } else {
                Json.decodeFromString(UserPreferences.serializer(), jsonString)
            }
        } catch (exception: SerializationException) {
            throw CorruptionException("Cannot read UserPreferences", exception)
        }
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        val jsonString = Json.encodeToString(UserPreferences.serializer(), t)
        output.write(jsonString.toByteArray())
    }
}