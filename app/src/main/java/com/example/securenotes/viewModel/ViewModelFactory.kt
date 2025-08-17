package com.example.securenotes.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.SavedStateHandle

class ViewModelFactory<T : ViewModel>(
    private val create: (SavedStateHandle) -> T
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return create(SavedStateHandle()) as T
    }
}