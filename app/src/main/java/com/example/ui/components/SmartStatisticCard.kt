package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.designsystem.theme.AppTheme
import com.example.core.designsystem.theme.Dimens

@Composable
fun SmartStatisticCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    icon: ImageVector? = null,
    progress: Float? = null, // Optional progress float (0.0f to 1.0f)
    trendPercentage: Float? = null, // Optional trend value (+3.5 or -2.0)
    cardGradient: Brush? = null,
    testTag: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { if (testTag != null) it.testTag(testTag) else it },
        shape = RoundedCornerShape(AppTheme.shapes.medium.topStart),
        colors = CardDefaults.cardColors(
            containerColor = if (cardGradient == null) MaterialTheme.colorScheme.surface else Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        val innerContent = @Composable {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = AppTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (cardGradient == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    )

                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (cardGradient == null) 
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                                    else 
                                        Color.White.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (cardGradient == null) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = value,
                        style = AppTheme.typography.displaySmall.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = if (cardGradient == null) MaterialTheme.colorScheme.onSurface else Color.White
                        )
                    )

                    if (trendPercentage != null) {
                        val isPositive = trendPercentage >= 0
                        val trendColor = if (isPositive) AppTheme.semanticColors.success else MaterialTheme.colorScheme.error
                        val trendIcon = if (isPositive) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown
                        val sign = if (isPositive) "+" else ""

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(trendColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = trendIcon,
                                contentDescription = "Trend",
                                tint = trendColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "$sign$trendPercentage%",
                                style = AppTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = trendColor
                                )
                            )
                        }
                    }
                }

                if (progress != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    SmartLinearProgress(
                        progress = progress,
                        height = 6.dp,
                        trackColor = if (cardGradient == null) 
                            MaterialTheme.colorScheme.surfaceVariant 
                        else 
                            Color.White.copy(alpha = 0.2f),
                        progressColor = if (cardGradient == null) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            Color.White
                    )
                }

                if (subtext != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = subtext,
                        style = AppTheme.typography.bodySmall.copy(
                            color = if (cardGradient == null) 
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f) 
                            else 
                                Color.White.copy(alpha = 0.7f)
                        )
                    )
                }
            }
        }

        if (cardGradient != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardGradient)
            ) {
                innerContent()
            }
        } else {
            innerContent()
        }
    }
}
