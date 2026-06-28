package com.example.ui.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Course
import com.example.ui.material3_foundation.AppTheme
import java.util.*

@Composable
fun MonthView(
    currentMonth: Calendar,
    selectedDate: Calendar,
    courses: List<Course>,
    onDateSelected: (Calendar) -> Unit,
    currentLanguage: String = "ar",
    modifier: Modifier = Modifier
) {
    val daysInMonth = remember(currentMonth) {
        getDaysInMonthList(currentMonth)
    }

    val weekdays = remember(currentLanguage) {
        if (currentLanguage == "ar") {
            listOf("ح", "ن", "ث", "ر", "خ", "ج", "س")
        } else {
            listOf("S", "M", "T", "W", "T", "F", "S")
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("month_view_container")
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Days of week header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    style = AppTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Calendar Days Grid
        val chunkedDays = daysInMonth.chunked(7)
        chunkedDays.forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                week.forEach { date ->
                    if (date == null) {
                        // Empty space for padding
                        Box(modifier = Modifier.weight(1f))
                    } else {
                        val isSelected = remember(selectedDate, date) {
                            isSameDay(selectedDate, date)
                        }
                        val isToday = remember(date) {
                            isToday(date)
                        }
                        val isCurrentMonth = remember(currentMonth, date) {
                            date.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
                        }

                        // Find courses active on this day of the week
                        val dayOfWeekIdx = date.get(Calendar.DAY_OF_WEEK) - 1 // Sunday = 0
                        val dayCourses = remember(courses, dayOfWeekIdx) {
                            courses.filter { course ->
                                course.status == "نشط" && parseDaysToIndices(course.days).contains(dayOfWeekIdx)
                            }
                        }

                        DayItem(
                            date = date,
                            isSelected = isSelected,
                            isToday = isToday,
                            isCurrentMonth = isCurrentMonth,
                            scheduledCourses = dayCourses,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayItem(
    date: Calendar,
    isSelected: Boolean,
    isToday: Boolean,
    isCurrentMonth: Boolean,
    scheduledCourses: List<Course>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayStr = date.get(Calendar.DAY_OF_MONTH).toString()

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .let {
                if (isToday && !isSelected) {
                    it.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else it
            }
            .clickable(onClick = onClick)
            .testTag("day_item_$dayStr"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = dayStr,
            style = AppTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.primary
                    isCurrentMonth -> MaterialTheme.colorScheme.onSurface
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                },
                fontSize = 15.sp
            )
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Dots for courses
        Row(
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            scheduledCourses.take(3).forEach { course ->
                val dotColor = remember(course.colorHex) {
                    try {
                        Color(android.graphics.Color.parseColor(course.colorHex))
                    } catch (e: Exception) {
                        Color.Gray
                    }
                }
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(
                            color = if (isSelected) Color.White else dotColor,
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

// Helpers
private fun getDaysInMonthList(month: Calendar): List<Calendar?> {
    val list = mutableListOf<Calendar?>()
    val cal = month.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // Sunday = 0, Monday = 1... Saturday = 6
    for (i in 0 until firstDayOfWeek) {
        list.add(null)
    }

    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    for (i in 1..maxDays) {
        val dayCal = cal.clone() as Calendar
        dayCal.set(Calendar.DAY_OF_MONTH, i)
        list.add(dayCal)
    }

    // Pad the end to full weeks (multiples of 7)
    while (list.size % 7 != 0) {
        list.add(null)
    }

    return list
}

fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

fun isToday(cal: Calendar): Boolean {
    val today = Calendar.getInstance()
    return isSameDay(today, cal)
}

fun parseDaysToIndices(daysStr: String): List<Int> {
    val list = mutableListOf<Int>()
    val cleaned = daysStr.replace("،", ",").replace(" ", "")
    val parts = cleaned.split(",")
    parts.forEach { part ->
        when (part.trim()) {
            "الأحد", "الأحد" -> list.add(0)
            "الاثنين", "الاثنين" -> list.add(1)
            "الثلاثاء", "الثلاثاء" -> list.add(2)
            "الأربعاء", "الأربعاء" -> list.add(3)
            "الخميس", "الخميس" -> list.add(4)
            "الجمعة", "الجمعة" -> list.add(5)
            "السبت", "السبت" -> list.add(6)
        }
    }
    return list
}
