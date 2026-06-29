package com.example.feature.files.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FilesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<FilesUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onEvent(event: FilesUiEvent) {
        when (event) {
            is FilesUiEvent.OnFileClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(FilesUiEffect.NavigateToFileDetail(event.fileId))
                }
            }
        }
    }
}
