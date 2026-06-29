package com.example.feature.schedule.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScheduleViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ScheduleUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadSchedule()
    }

    fun onEvent(event: ScheduleUiEvent) {
        when (event) {
            is ScheduleUiEvent.OnRefresh -> loadSchedule()
            is ScheduleUiEvent.OnCourseClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(ScheduleUiEffect.NavigateToCourseDetails(event.courseId))
                }
            }
            is ScheduleUiEvent.OnAddCourseClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(ScheduleUiEffect.ShowAddCourseDialog)
                }
            }
        }
    }

    private fun loadSchedule() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    courses = emptyList() // TODO: Fetch from repository
                )
            }
        }
    }
}
