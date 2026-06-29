package com.example.feature.studyhub.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StudyHubViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(StudyHubUiState())
    val uiState: StateFlow<StudyHubUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<StudyHubUiEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadGroups()
    }

    fun onEvent(event: StudyHubUiEvent) {
        when (event) {
            is StudyHubUiEvent.OnJoinGroupClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(StudyHubUiEffect.ShowJoinGroupDialog)
                }
            }
            is StudyHubUiEvent.OnGroupClicked -> {
                viewModelScope.launch {
                    _uiEffect.emit(StudyHubUiEffect.NavigateToGroupChat(event.groupId))
                }
            }
        }
    }

    private fun loadGroups() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    studyGroups = emptyList() // Fetch from repository
                )
            }
        }
    }
}
