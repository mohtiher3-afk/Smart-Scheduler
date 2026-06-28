package com.example.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SmartCircularProgress
import com.example.ui.material3_foundation.AppTheme

@Composable
fun ProgressCard(
    completedLectures: Int,
    totalLectures: Int,
    totalHours: Double,
    activeCourses: Int,
    progressPercentage: Float,
    currentLanguage: String = "ar",
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { if (testTag != null) it.testTag(testTag) else it },
        shape = RoundedCornerShape(AppTheme.shapes.medium.topStart),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (currentLanguage == "ar") "إجمالي مستوى الإنجاز" else "Overall Academic Progress",
                    style = AppTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                
                Spacer(modifier = Modifier.height(14.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Stat 1: Lectures Count
                    Column {
                        Text(
                            text = if (currentLanguage == "ar") "المحاضرات" else "Lectures",
                            style = AppTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$completedLectures / $totalLectures",
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .width(1.dp)
                            .align(Alignment.CenterVertically)
                    )

                    // Stat 2: Hours studied
                    Column {
                        Text(
                            text = if (currentLanguage == "ar") "الساعات" else "Hours",
                            style = AppTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$totalHours h",
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 16.sp
                            )
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .width(1.dp)
                            .align(Alignment.CenterVertically)
                    )

                    // Stat 3: Active courses
                    Column {
                        Text(
                            text = if (currentLanguage == "ar") "الدورات النشطة" else "Active Courses",
                            style = AppTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$activeCourses",
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Beautiful custom circular progress
            SmartCircularProgress(
                progress = progressPercentage,
                size = 80.dp,
                strokeWidth = 8.dp,
                progressColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                showLabel = true,
                testTag = "overall_progress_indicator"
            )
        }
    }
}
