package com.example.feature.calculator.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CalculatorUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onEvent(event: CalculatorUiEvent) {
        when (event) {
            is CalculatorUiEvent.OnCourseSelected -> {
                _uiState.update { it.copy(selectedCourseId = event.courseId) }
            }
            is CalculatorUiEvent.OnCalculateClicked -> {
                // Calculate logic
                _uiState.update { it.copy(calculatedSessions = 15) }
            }
        }
    }
}
