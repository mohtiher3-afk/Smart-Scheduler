package com.example.feature.tasks.presentation

data class TasksUiState(
    val isLoading: Boolean = false,
    val tasks: List<String> = emptyList(),
    val errorMessage: String? = null
)

sealed interface TasksUiEvent {
    data class OnTaskClicked(val taskId: String) : TasksUiEvent
}

sealed interface TasksUiEffect {
    data class NavigateToTaskDetail(val taskId: String) : TasksUiEffect
}
