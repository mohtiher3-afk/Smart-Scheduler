package com.example.feature.alerts.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlertsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AlertsUiState())
    val uiState: StateFlow<AlertsUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AlertsUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onEvent(event: AlertsUiEvent) {
        when (event) {
            is AlertsUiEvent.OnDeleteReminder -> {
                viewModelScope.launch {
                    _uiEffect.emit(AlertsUiEffect.ShowToast("Reminder deleted"))
                }
            }
            is AlertsUiEvent.OnClearAll -> {
                viewModelScope.launch {
                    _uiEffect.emit(AlertsUiEffect.ShowToast("All reminders cleared"))
                }
            }
        }
    }
}
