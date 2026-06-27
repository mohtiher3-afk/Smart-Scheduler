package com.example.screens.tabs

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Course
import com.example.models.ReminderEntity
import com.example.models.SessionResult
import com.example.services.SchedulerUtils
import com.example.screens.LocalAppLanguage
import com.example.screens.Loc
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalculatorTab(
    courses: List<Course>,
    selectedCourseId: Long,
    startDate: String,
    endDate: String,
    calculatedSessions: List<SessionResult>,
    reminders: List<ReminderEntity>,
    onCourseSelected: (Long) -> Unit,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onToggleReminder: (SessionResult) -> Unit,
    context: Context,
    onToggleSessionCompleted: (Int) -> Unit,
    onCourseUpdated: (Course) -> Unit = {}
) {
    val currentLang = LocalAppLanguage.current
    val loc = remember(currentLang) { Loc(currentLang) }

    val activeCourse = courses.find { it.id.toLong() == selectedCourseId }
    val activeCourseColor = remember(activeCourse?.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(activeCourse?.colorHex ?: "#2563EB"))
        } catch (e: Exception) {
            Color(0xFF2563EB)
        }
    }
    val formatterOutStr = remember(currentLang) { SimpleDateFormat("d MMM yyyy", Locale(currentLang)) }

    val startFormatted = remember(startDate) {
        SchedulerUtils.parseDate(startDate)?.let { formatterOutStr.format(it) } ?: startDate
    }
    val endFormatted = remember(endDate) {
        SchedulerUtils.parseDate(endDate)?.let { formatterOutStr.format(it) } ?: endDate
    }

    val sessionsSize = calculatedSessions.size
    val hoursDiff = if (activeCourse != null) {
        SchedulerUtils.calculateHoursDifference(activeCourse.timeStart, activeCourse.timeEnd)
    } else 3.75
    val totalHours = sessionsSize * hoursDiff

    val elapsedSessions = remember(calculatedSessions) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = sdf.format(Date())
        val today = try { sdf.parse(todayStr) } catch(e: Exception) { Date() } ?: Date()
        calculatedSessions.filter {
            try {
                val d = sdf.parse(it.dateString)
                d != null && (d.before(today) || d == today)
            } catch (e: Exception) {
                false
            }
        }
    }
    val elapsedCount = elapsedSessions.size
    val totalDaysCount = calculatedSessions.size
    val remainingCount = totalDaysCount - elapsedCount
    val progressPercentage = if (totalDaysCount > 0) (elapsedCount.toFloat() / totalDaysCount.toFloat()) else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dropdown selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = loc.selectCourseToCalc,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (courses.isEmpty()) {
                        Text(loc.emptyCoursesList, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { expanded = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("course_dropdown_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(activeCourse?.name ?: if (currentLang == "ar") "اختر المحاضرة..." else "Select course...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Icon(
                                        Icons.Rounded.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                             ) {
                                courses.forEach { course ->
                                    DropdownMenuItem(
                                        text = { Text(course.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                                        onClick = {
                                            onCourseSelected(course.id.toLong())
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Calendar fields
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = loc.selectTimePeriod,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(loc.fromDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                    .clickable { onStartDateClick() }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(startFormatted, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(loc.toDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                                    .clickable { onEndDateClick() }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(endFormatted, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Analytical Dashboard Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (currentLang == "ar") "محاسبة المواعيد والتحليل الذكي" else "Session Calculations & Smart Analysis",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sessions Graphic Pill
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sessionsSize.toString(),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(if (currentLang == "ar") "إجمالي المحاضرات" else "Total Lectures", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Divider Line
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(60.dp)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))
                        )

                        // Sizing Hours Graphic
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val formattedHours = String.format(Locale.US, "%.1f", totalHours)
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = formattedHours,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(if (currentLang == "ar") "مجموع الساعات" else "Total Hours", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (activeCourse != null && calculatedSessions.isNotEmpty()) {
                                SchedulerUtils.exportAllToIcsFile(
                                    context = context,
                                    courseName = activeCourse.name,
                                    sessions = calculatedSessions,
                                    timeStart = activeCourse.timeStart,
                                    timeEnd = activeCourse.timeEnd,
                                    zoomAccount = activeCourse.zoomAccount
                                )
                            }
                        },
                        enabled = activeCourse != null && calculatedSessions.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("export_all_calendar_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.08f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.40f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentLang == "ar") "تصدير كشف المواعيد بالكامل إلى تقويم الهاتف" else "Export entire schedule to phone calendar",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Interactive Progress Tracking Card
        if (activeCourse != null && calculatedSessions.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.School,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (currentLang == "ar") "تتبع مسيرة الدورة التدريبية" else "Course Journey Tracking",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = if (currentLang == "ar") "التقدم الزمني ومقدار الإنجاز الفعلي" else "Chronological progress & actual completion",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            val pct = (progressPercentage * 100).toInt()
                            Text(
                                text = "${loc.completed} $pct%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progressPercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = MaterialTheme.colorScheme.tertiary,
                            trackColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (currentLang == "ar") "الأيام/المحاضرات المنقضية" else "Elapsed Days/Lectures",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val dayLectureText = if (currentLang == "ar") "يوم / محاضرة" else "day / lecture"
                                Text(
                                    text = "$elapsedCount $dayLectureText",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (currentLang == "ar") "الأيام/المحاضرات المتبقية" else "Remaining Days/Lectures",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val dayLectureText = if (currentLang == "ar") "يوم / محاضرة" else "day / lecture"
                                Text(
                                    text = "$remainingCount $dayLectureText",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Course Notes Card
        if (activeCourse != null) {
            item {
                var notesText by remember(activeCourse.id) { mutableStateOf(activeCourse.notes) }
                val isUnsaved = notesText != activeCourse.notes

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = activeCourseColor.copy(alpha = 0.04f)),
                    border = BorderStroke(1.2.dp, activeCourseColor.copy(alpha = 0.22f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(activeCourseColor.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.School,
                                        contentDescription = null,
                                        tint = activeCourseColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentLang == "ar") "ملاحظات سريعة حول الدورة" else "Quick Course Notes",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = activeCourseColor
                                )
                            }
                            if (isUnsaved) {
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == "ar") "غير محفوظة" else "Unsaved",
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("course_notes_field"),
                            placeholder = {
                                Text(
                                    text = if (currentLang == "ar") "اكتب مذكراتك ومسائل المحاضرة أو روابط التواصل السريعة الخاصة بهذه الدورة هنا..." else "Type notes, key lecture topics, or quick links for this course here...",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = activeCourseColor,
                                unfocusedBorderColor = activeCourseColor.copy(alpha = 0.2f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                onCourseUpdated(activeCourse.copy(notes = notesText))
                                val notesSavedText = if (currentLang == "ar") "تم حفظ ملاحظاتك لهذه الدورة بنجاح! 💾" else "Course notes saved successfully! 💾"
                                Toast.makeText(context, notesSavedText, Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("save_notes_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isUnsaved) activeCourseColor else activeCourseColor.copy(alpha = 0.10f),
                                contentColor = if (isUnsaved) Color.White else activeCourseColor
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (isUnsaved) {
                                    if (currentLang == "ar") "حفظ التغييرات والمذكرات" else "Save Changes & Notes"
                                } else {
                                    if (currentLang == "ar") "تم الحفظ بنجاح ✓" else "Saved Successfully ✓"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Sessions list header
        if (calculatedSessions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = loc.scheduledLecturesList,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (currentLang == "ar") "$sessionsSize محاضرة مجدولة" else "$sessionsSize scheduled lectures",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            itemsIndexed(calculatedSessions) { index, session ->
                val hasReminder = reminders.any {
                    it.courseId == selectedCourseId && it.sessionDate == session.dateString
                }
                val sessionNum = index + 1
                val isCompleted = activeCourse?.getCompletedLecturesSet()?.contains(sessionNum) == true

                SessionRowItem(
                    session = session,
                    hasReminder = hasReminder,
                    onToggleReminder = { onToggleReminder(session) },
                    onExportToCalendar = {
                        if (activeCourse != null) {
                             SchedulerUtils.exportSingleToCalendar(
                                context = context,
                                courseName = activeCourse.name,
                                dateStr = session.dateString,
                                timeStart = activeCourse.timeStart,
                                timeEnd = activeCourse.timeEnd,
                                zoomAccount = activeCourse.zoomAccount
                             )
                        }
                    },
                    isCompleted = isCompleted,
                    onToggleCompleted = { onToggleSessionCompleted(sessionNum) },
                    courseColor = activeCourseColor
                )
            }
        } else {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (currentLang == "ar") "لا توجد مواعيد محاضرات في الفترة المحددة" else "No lecture dates in the selected range",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SessionRowItem(
    session: SessionResult,
    hasReminder: Boolean,
    onToggleReminder: () -> Unit,
    onExportToCalendar: () -> Unit,
    isCompleted: Boolean,
    onToggleCompleted: () -> Unit,
    courseColor: Color = MaterialTheme.colorScheme.primary
) {
    val currentLang = LocalAppLanguage.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.20f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) courseColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onToggleCompleted() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = if (currentLang == "ar") "تحديد مكتمل" else "Mark completed",
                        modifier = Modifier.size(18.dp),
                        tint = if (isCompleted) courseColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = session.formattedDate,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                        if (isCompleted) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(courseColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (currentLang == "ar") "مكتمل" else "Completed",
                                    color = courseColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    val dayDisplayName = if (currentLang == "en") {
                        when (session.dayNameArabic) {
                            "الأحد", "الاحد" -> "Sunday"
                            "الاثنين" -> "Monday"
                            "الثلاثاء" -> "Tuesday"
                            "الأربعاء", "الاربعاء" -> "Wednesday"
                            "الخميس" -> "Thursday"
                            "الجمعة" -> "Friday"
                            "السبت" -> "Saturday"
                            else -> session.dayNameArabic
                        }
                    } else {
                        session.dayNameArabic
                    }
                    Text(
                        text = dayDisplayName,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Export to Phone Calendar Button
                IconButton(
                    onClick = onExportToCalendar,
                    modifier = Modifier.size(38.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CalendarToday,
                        contentDescription = if (currentLang == "ar") "تصدير للتقويم" else "Export to calendar",
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Interactive bell alert toggle (using M3 secondary vs surfaceVariant states)
                IconButton(
                    onClick = onToggleReminder,
                    modifier = Modifier.size(38.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (hasReminder) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (hasReminder) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (hasReminder) Icons.Rounded.Notifications else Icons.Rounded.NotificationsOff,
                        contentDescription = if (currentLang == "ar") "التنبيهات" else "Alerts",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
