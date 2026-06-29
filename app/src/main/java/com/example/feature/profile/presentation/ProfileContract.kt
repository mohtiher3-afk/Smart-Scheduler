package com.example.feature.profile.presentation

data class ProfileUiState(
    val isLoading: Boolean = false,
    val name: String = "Student"
)

sealed interface ProfileUiEvent {
    data object OnLogout : ProfileUiEvent
}
