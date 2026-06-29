package com.example.feature.files.presentation

data class FilesUiState(
    val isLoading: Boolean = false,
    val files: List<String> = emptyList(),
    val errorMessage: String? = null
)

sealed interface FilesUiEvent {
    data class OnFileClicked(val fileId: String) : FilesUiEvent
}

sealed interface FilesUiEffect {
    data class NavigateToFileDetail(val fileId: String) : FilesUiEffect
}
