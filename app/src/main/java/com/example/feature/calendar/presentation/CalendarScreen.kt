package com.example.feature.calendar.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
fun CalendarScreenRoot(
    viewModel: CalendarViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    CalendarScreenContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun CalendarScreenContent(
    state: CalendarUiState,
    onEvent: (CalendarUiEvent) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(CalendarUiEvent.OnAddEventClicked) },
                containerColor = ColorTokens.Primary,
                contentColor = ColorTokens.OnPrimary
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Event")
            }
        }
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
                if (state.events.isEmpty()) {
                    Text("No events on this day", color = ColorTokens.Secondary)
                } else {
                    Text("Events count: ${state.events.size}", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
