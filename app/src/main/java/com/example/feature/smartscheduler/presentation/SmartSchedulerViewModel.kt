package com.example.feature.smartscheduler.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SmartSchedulerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SmartSchedulerUiState())
    val uiState: StateFlow<SmartSchedulerUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SmartSchedulerUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onEvent(event: SmartSchedulerUiEvent) {
        when (event) {
            is SmartSchedulerUiEvent.OnGenerateScheduleClicked -> {
                generateSchedule(event.prompt)
            }
            is SmartSchedulerUiEvent.OnAcceptScheduleClicked -> {
                viewModelScope.launch {
                    // Save to repository
                    _uiEffect.emit(SmartSchedulerUiEffect.NavigateToSchedule)
                }
            }
        }
    }

    private fun generateSchedule(prompt: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    aiResponse = "Here is a suggested schedule based on your prompt: '$prompt'",
                    suggestedCourses = emptyList() // Parsed AI courses
                )
            }
        }
    }
}
