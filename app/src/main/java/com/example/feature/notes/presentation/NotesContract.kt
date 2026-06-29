package com.example.feature.notes.presentation

data class NotesUiState(
    val isLoading: Boolean = true,
    val notes: List<String> = emptyList(),
    val errorMessage: String? = null
)

sealed interface NotesUiEvent {
    data object OnAddNoteClicked : NotesUiEvent
    data class OnNoteClicked(val noteId: String) : NotesUiEvent
}

sealed interface NotesUiEffect {
    data object ShowAddNoteScreen : NotesUiEffect
    data class NavigateToNoteDetails(val noteId: String) : NotesUiEffect
}
