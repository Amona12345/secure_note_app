package com.example.securenotes.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.securenotes.data.db.entities.Note
import java.io.IOException

class FileExportManager(private val context: Context) {

    fun createExportIntent(fileName: String): Intent {
        return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "$fileName.txt")
        }
    }

    suspend fun exportNoteToFile(uri: Uri, note: Note): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                val content = buildString {
                    appendLine("Title: ${note.title}")
                    appendLine("Created: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(note.timestamp))}")
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