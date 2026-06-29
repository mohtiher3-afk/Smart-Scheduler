package com.example.feature.schedule.presentation

import com.example.models.Course

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val courses: List<Course> = emptyList(),
    val errorMessage: String? = null
)

sealed interface ScheduleUiEvent {
    data object OnRefresh : ScheduleUiEvent
    data class OnCourseClicked(val courseId: String) : ScheduleUiEvent
    data object OnAddCourseClicked : ScheduleUiEvent
}

sealed interface ScheduleUiEffect {
    data class NavigateToCourseDetails(val courseId: String) : ScheduleUiEffect
    data object ShowAddCourseDialog : ScheduleUiEffect
}
