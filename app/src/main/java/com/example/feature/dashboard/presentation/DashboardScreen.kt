package com.example.feature.dashboard.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.theme.ColorTokens
import com.example.core.designsystem.theme.SpacingTokens
import org.koin.androidx.compose.koinViewModel

@Composable
fun DashboardScreenRoot(
    viewModel: DashboardViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    
    DashboardScreenContent(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun DashboardScreenContent(
    state: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = SpacingTokens.Medium),
                verticalArrangement = Arrangement.spacedBy(SpacingTokens.Medium)
            ) {
                item {
                    Spacer(modifier = Modifier.height(SpacingTokens.Medium))
                    Text(
                        text = "Good Morning, Student!",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Here is your study overview for today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ColorTokens.Secondary
                    )
                }

                if (state.aiInsights != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = ColorTokens.Primary.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(SpacingTokens.Medium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "AI Insight",
                                    tint = ColorTokens.Primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.size(SpacingTokens.Small))
                                Text(
                                    text = state.aiInsights,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ColorTokens.OnBackground
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.Medium)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Focus Score",
                            value = "${state.focusScore}%"
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            title = "Study Streak",
                            value = "${state.studyStreak} days"
                        )
                    }
                }

                item {
                    Button(
                        onClick = { onEvent(DashboardUiEvent.OnStartStudySession) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = "Start")
                        Spacer(modifier = Modifier.size(SpacingTokens.Small))
                        Text("Start Focus Session")
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = ColorTokens.SurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(SpacingTokens.Medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = ColorTokens.Primary
            )
            Spacer(modifier = Modifier.height(SpacingTokens.ExtraSmall))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = ColorTokens.Secondary
            )
        }
    }
}
