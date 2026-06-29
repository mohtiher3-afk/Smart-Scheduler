package com.example.feature.courses.presentation

data class CoursesUiState(
    val isLoading: Boolean = false,
    val courses: List<String> = emptyList()
)

sealed interface CoursesUiEvent {
    data object OnAddCourse : CoursesUiEvent
}
