package com.example.feature.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TasksViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<TasksUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onEvent(event: TasksUiEvent) {
        when (event) {
            is TasksUiEvent.OnTaskClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(TasksUiEffect.NavigateToTaskDetail(event.taskId))
                }
            }
        }
    }
}
