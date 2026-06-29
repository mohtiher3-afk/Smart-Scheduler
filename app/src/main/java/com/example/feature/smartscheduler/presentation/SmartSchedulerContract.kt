package com.example.feature.smartscheduler.presentation

import com.example.models.Course

data class SmartSchedulerUiState(
    val isLoading: Boolean = false,
    val aiResponse: String? = null,
    val suggestedCourses: List<Course> = emptyList(),
    val errorMessage: String? = null
)

sealed interface SmartSchedulerUiEvent {
    data class OnGenerateScheduleClicked(val prompt: String) : SmartSchedulerUiEvent
    data class OnAcceptScheduleClicked(val courses: List<Course>) : SmartSchedulerUiEvent
}

sealed interface SmartSchedulerUiEffect {
    data object NavigateToSchedule : SmartSchedulerUiEffect
    data class ShowToast(val message: String) : SmartSchedulerUiEffect
}
