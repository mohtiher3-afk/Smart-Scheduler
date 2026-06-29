package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.core.designsystem.theme.Dimens
import com.example.core.designsystem.theme.Elevation

enum class SmartCardType {
    Elevated, Filled, Outlined
}

@Composable
fun SmartCard(
    modifier: Modifier = Modifier,
    type: SmartCardType = SmartCardType.Filled,
    onClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp? = null,
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(Dimens.RadiusLarge)
    val cardElevation = elevation ?: when (type) {
        SmartCardType.Elevated -> Elevation.Level2
        else -> Elevation.Level0
    }

    val finalModifier = modifier
        .fillMaxWidth()
        .let { if (testTag != null) it.testTag(testTag) else it }
        .let {
            if (onClick != null) {
                it
                    .clip(cardShape)
                    .clickable(onClick = onClick)
            } else {
                it
            }
        }

    val finalContainerColor = containerColor ?: when (type) {
        SmartCardType.Filled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        SmartCardType.Outlined -> MaterialTheme.colorScheme.surface
        SmartCardType.Elevated -> MaterialTheme.colorScheme.surface
    }

    val finalBorder = when {
        borderColor != null -> BorderStroke(borderWidth, borderColor)
        type == SmartCardType.Outlined -> BorderStroke(borderWidth, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        else -> null
    }

    Card(
        modifier = finalModifier,
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = finalContainerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        border = finalBorder
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
