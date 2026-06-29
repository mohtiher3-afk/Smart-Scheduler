package com.example.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
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
    onConfirm: (String, List<Int>, String, String, String, Int, Boolean, Int, String, String) -> Unit
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
    var category by remember { mutableStateOf(course?.category ?: "عام") }

    var showConflictConfirmDialog by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    val currentLang = com.example.screens.LocalAppLanguage.current

    // Represent days of week
    val initialDays = remember {
        if (course == null) emptyList()
        else parseArabicDaysStringToIndices(course.days)
    }
    val selectedDays = remember { mutableStateListOf<Int>().apply { addAll(initialDays) } }

    val conflicts = remember(name, selectedDays.toList(), startTime, endTime, isActive, selectedColorHex, targetCount, category) {
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
            colorHex = selectedColorHex,
            category = category
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
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                // Dialog Header
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(primaryColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (course == null) Icons.Rounded.AddBox else Icons.Rounded.EditCalendar,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (course == null) "إضافة دورة جديدة" else "تعديل تفاصيل الدورة",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (course == null) "أدخل تفاصيل الدورة التدريبية ومواعيد البث بوضوح" else "قم بتحديث المواعيد والمعلومات الأساسية",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Course name card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Book,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "اسم الدورة التدريبية",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                placeholder = { Text("مثال: دورة إدارة الأعمال CMA") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("course_name_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Category Section
                item {
                    val currentLang = com.example.screens.LocalAppLanguage.current
                    val isAr = currentLang == "ar"
                    val predefinedCategories = if (isAr) {
                        listOf("عام", "محاسبة", "إدارة مالية", "تقنية", "لغات")
                    } else {
                        listOf("General", "Accounting", "Financial Management", "Technology", "Languages")
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Category,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    if (isAr) "التصنيف / مجال الدراسة" else "Category / Subject Area",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                placeholder = { Text(if (isAr) "اكتب أو اختر تصنيفاً..." else "Type or select a category...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("course_category_input"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Predefined chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                predefinedCategories.forEach { cat ->
                                    val isSelected = category.trim().equals(cat.trim(), ignoreCase = true)
                                    InputChip(
                                        selected = isSelected,
                                        onClick = { category = cat },
                                        label = { Text(cat, fontSize = 11.sp) },
                                        colors = InputChipDefaults.inputChipColors(
                                            selectedContainerColor = primaryColor.copy(alpha = 0.2f),
                                            selectedLabelColor = primaryColor
                                        ),
                                        border = if (isSelected) {
                                            BorderStroke(1.dp, primaryColor)
                                        } else {
                                            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Days Picker Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "أيام البث (اختر يومًا أو أكثر)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                mainAxisSpacing = 8.dp,
                                crossAxisSpacing = 8.dp
                            ) {
                                daysOptions.forEach { p ->
                                    val isSelected = selectedDays.contains(p.first)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) primaryColor else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                if (isSelected) selectedDays.remove(p.first)
                                                else selectedDays.add(p.first)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Text(
                                                text = p.second,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Timings Row Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "توقيت البث المباشر",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        if (currentLang == "ar") "البدء (م / ص)" else "Start (AM / PM)",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showStartTimePicker = true }
                                    ) {
                                        OutlinedTextField(
                                            value = startTime,
                                            onValueChange = {},
                                            readOnly = true,
                                            enabled = false,
                                            placeholder = { Text("06:15 م") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.Rounded.AccessTime,
                                                    contentDescription = null,
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        if (currentLang == "ar") "الانتهاء" else "End",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { showEndTimePicker = true }
                                    ) {
                                        OutlinedTextField(
                                            value = endTime,
                                            onValueChange = {},
                                            readOnly = true,
                                            enabled = false,
                                            placeholder = { Text("10:00 م") },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.Rounded.AccessTime,
                                                    contentDescription = null,
                                                    tint = primaryColor,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Account & Lectures Info Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            // Zoom Account
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountCircle,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "حساب Zoom المخصص",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = zoomAccount,
                                    onValueChange = { zoomAccount = it },
                                    placeholder = { Text("support.xx@fin.com.sa") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                            }

                            // Total Lectures
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Numbers,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        "عدد محاضرات الدورة الكلي",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = targetCount,
                                    onValueChange = { targetCount = it },
                                    placeholder = { Text("12") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("course_target_count_input"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }

                // Reminder Lead Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "توقيت التنبيه التلقائي قبل البث المباشر",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                listOf(15, 30).forEach { mins ->
                                    val isSelected = reminderLeadMinutes == mins
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) primaryColor else MaterialTheme.colorScheme.surface
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { reminderLeadMinutes = mins }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "قبل البث بـ $mins دقيقة",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Custom Card Color Picker Card
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "اللون المميز للبطاقة والدورة",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
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
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(parsedColor)
                                            .border(
                                                width = if (isChosen) 3.dp else 0.dp,
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
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Active Switch Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isActive) Icons.Rounded.OfflinePin else Icons.Rounded.Cancel,
                                    contentDescription = null,
                                    tint = if (isActive) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "حالة الدورة (نشطة تلقائياً)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Switch(
                                checked = isActive,
                                onCheckedChange = { isActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = primaryColor
                                )
                            )
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
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
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

                // Confirm Actions Bar
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                "إلغاء",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
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
                                        selectedColorHex,
                                        category
                                    )
                                }
                            },
                            enabled = name.trim().isNotEmpty(),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("save_course_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                disabledContainerColor = primaryColor.copy(alpha = 0.4f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Save,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "حفظ الدورة",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
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
                            selectedColorHex,
                            category
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تأكيد وحفظ الموعد", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConflictConfirmDialog = false }
                ) {
                    Text("إلغاء وتعديل الوقت", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showStartTimePicker) {
        M3TimePickerDialog(
            title = if (currentLang == "ar") "اختر وقت البدء" else "Select Start Time",
            initialTimeStr = startTime,
            currentLang = currentLang,
            onDismiss = { showStartTimePicker = false },
            onConfirm = {
                startTime = it
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        M3TimePickerDialog(
            title = if (currentLang == "ar") "اختر وقت الانتهاء" else "Select End Time",
            initialTimeStr = endTime,
            currentLang = currentLang,
            onDismiss = { showEndTimePicker = false },
            onConfirm = {
                endTime = it
                showEndTimePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3TimePickerDialog(
    title: String,
    initialTimeStr: String,
    currentLang: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val (initialHour, initialMinute) = remember(initialTimeStr) {
        parseTimeString(initialTimeStr)
    }
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val formatted = formatTime(state.hour, state.minute)
                    onConfirm(formatted)
                }
            ) {
                Text(if (currentLang == "ar") "تأكيد" else "Confirm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (currentLang == "ar") "إلغاء" else "Cancel")
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = state)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

private fun parseTimeString(timeStr: String): Pair<Int, Int> {
    try {
        val clean = timeStr.trim()
        val isPm = clean.contains("م") || clean.lowercase().contains("pm")
        val numbers = clean.replace(Regex("[^0-9:]"), "")
        val parts = numbers.split(":")
        var hours = parts[0].toIntOrNull() ?: 12
        val minutes = parts[1].toIntOrNull() ?: 0
        if (isPm && hours < 12) {
            hours += 12
        } else if (!isPm && hours == 12) {
            hours = 0
        }
        return Pair(hours, minutes)
    } catch (e: Exception) {
        return Pair(12, 0)
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val amPm = if (hour >= 12) "م" else "ص"
    val adjustedHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return String.format(java.util.Locale.US, "%02d:%02d %s", adjustedHour, minute, amPm)
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
