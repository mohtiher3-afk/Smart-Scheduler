package com.example.feature.studyhub.presentation

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
fun StudyHubScreenRoot(
    viewModel: StudyHubViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    StudyHubScreenContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun StudyHubScreenContent(
    state: StudyHubUiState,
    onEvent: (StudyHubUiEvent) -> Unit
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
                if (state.studyGroups.isEmpty()) {
                    Text("No active study groups", color = ColorTokens.Secondary)
                } else {
                    Text("Groups count: ${state.studyGroups.size}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
