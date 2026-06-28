package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.material3_foundation.AppTheme
import com.example.ui.material3_foundation.Motion
import java.util.Calendar

data class DayScheduleState(
    val dayIndex: Int, // 0 = Sunday, 1 = Monday, ..., 6 = Saturday
    val shortNameAr: String,
    val shortNameEn: String,
    val hasEvents: Boolean
)

@Composable
fun SmartCalendarCard(
    selectedDayIndex: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    activeDays: List<Int> = emptyList(), // Days containing scheduled courses
    currentLanguage: String = "en",
    testTag: String? = null
) {
    val weekDays = remember(activeDays) {
        listOf(
            DayScheduleState(0, "أحد", "Sun", activeDays.contains(0)),
            DayScheduleState(1, "نثن", "Mon", activeDays.contains(1)),
            DayScheduleState(2, "ثلا", "Tue", activeDays.contains(2)),
            DayScheduleState(3, "ربع", "Wed", activeDays.contains(3)),
            DayScheduleState(4, "خميس", "Thu", activeDays.contains(4)),
            DayScheduleState(5, "جمعة", "Fri", activeDays.contains(5)),
            DayScheduleState(6, "سبت", "Sat", activeDays.contains(6))
        )
    }

    val todayIndex = remember {
        val cal = Calendar.getInstance()
        // Calendar.SUNDAY = 1 -> our index 0
        cal.get(Calendar.DAY_OF_WEEK) - 1
    }

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
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (currentLanguage == "ar") "جدول الأسبوع" else "Weekly Schedule",
                style = AppTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekDays.forEach { day ->
                    val isSelected = day.dayIndex == selectedDayIndex
                    val isToday = day.dayIndex == todayIndex

                    val itemScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.08f else 1.0f,
                        animationSpec = Motion.GentleSpring,
                        label = "day_scale"
                    )

                    val containerColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            else -> Color.Transparent
                        },
                        label = "day_bg"
                    )

                    val textColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        label = "day_text"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .scale(itemScale)
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(containerColor)
                            .clickable { onDaySelected(day.dayIndex) }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = if (currentLanguage == "ar") day.shortNameAr else day.shortNameEn,
                            style = AppTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = textColor
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Small active event indicator dot
                        if (day.hasEvents) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) 
                                            MaterialTheme.colorScheme.onPrimary 
                                        else 
                                            MaterialTheme.colorScheme.secondary
                                    )
                            )
                        } else {
                            // Empty spacer to maintain vertical spacing
                            Box(modifier = Modifier.size(6.dp))
                        }
                    }
                }
            }
        }
    }
}
