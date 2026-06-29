package com.example.feature.alerts.presentation

data class AlertsUiState(
    val isLoading: Boolean = false,
    val reminders: List<String> = emptyList(),
    val errorMessage: String? = null
)

sealed interface AlertsUiEvent {
    data class OnDeleteReminder(val reminderId: String) : AlertsUiEvent
    data object OnClearAll : AlertsUiEvent
}

sealed interface AlertsUiEffect {
    data class ShowToast(val message: String) : AlertsUiEffect
}
