package com.example.feature.more.presentation

data class MoreUiState(
    val isLoading: Boolean = false
)

sealed interface MoreUiEvent {
    data object OnOpenSettings : MoreUiEvent
}
