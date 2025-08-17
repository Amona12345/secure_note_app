package com.example.securenotes.ui.theme.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.example.securenotes.data.db.entities.Note
import com.example.securenotes.viewModel.NotesViewModel

@Composable
fun PrivateNotePasswordDialog(
    note: Note,
    viewModel: NotesViewModel,
    onClose: () -> Unit
) {
    var inputPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val context = LocalContext.current
    val decryptedNote = remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Enter Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = inputPassword,
                    onValueChange = { inputPassword = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errorMessage.isNotEmpty()
                )
                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val result = viewModel.openPrivateNote(context, note, inputPassword)
                if (result != null) {
                    decryptedNote.value = result
                    onClose() // اقفل الديالوج بعد النجاح
                } else {
                    errorMessage = "Wrong password"
                }
            }) {
                Text("Open")
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text("Cancel")
            }
        }
    )

    // ممكن تعرض النص المفكك بعد فتحه
    decryptedNote.value?.let {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(note.title) },
            text = { Text(it) },
            confirmButton = {
                Button(onClick = { decryptedNote.value = null }) {
                    Text("Close")
                }
            }
        )
    }
}


