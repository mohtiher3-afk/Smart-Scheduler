package com.example.feature.alerts.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.core.designsystem.theme.ColorTokens
import com.example.core.designsystem.theme.SpacingTokens
import org.koin.androidx.compose.koinViewModel

@Composable
fun AlertsScreenRoot(
    viewModel: AlertsViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    AlertsScreenContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun AlertsScreenContent(
    state: AlertsUiState,
    onEvent: (AlertsUiEvent) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(SpacingTokens.Medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active alerts",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ColorTokens.Secondary
                )
            }
        }
    }
}
