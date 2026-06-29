package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.theme.AppTheme
import com.example.core.designsystem.theme.Dimens
import com.example.core.designsystem.theme.Spacing

enum class SmartButtonType {
    Filled, Outlined, Tonal, Text
}

@Composable
fun SmartButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: SmartButtonType = SmartButtonType.Filled,
    icon: ImageVector? = null,
    iconContentDescription: String? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
    containerColor: Color? = null,
    contentColor: Color? = null,
    testTag: String? = null
) {
    val finalModifier = modifier
        .height(Dimens.StandardButtonHeight)
        .let { if (testTag != null) it.testTag(testTag) else it }

    val content: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = if (type == SmartButtonType.Filled) AppTheme.colors.onPrimary else AppTheme.colors.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(Dimens.IconSmall - 6.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.Small))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    modifier = Modifier.size(Dimens.IconSmall)
                )
                Spacer(modifier = Modifier.width(Spacing.Small))
            }
            Text(
                text = text,
                style = AppTheme.typography.labelLarge
            )
        }
    }

    when (type) {
        SmartButtonType.Filled -> {
            Button(
                onClick = onClick,
                modifier = finalModifier,
                enabled = enabled && !loading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor ?: AppTheme.colors.primary,
                    contentColor = contentColor ?: AppTheme.colors.onPrimary
                ),
                shape = AppTheme.shapes.medium,
                content = { content() }
            )
        }
        SmartButtonType.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = finalModifier,
                enabled = enabled && !loading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = contentColor ?: AppTheme.colors.primary
                ),
                shape = AppTheme.shapes.medium,
                content = { content() }
            )
        }
        SmartButtonType.Tonal -> {
            FilledTonalButton(
                onClick = onClick,
                modifier = finalModifier,
                enabled = enabled && !loading,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = containerColor ?: AppTheme.colors.secondaryContainer,
                    contentColor = contentColor ?: AppTheme.colors.onSecondaryContainer
                ),
                shape = AppTheme.shapes.medium,
                content = { content() }
            )
        }
        SmartButtonType.Text -> {
            TextButton(
                onClick = onClick,
                modifier = finalModifier,
                enabled = enabled && !loading,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor ?: AppTheme.colors.primary
                ),
                shape = AppTheme.shapes.medium,
                content = { content() }
            )
        }
    }
}
