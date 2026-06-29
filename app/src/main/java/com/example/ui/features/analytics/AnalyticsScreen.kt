package com.example.ui.features.analytics

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.screens.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val totalMinutes by viewModel.totalStudyMinutes.collectAsStateWithLifecycle()
    val weeklyStats by viewModel.weeklyStudyStats.collectAsStateWithLifecycle()
    val gpa by viewModel.currentGpa.collectAsStateWithLifecycle()
    val productivity by viewModel.productivityScore.collectAsStateWithLifecycle()
    val sessions by viewModel.allStudySessions.collectAsStateWithLifecycle()
    val goals by viewModel.allGoals.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مركز التحليلات والذكاء", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_hero_analytics_1782742819496),
                    contentDescription = "Analytics Hero",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            item {
                AnalyticsHeader(totalMinutes, gpa, productivity)
            }

            item {
                StudyTimeSection(weeklyStats)
            }

            item {
                AiInsightsSection()
            }

            item {
                GoalsSection(goals)
            }

            item {
                HeatmapSection()
            }

            item {
                RecentSessionsSection(sessions)
            }
        }
    }
}

@Composable
fun AnalyticsHeader(minutes: Long, gpa: Double, productivity: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            title = "ساعات الدراسة",
            value = "${minutes / 60}h ${minutes % 60}m",
            icon = Icons.Rounded.Timer,
            color = Color(0xFF2196F3)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "المعدل التراكمي",
            value = String.format("%.2f", gpa),
            icon = Icons.Rounded.School,
            color = Color(0xFF4CAF50)
        )
        StatCard(
            modifier = Modifier.weight(1f),
            title = "درجة الإنتاجية",
            value = "$productivity%",
            icon = Icons.Rounded.Bolt,
            color = Color(0xFFFF9800)
        )
    }
}

@Composable
fun StatCard(modifier: Modifier, title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            Text(title, fontSize = 10.sp, color = color.copy(alpha = 0.7f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun StudyTimeSection(weeklyStats: Map<Int, Long>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("وقت الدراسة الأسبوعي", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(24.dp))
            
            // Custom Bar Chart using Canvas
            val days = listOf("أحد", "اثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")
            val data = (1..7).map { weeklyStats[it] ?: 0L }
            val maxMinutes = data.maxOfOrNull { it }?.coerceAtLeast(60L) ?: 60L
            
            Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barWidth = size.width / (data.size * 1.5f)
                    val spacing = (size.width - (barWidth * data.size)) / (data.size + 1)
                    
                    data.forEachIndexed { index, minutes ->
                        val barHeight = (minutes.toFloat() / maxMinutes) * size.height
                        val x = spacing + index * (barWidth + spacing)
                        val y = size.height - barHeight
                        
                        drawRoundRect(
                            color = Color(0xFF2196F3).copy(alpha = if (minutes > 0) 1f else 0.2f),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                        )
                    }
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                days.forEach { day ->
                    Text(day, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun AiInsightsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("توقعات وتوصيات الذكاء الاصطناعي", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            InsightItem("تدرس بشكل أفضل بين الساعة 6 مساءً و 8 مساءً. حاول جدولة المواد الصعبة في هذا الوقت.")
            InsightItem("مستوى تركيزك في مادة 'الخوارزميات' مرتفع جداً (95%).")
            InsightItem("توقعات المعدل القادم: 3.94 (بناءً على أدائك الحالي).")
        }
    }
}

@Composable
fun InsightItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("•", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 13.sp)
    }
}

@Composable
fun GoalsSection(goals: List<com.example.models.StudyGoal>) {
    Column {
        Text("الأهداف الحالية", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        if (goals.isEmpty()) {
            Text("لا توجد أهداف نشطة حالياً", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            goals.forEach { goal ->
                GoalItem(goal)
            }
        }
    }
}

@Composable
fun GoalItem(goal: com.example.models.StudyGoal) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(goal.title, fontWeight = FontWeight.Bold)
                Text("${if (goal.targetMinutes > 0) (goal.currentMinutes.toFloat() / goal.targetMinutes * 100).toInt() else 0}%", color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = if (goal.targetMinutes > 0) goal.currentMinutes.toFloat() / goal.targetMinutes else 0f,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text("${goal.currentMinutes} / ${goal.targetMinutes} دقيقة", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun HeatmapSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("خريطة الالتزام (Heatmap)", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            // Simple mockup of a heatmap grid
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(4) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        repeat(15) { col ->
                            val intensity = (row + col) % 4
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        when(intensity) {
                                            0 -> Color.LightGray.copy(alpha = 0.3f)
                                            1 -> Color(0xFFC8E6C9)
                                            2 -> Color(0xFF81C784)
                                            else -> Color(0xFF2E7D32)
                                        }
                                    )
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("آخر 60 يوماً من النشاط المستمر", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RecentSessionsSection(sessions: List<com.example.models.StudySession>) {
    Column {
        Text("آخر جلسات الدراسة", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        sessions.take(5).forEach { session ->
            SessionItem(session)
        }
    }
}

@Composable
fun SessionItem(session: com.example.models.StudySession) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("جلسة دراسة", fontWeight = FontWeight.Medium)
                Text("${session.durationMinutes} دقيقة • تركيز ${session.focusScore}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val date = java.text.SimpleDateFormat("MMM dd", java.util.Locale("ar")).format(java.util.Date(session.startTime))
            Text(date, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
