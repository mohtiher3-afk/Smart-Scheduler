package com.example.feature.calendar.presentation

import com.example.models.Course

data class CalendarUiState(
    val isLoading: Boolean = true,
    val selectedDate: Long = System.currentTimeMillis(),
    val events: List<Course> = emptyList(), // Can be updated to CalendarEvent model
    val errorMessage: String? = null
)

sealed interface CalendarUiEvent {
    data class OnDateSelected(val date: Long) : CalendarUiEvent
    data class OnEventClicked(val eventId: String) : CalendarUiEvent
    data object OnAddEventClicked : CalendarUiEvent
}

sealed interface CalendarUiEffect {
    data class NavigateToEventDetails(val eventId: String) : CalendarUiEffect
    data object ShowAddEventDialog : CalendarUiEffect
}
