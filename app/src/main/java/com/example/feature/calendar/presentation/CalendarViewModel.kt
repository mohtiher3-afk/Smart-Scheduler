package com.example.feature.calendar.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalendarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CalendarUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadEvents()
    }

    fun onEvent(event: CalendarUiEvent) {
        when (event) {
            is CalendarUiEvent.OnDateSelected -> {
                _uiState.update { it.copy(selectedDate = event.date) }
                loadEvents()
            }
            is CalendarUiEvent.OnEventClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(CalendarUiEffect.NavigateToEventDetails(event.eventId))
                }
            }
            is CalendarUiEvent.OnAddEventClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(CalendarUiEffect.ShowAddEventDialog)
                }
            }
        }
    }

    private fun loadEvents() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    events = emptyList() // Fetch real events from repository based on selected date
                )
            }
        }
    }
}
