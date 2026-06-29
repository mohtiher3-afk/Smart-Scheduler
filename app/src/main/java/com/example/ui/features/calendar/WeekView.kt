package com.example.ui.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Course
import com.example.core.designsystem.theme.AppTheme
import java.util.*

@Composable
fun WeekView(
    selectedDate: Calendar,
    courses: List<Course>,
    onDateSelected: (Calendar) -> Unit,
    currentLanguage: String = "ar",
    modifier: Modifier = Modifier
) {
    // Calculate the 7 days of the week containing the selectedDate
    val weekDays = remember(selectedDate) {
        getWeekDays(selectedDate)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("week_view_container")
            .background(
                color = AppTheme.colors.surface,
                shape = AppTheme.shapes.large
            )
            .padding(AppTheme.spacing.Medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEach { date ->
                val isSelected = remember(selectedDate, date) {
                    isSameDay(selectedDate, date)
                }
                val isToday = remember(date) {
                    isToday(date)
                }

                // Get Arabic day letter
                val dayLabel = remember(date, currentLanguage) {
                    val dayNum = date.get(Calendar.DAY_OF_WEEK)
                    getShortDayLabel(dayNum, currentLanguage)
                }

                val dayOfMon = date.get(Calendar.DAY_OF_MONTH).toString()

                // Find courses active on this day of the week
                val dayOfWeekIdx = date.get(Calendar.DAY_OF_WEEK) - 1 // Sunday = 0
                val dayCourses = remember(courses, dayOfWeekIdx) {
                    courses.filter { course ->
                        course.status == "نشط" && parseDaysToIndices(course.days).contains(dayOfWeekIdx)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp)
                        .clip(AppTheme.shapes.medium)
                        .background(
                            color = when {
                                isSelected -> AppTheme.colors.primary
                                isToday -> AppTheme.colors.primaryContainer.copy(alpha = 0.25f)
                                else -> Color.Transparent
                            }
                        )
                        .clickable { onDateSelected(date) }
                        .padding(vertical = AppTheme.spacing.Medium),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayLabel,
                        style = AppTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) AppTheme.colors.onPrimary.copy(alpha = 0.8f) else AppTheme.colors.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(AppTheme.spacing.Small))

                    Text(
                        text = dayOfMon,
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) AppTheme.colors.onPrimary else AppTheme.colors.onSurface
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Mini Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dayCourses.take(2).forEach { course ->
                            val dotColor = remember(course.colorHex) {
                                try {
                                    Color(android.graphics.Color.parseColor(course.colorHex))
                                } catch (e: Exception) {
                                    Color.Gray
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(
                                        color = if (isSelected) Color.White else dotColor,
                                        shape = RoundedCornerShape(100)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getWeekDays(date: Calendar): List<Calendar> {
    val list = mutableListOf<Calendar>()
    val cal = date.clone() as Calendar
    // Adjust to first day of the week (Sunday in most standard calendars)
    val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    cal.add(Calendar.DAY_OF_YEAR, -(dayOfWeek - Calendar.SUNDAY))

    for (i in 0 until 7) {
        list.add(cal.clone() as Calendar)
        cal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return list
}

private fun getShortDayLabel(dayNum: Int, currentLanguage: String): String {
    return if (currentLanguage == "ar") {
        when (dayNum) {
            Calendar.SUNDAY -> "أحد"
            Calendar.MONDAY -> "اثنين"
            Calendar.TUESDAY -> "ثلاثاء"
            Calendar.WEDNESDAY -> "أربعاء"
            Calendar.THURSDAY -> "خميس"
            Calendar.FRIDAY -> "جمعة"
            Calendar.SATURDAY -> "سبت"
            else -> ""
        }
    } else {
        when (dayNum) {
            Calendar.SUNDAY -> "Sun"
            Calendar.MONDAY -> "Mon"
            Calendar.TUESDAY -> "Tue"
            Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"
            Calendar.FRIDAY -> "Fri"
            Calendar.SATURDAY -> "Sat"
            else -> ""
        }
    }
}
