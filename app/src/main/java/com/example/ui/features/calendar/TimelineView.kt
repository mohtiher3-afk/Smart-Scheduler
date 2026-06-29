package com.example.ui.features.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.Course
import com.example.ui.components.SmartEmptyState
import com.example.core.designsystem.theme.AppTheme

@Composable
fun TimelineView(
    courses: List<Course>,
    onCourseUpdated: (Course) -> Unit,
    currentLanguage: String = "ar",
    modifier: Modifier = Modifier
) {
    var selectedCourse by remember(courses) {
        mutableStateOf(courses.firstOrNull { it.status == "نشط" } ?: courses.firstOrNull())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("timeline_view_container")
            .background(AppTheme.colors.background)
    ) {
        if (courses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                SmartEmptyState(
                    title = if (currentLanguage == "ar") "لا توجد دورات" else "No Courses",
                    description = if (currentLanguage == "ar") "يرجى إضافة دورات دراسية لعرض المخطط الزمني." else "Add courses to view the progress timeline.",
                    icon = Icons.Rounded.School,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Course selection slider
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(courses) { course ->
                    val isSelected = selectedCourse?.id == course.id
                    val colorHex = course.colorHex
                    val courseColor = remember(colorHex) {
                        try {
                            Color(android.graphics.Color.parseColor(colorHex))
                        } catch (e: Exception) {
                            Color.Blue
                        }
                    }

                    Surface(
                        onClick = { selectedCourse = course },
                        color = if (isSelected) courseColor else AppTheme.colors.surface,
                        shape = AppTheme.shapes.extraLarge,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else AppTheme.colors.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("timeline_chip_${course.id}")
                    ) {
                        Text(
                            text = course.name,
                            style = AppTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else AppTheme.colors.onSurface,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            selectedCourse?.let { course ->
                val completedSet = remember(course.completedLecturesCsv) {
                    course.getCompletedLecturesSet()
                }

                val defaultColor = AppTheme.colors.primary
                val courseColor = remember(course.colorHex, defaultColor) {
                    try {
                        Color(android.graphics.Color.parseColor(course.colorHex))
                    } catch (e: Exception) {
                        defaultColor
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Lecture milestones
                    items(course.targetCount) { index ->
                        val lectureNum = index + 1
                        val isCompleted = completedSet.contains(lectureNum)
                        val isNext = !isCompleted && (lectureNum == 1 || completedSet.contains(lectureNum - 1))

                        TimelineItemRow(
                            lectureNum = lectureNum,
                            isCompleted = isCompleted,
                            isNext = isNext,
                            courseColor = courseColor,
                            noteText = course.getLectureNote(lectureNum),
                            meetingDate = course.getLectureMeetingDate(lectureNum),
                            meetingTime = course.getLectureMeetingTime(lectureNum),
                            onToggleComplete = {
                                val updated = course.toggleLectureCompleted(lectureNum)
                                onCourseUpdated(updated)
                            },
                            onSaveNote = { note ->
                                val updated = course.setLectureNote(lectureNum, note)
                                onCourseUpdated(updated)
                            },
                            currentLanguage = currentLanguage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineItemRow(
    lectureNum: Int,
    isCompleted: Boolean,
    isNext: Boolean,
    courseColor: Color,
    noteText: String,
    meetingDate: String,
    meetingTime: String,
    onToggleComplete: () -> Unit,
    onSaveNote: (String) -> Unit,
    currentLanguage: String
) {
    var isEditingNote by remember { mutableStateOf(false) }
    var currentNoteInput by remember(noteText) { mutableStateOf(noteText) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_row_$lectureNum")
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Vertical Timeline Indicator Column
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(130.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            // Draw connector line
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeW = 3.dp.toPx()
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = strokeW
                )
            }

            // Draw Node Circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        color = when {
                            isCompleted -> courseColor
                            isNext -> AppTheme.colors.background
                            else -> AppTheme.colors.surface
                        }
                    )
                    .border(
                        width = 2.dp,
                        color = if (isCompleted) Color.Transparent else if (isNext) courseColor else Color.Gray.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .clickable(onClick = onToggleComplete),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (isNext) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(courseColor)
                    )
                }
            }
        }

        // Details card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .clickable(onClick = onToggleComplete),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isCompleted -> AppTheme.colors.surfaceVariant.copy(alpha = 0.4f)
                    isNext -> courseColor.copy(alpha = 0.05f)
                    else -> AppTheme.colors.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isNext) 2.dp else 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (currentLanguage == "ar") "المحاضرة #$lectureNum" else "Lecture #$lectureNum",
                        style = AppTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) AppTheme.colors.onSurface.copy(alpha = 0.6f) else AppTheme.colors.onSurface
                        )
                    )

                    // Complete/Incomplete status badge
                    Surface(
                        color = if (isCompleted) AppTheme.colors.primary.copy(alpha = 0.12f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isCompleted) {
                                if (currentLanguage == "ar") "مكتملة" else "Done"
                            } else {
                                if (currentLanguage == "ar") "مستمرة" else "Pending"
                            },
                            style = AppTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isCompleted) AppTheme.colors.primary else Color.Gray,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                if (meetingDate.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$meetingDate • $meetingTime",
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notes area
                if (isEditingNote) {
                    OutlinedTextField(
                        value = currentNoteInput,
                        onValueChange = { currentNoteInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_input_$lectureNum"),
                        textStyle = AppTheme.typography.bodyMedium,
                        placeholder = { Text(if (currentLanguage == "ar") "أضف ملاحظات لهذه المحاضرة..." else "Add notes...") },
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveNote(currentNoteInput)
                                isEditingNote = false
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("save_note_$lectureNum")
                        ) {
                            Text(if (currentLanguage == "ar") "حفظ" else "Save", fontSize = 11.sp)
                        }
                        TextButton(
                            onClick = { isEditingNote = false }
                        ) {
                            Text(if (currentLanguage == "ar") "إلغاء" else "Cancel", fontSize = 11.sp)
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isEditingNote = true },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (noteText.isBlank()) {
                                if (currentLanguage == "ar") "اضغط لإضافة ملاحظات دراسية..." else "Tap to add study notes..."
                            } else {
                                noteText
                            },
                            style = AppTheme.typography.bodyMedium.copy(
                                color = if (noteText.isBlank()) AppTheme.colors.onSurfaceVariant.copy(alpha = 0.5f) else AppTheme.colors.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Icons.Rounded.EditNote,
                            contentDescription = "Edit Note",
                            tint = AppTheme.colors.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
