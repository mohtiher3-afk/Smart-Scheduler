package com.example.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.models.Course

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCourseDialog(
    course: Course?,
    existingCourses: List<Course>,
    onDismiss: () -> Unit,
    onConfirm: (String, List<Int>, String, String, String, Int, Boolean, Int, String) -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    var name by remember { mutableStateOf(course?.name ?: "") }
    var zoomAccount by remember { mutableStateOf(course?.zoomAccount ?: "") }
    var startTime by remember { mutableStateOf(course?.timeStart ?: "06:15 م") }
    var endTime by remember { mutableStateOf(course?.timeEnd ?: "10:00 م") }
    var targetCount by remember { mutableStateOf(course?.targetCount?.toString() ?: "12") }
    var isActive by remember { mutableStateOf(course?.status == "نشط" || course == null) }
    var reminderLeadMinutes by remember { mutableStateOf(course?.reminderLeadMinutes ?: 15) }
    var selectedColorHex by remember { mutableStateOf(course?.colorHex ?: "#2563EB") }

    var showConflictConfirmDialog by remember { mutableStateOf(false) }

    // Represent days of week
    val initialDays = remember {
        if (course == null) emptyList()
        else parseArabicDaysStringToIndices(course.days)
    }
    val selectedDays = remember { mutableStateListOf<Int>().apply { addAll(initialDays) } }

    val conflicts = remember(name, selectedDays.toList(), startTime, endTime, isActive, selectedColorHex, targetCount) {
        val daysStr = mapIndicesToArabicDays(selectedDays.toList().sorted())
        val tempCourse = Course(
            id = course?.id ?: 0,
            name = name.ifBlank { "دورة مؤقتة" },
            days = daysStr,
            timeStart = startTime,
            timeEnd = endTime,
            zoomAccount = zoomAccount,
            status = if (isActive) "نشط" else "غير نشط",
            completedCount = course?.completedCount ?: 0,
            targetCount = targetCount.toIntOrNull() ?: 12,
            reminderLeadMinutes = reminderLeadMinutes,
            colorHex = selectedColorHex
        )
        com.example.services.ConflictDetector.findConflicts(tempCourse, existingCourses)
    }

    val daysOptions = listOf(
        Pair(0, "الأحد"),
        Pair(1, "الاثنين"),
        Pair(2, "الثلاثاء"),
        Pair(3, "الأربعاء"),
        Pair(4, "الخميس"),
        Pair(5, "الجمعة"),
        Pair(6, "السبت")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = if (course == null) "إضافة دورة جديدة" else "تعديل تفاصيل الدورة",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // Course name
                item {
                    Column {
                        Text("اسم الدورة التدريبية", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("CMA PART 1 - October") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("course_name_input"),
                            shape = RoundedCornerShape(8.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Days picker Row
                item {
                    Column {
                        Text("أيام البث (اختر يومًا أو أكثر)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            mainAxisSpacing = 6.dp,
                            crossAxisSpacing = 6.dp
                        ) {
                            daysOptions.forEach { p ->
                                val isSelected = selectedDays.contains(p.first)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        if (isSelected) selectedDays.remove(p.first)
                                        else selectedDays.add(p.first)
                                    },
                                    label = { Text(p.second, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryColor,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Timings Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("البدء (م / ص)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = startTime,
                                onValueChange = { startTime = it },
                                placeholder = { Text("06:15 م") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("الانتهاء", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = endTime,
                                onValueChange = { endTime = it },
                                placeholder = { Text("10:00 م") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                // Zoom Info
                item {
                    Column {
                        Text("حساب Zoom المخصص", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = zoomAccount,
                            onValueChange = { zoomAccount = it },
                            placeholder = { Text("support.xx@fin.com.sa") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }

                // Target Count / Total Lectures
                item {
                    Column {
                        Text("عدد محاضرات الدورة (المحاضرات الكلية)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = targetCount,
                            onValueChange = { targetCount = it },
                            placeholder = { Text("12") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("course_target_count_input"),
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }

                // Active check
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("حالة الدورة (نشطة)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                        )
                    }
                }

                // Lead time choice for notifications
                item {
                    Column {
                        Text("توقيت التنبيه التلقائي قبل البث المباشر", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(15, 30).forEach { mins ->
                                val isSelected = reminderLeadMinutes == mins
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { reminderLeadMinutes = mins },
                                    label = { Text("قَبْل البث بـ $mins دقيقة", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryColor,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Distinct Custom Color Picker (New Beautiful Feature)
                item {
                    val colorOptions = listOf(
                        Pair("#2563EB", "أزرق ملكي"),
                        Pair("#7C3AED", "بنفسجي"),
                        Pair("#0D9488", "تيل/مخضر"),
                        Pair("#059669", "أخضر زمردي"),
                        Pair("#DC2626", "أحمر قرمزي"),
                        Pair("#D97706", "ذهبي/برتقالي"),
                        Pair("#DB2777", "وردي")
                    )
                    Column {
                        Text("اللون المميز للبطاقة والدورة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            colorOptions.forEach { (hex, colorName) ->
                                val parsedColor = try {
                                    Color(android.graphics.Color.parseColor(hex))
                                } catch (e: Exception) {
                                    primaryColor
                                }
                                val isChosen = selectedColorHex.equals(hex, ignoreCase = true)
                                
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(parsedColor)
                                        .border(
                                            width = if (isChosen) 3.dp else 1.dp,
                                            color = if (isChosen) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = hex }
                                        .testTag("color_choice_$hex"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = colorName,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Live conflicts warning
                if (conflicts.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Warning,
                                    contentDescription = "تحذير تعارض",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "تنبيه تعارض في الموعد!",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "تتداخل هذه الأوقات مع دورة '${conflicts[0].secondCourse.name}' في الأيام (${conflicts[0].conflictingDays.joinToString("، ")}) من الساعة ${conflicts[0].secondCourse.timeStart} إلى ${conflicts[0].secondCourse.timeEnd}.",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Confirm actions
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إلغاء", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                if (name.trim().isEmpty()) return@Button
                                if (conflicts.isNotEmpty()) {
                                    showConflictConfirmDialog = true
                                } else {
                                    onConfirm(
                                        name,
                                        selectedDays.toList().sorted(),
                                        startTime,
                                        endTime,
                                        zoomAccount,
                                        targetCount.toIntOrNull() ?: 12,
                                        isActive,
                                        reminderLeadMinutes,
                                        selectedColorHex
                                    )
                                }
                            },
                            enabled = name.trim().isNotEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_course_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("حفظ", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showConflictConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConflictConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text("تعارض في المواعيد! ⚠️", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "الدورة التدريبية تتداخل في أيامها أو أوقاتها مع دورة أخرى مسجلة حالياً:\n\n" +
                            "• الدورة المتعارضة: '${conflicts.firstOrNull()?.secondCourse?.name}'\n" +
                            "• في أيام: ${conflicts.firstOrNull()?.conflictingDays?.joinToString("، ")}\n" +
                            "• توقيت الدورة الأخرى: ${conflicts.firstOrNull()?.timeSpan}\n\n" +
                            "هل أنت متأكد من رغبتك في حفظ الموعد وتجاهل هذا التعارض؟",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConflictConfirmDialog = false
                        onConfirm(
                            name,
                            selectedDays.toList().sorted(),
                            startTime,
                            endTime,
                            zoomAccount,
                            targetCount.toIntOrNull() ?: 12,
                            isActive,
                            reminderLeadMinutes,
                            selectedColorHex
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("تأكيد وحفظ الموعد", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConflictConfirmDialog = false }) {
                    Text("إلغاء وتعديل الوقت", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

private fun parseArabicDaysStringToIndices(daysStr: String): List<Int> {
    val indices = mutableListOf<Int>()
    val clean = daysStr.replace("،", " ").replace(",", " ")
    if (clean.contains("الأحد") || clean.contains("الاحد")) indices.add(0)
    if (clean.contains("الاثنين")) indices.add(1)
    if (clean.contains("الثلاثاء")) indices.add(2)
    if (clean.contains("الأربعاء") || clean.contains("الاربعاء")) indices.add(3)
    if (clean.contains("الخميس")) indices.add(4)
    if (clean.contains("الجمعة")) indices.add(5)
    if (clean.contains("السبت")) indices.add(6)
    return indices
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        
        var currentX = 0
        var currentY = 0
        var maxHeightInLine = 0
        val positionMap = mutableListOf<Pair<androidx.compose.ui.layout.Placeable, Pair<Int, Int>>>()

        for (placeable in placeables) {
            val mainSpacing = mainAxisSpacing.roundToPx()
            val crossSpacing = crossAxisSpacing.roundToPx()

            if (currentX + placeable.width > layoutWidth) {
                currentX = 0
                currentY += maxHeightInLine + crossSpacing
                maxHeightInLine = 0
            }

            positionMap.add(placeable to (currentX to currentY))
            currentX += placeable.width + mainSpacing
            if (placeable.height > maxHeightInLine) {
                maxHeightInLine = placeable.height
            }
        }

        layout(layoutWidth, currentY + maxHeightInLine) {
            for ((placeable, position) in positionMap) {
                placeable.placeRelative(position.first, position.second)
            }
        }
    }
}

private fun mapIndicesToArabicDays(indices: List<Int>): String {
    return indices.map { index ->
        when (index) {
            0 -> "الأحد"
            1 -> "الاثنين"
            2 -> "الثلاثاء"
            3 -> "الأربعاء"
            4 -> "الخميس"
            5 -> "الجمعة"
            6 -> "السبت"
            else -> ""
        }
    }.filter { it.isNotEmpty() }.joinToString("، ")
}

