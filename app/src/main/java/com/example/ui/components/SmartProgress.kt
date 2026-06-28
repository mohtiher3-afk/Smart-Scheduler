package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.material3_foundation.AppTheme
import com.example.ui.material3_foundation.Motion

@Composable
fun SmartCircularProgress(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 10.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    showLabel: Boolean = true,
    testTag: String? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0.0f, 1.0f),
        animationSpec = Motion.NormalTween,
        label = "circular_progress"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .let { if (testTag != null) it.testTag(testTag) else it }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            
            // Draw background track
            drawCircle(
                color = trackColor,
                radius = (this.size.minDimension - strokeWidthPx) / 2,
                style = Stroke(width = strokeWidthPx)
            )

            // Draw progress arc
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        if (showLabel) {
            val percent = (progress.coerceIn(0.0f, 1.0f) * 100).toInt()
            Text(
                text = "$percent%",
                style = AppTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun SmartLinearProgress(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    showLabel: Boolean = false,
    testTag: String? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0.0f, 1.0f),
        animationSpec = Motion.NormalTween,
        label = "linear_progress"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        if (showLabel) {
            val percent = (progress.coerceIn(0.0f, 1.0f) * 100).toInt()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progress",
                    style = AppTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$percent%",
                    style = AppTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .let { if (testTag != null) it.testTag(testTag) else it }
        ) {
            // Re-usable custom linear progress drawing with round ends
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeHeight = size.height
                val radius = strokeHeight / 2

                // Track
                drawLine(
                    color = trackColor,
                    start = Offset(radius, radius),
                    end = Offset(size.width - radius, radius),
                    strokeWidth = strokeHeight,
                    cap = StrokeCap.Round
                )

                // Active progress
                val activeWidth = animatedProgress * size.width
                if (activeWidth > strokeHeight) {
                    drawLine(
                        color = progressColor,
                        start = Offset(radius, radius),
                        end = Offset(activeWidth - radius, radius),
                        strokeWidth = strokeHeight,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
