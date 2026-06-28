package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.material3_foundation.Dimens
import com.example.ui.material3_foundation.Motion

data class NavigationTabItem(
    val index: Int,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun SmartBottomBar(
    tabs: List<NavigationTabItem>,
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    NavigationBar(
        modifier = modifier
            .navigationBarsPadding()
            .let { if (testTag != null) it.testTag(testTag) else it },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = Dimens.SpaceSmall
    ) {
        tabs.forEach { tab ->
            val isSelected = activeTab == tab.index
            
            // Dynamic scale animation on select
            val animatedIconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.15f else 1.0f,
                animationSpec = Motion.ResponsiveSpring,
                label = "iconScale"
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.index) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                        modifier = Modifier
                            .size(Dimens.IconMedium)
                            .scale(animatedIconScale)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}
