package com.example.feature.notes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<NotesUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadNotes()
    }

    fun onEvent(event: NotesUiEvent) {
        when (event) {
            is NotesUiEvent.OnAddNoteClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(NotesUiEffect.ShowAddNoteScreen)
                }
            }
            is NotesUiEvent.OnNoteClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(NotesUiEffect.NavigateToNoteDetails(event.noteId))
                }
            }
        }
    }

    private fun loadNotes() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(400)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    notes = emptyList() // Fetch from repository
                )
            }
        }
    }
}
