package com.example.securenotes.services

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.securenotes.data.db.entities.Note
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileExportManager(private val context: Context) {

     fun exportNoteToFile(uri: Uri, note: Note): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val content = buildString {
                    appendLine("Title: ${note.title}")
                    appendLine("Created: ${
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                            Date(note.timestamp)
                        )}")
                    appendLine("Private: ${if (note.isPrivate) "Yes" else "No"}")
                    appendLine()
                    appendLine("Content:")
                    appendLine(note.body)
                }
                outputStream.write(content.toByteArray())
            }
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}