package com.example.feature.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<DashboardUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadDashboardData()
    }

    fun onEvent(event: DashboardUiEvent) {
        when (event) {
            is DashboardUiEvent.OnRefresh -> loadDashboardData()
            is DashboardUiEvent.OnCourseClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(DashboardUiEffect.NavigateToCourse(event.courseId))
                }
            }
            is DashboardUiEvent.OnStartStudySession -> {
                viewModelScope.launch {
                    _uiEffect.emit(DashboardUiEffect.NavigateToStudySession)
                }
            }
            is DashboardUiEvent.OnTaskCompleted -> {
                // Handle task completion
                // Call UseCase to update database
            }
            DashboardUiEvent.OnDismissError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun loadDashboardData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            // Simulate network/database load
            kotlinx.coroutines.delay(1000)
            
            _uiState.update {
                it.copy(
                    isLoading = false,
                    focusScore = 85,
                    studyStreak = 12,
                    aiInsights = "You're most productive in the morning. Try scheduling hard subjects then."
                )
            }
        }
    }
}
