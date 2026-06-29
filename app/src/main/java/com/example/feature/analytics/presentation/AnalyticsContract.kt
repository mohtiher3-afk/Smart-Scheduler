package com.example.feature.analytics.presentation

data class AnalyticsUiState(
    val isLoading: Boolean = false,
    val focusTime: String = "0h 0m",
    val errorMessage: String? = null
)

sealed interface AnalyticsUiEvent {
    data object OnRefresh : AnalyticsUiEvent
}

sealed interface AnalyticsUiEffect {
    data class ShowToast(val message: String) : AnalyticsUiEffect
}
