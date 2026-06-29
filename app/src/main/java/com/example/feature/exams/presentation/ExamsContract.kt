package com.example.feature.exams.presentation

data class ExamsUiState(
    val isLoading: Boolean = false,
    val exams: List<String> = emptyList()
)

sealed interface ExamsUiEvent {
    data class OnExamClicked(val examId: String) : ExamsUiEvent
}
