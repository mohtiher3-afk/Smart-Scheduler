package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.material3_foundation.Dimens

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
                    color = if (type == SmartButtonType.Filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    modifier = Modifier.size(Dimens.IconSmall)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
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
                    containerColor = containerColor ?: MaterialTheme.colorScheme.primary,
                    contentColor = contentColor ?: MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(Dimens.RadiusMedium),
                content = { content() }
            )
        }
        SmartButtonType.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = finalModifier,
                enabled = enabled && !loading,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = contentColor ?: MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(Dimens.RadiusMedium),
                content = { content() }
            )
        }
        SmartButtonType.Tonal -> {
            FilledTonalButton(
                onClick = onClick,
                modifier = finalModifier,
                enabled = enabled && !loading,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = containerColor ?: MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = contentColor ?: MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(Dimens.RadiusMedium),
                content = { content() }
            )
        }
        SmartButtonType.Text -> {
            TextButton(
                onClick = onClick,
                modifier = finalModifier,
                enabled = enabled && !loading,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor ?: MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(Dimens.RadiusMedium),
                content = { content() }
            )
        }
    }
}
