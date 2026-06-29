package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.core.designsystem.theme.Dimens
import com.example.core.designsystem.theme.Elevation
import com.example.core.designsystem.theme.Motion

@Composable
fun SmartFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    expanded: Boolean = false,
    containerColor: Color? = null,
    contentColor: Color? = null,
    testTag: String? = null
) {
    val finalContainerColor = containerColor ?: MaterialTheme.colorScheme.primaryContainer
    val finalContentColor = contentColor ?: MaterialTheme.colorScheme.onPrimaryContainer

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .let { if (testTag != null) it.testTag(testTag) else it },
        containerColor = finalContainerColor,
        contentColor = finalContentColor,
        shape = RoundedCornerShape(Dimens.RadiusLarge),
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = Elevation.FloatingActionButton
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(Dimens.IconMedium)
            )
            
            // Render text only if label is provided AND extended state is enabled
            if (label != null) {
                AnimatedVisibility(visible = expanded) {
                    Row {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
