package com.example.feature.synccenter.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncCenterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SyncCenterUiState())
    val uiState: StateFlow<SyncCenterUiState> = _uiState.asStateFlow()

    fun onEvent(event: SyncCenterUiEvent) {}
}
