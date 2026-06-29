package com.example.feature.studyhub.presentation

data class StudyHubUiState(
    val isLoading: Boolean = true,
    val studyGroups: List<String> = emptyList(),
    val errorMessage: String? = null
)

sealed interface StudyHubUiEvent {
    data object OnJoinGroupClicked : StudyHubUiEvent
    data class OnGroupClicked(val groupId: String) : StudyHubUiEvent
}

sealed interface StudyHubUiEffect {
    data object ShowJoinGroupDialog : StudyHubUiEffect
    data class NavigateToGroupChat(val groupId: String) : StudyHubUiEffect
}
