package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.material3_foundation.AppTheme
import com.example.ui.material3_foundation.Motion

enum class SmartChipType {
    Filter, Suggestion, Assist, Input
}

@Composable
fun SmartChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: SmartChipType = SmartChipType.Filter,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1.0f,
        animationSpec = Motion.GentleSpring,
        label = "chip_scale"
    )

    val containerColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
            selected -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surface
        },
        label = "chip_container_color"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            selected -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "chip_content_color"
    )

    val borderColor = when {
        !enabled -> MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        selected -> Color.Transparent
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = modifier
            .scale(scale)
            .let { if (testTag != null) it.testTag(testTag) else it },
        shape = RoundedCornerShape(AppTheme.shapes.small.topStart), // or 8.dp to 12.dp
        color = containerColor,
        contentColor = contentColor,
        border = if (borderColor != Color.Transparent) BorderStroke(1.dp, borderColor) else null,
        tonalElevation = if (selected) AppTheme.dimens.RadiusSmall else 0.dp
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier
                        .size(AppTheme.dimens.IconSmall)
                        .padding(end = 4.dp),
                    tint = contentColor
                )
            }

            Text(
                text = label,
                style = AppTheme.typography.labelLarge.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                )
            )

            if (trailingIcon != null) {
                Box(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .let {
                            if (onTrailingIconClick != null) {
                                it.clickable(onClick = onTrailingIconClick)
                            } else {
                                it
                            }
                        }
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = "Remove or Action",
                        modifier = Modifier.size(AppTheme.dimens.IconSmall),
                        tint = contentColor
                    )
                }
            }
        }
    }
}
