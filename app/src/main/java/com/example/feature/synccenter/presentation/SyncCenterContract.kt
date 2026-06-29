package com.example.feature.synccenter.presentation

data class SyncCenterUiState(
    val isSyncing: Boolean = false
)

sealed interface SyncCenterUiEvent {
    data object OnSyncNow : SyncCenterUiEvent
}
