package com.example.securenotes.ui.theme

import androidx.compose.runtime.Composable
import com.example.securenotes.viewModel.PasswordDialogState

@Composable
fun PasswordDialog(
    state: PasswordDialogState,
    onPasswordSet: (String) -> Unit,
    onPasswordVerified: (String) -> Unit,
    onDismiss: () -> Unit
) {
    when (state) {
        is PasswordDialogState.SetupPassword -> {
            PasswordSetupDialog(
                onPasswordSet = onPasswordSet,
                onDismiss = onDismiss
            )
        }
        is PasswordDialogState.EnterPassword -> {
            PasswordEntryDialog(
                onPasswordEntered = onPasswordVerified,
                onDismiss = onDismiss
            )
        }
        is PasswordDialogState.Error -> {
            PasswordErrorDialog(
                message = state.message,
                onDismiss = onDismiss
            )
        }
        else -> { /* Hidden */ }
    }
}