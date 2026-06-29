package com.example.feature.calculator.presentation

data class CalculatorUiState(
    val isLoading: Boolean = false,
    val selectedCourseId: String? = null,
    val calculatedSessions: Int = 0,
    val errorMessage: String? = null
)

sealed interface CalculatorUiEvent {
    data class OnCourseSelected(val courseId: String) : CalculatorUiEvent
    data object OnCalculateClicked : CalculatorUiEvent
}

sealed interface CalculatorUiEffect {
    data class ShowToast(val message: String) : CalculatorUiEffect
}
