package com.example.ui.features.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Launch
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.SessionInfo
import com.example.ui.components.SmartButton
import com.example.ui.components.SmartButtonType
import com.example.ui.components.SmartEmptyState
import com.example.ui.material3_foundation.AppTheme

@Composable
fun UpcomingTasks(
    upcomingSessions: List<SessionInfo>,
    currentLanguage: String = "ar",
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .let { if (testTag != null) it.testTag(testTag) else it }
    ) {
        Text(
            text = if (currentLanguage == "ar") "المحاضرات القادمة" else "Upcoming Lectures",
            style = AppTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (upcomingSessions.isEmpty()) {
            SmartEmptyState(
                title = if (currentLanguage == "ar") "لا توجد محاضرات قادمة" else "All Caught Up!",
                description = if (currentLanguage == "ar") 
                    "جدولك خالي من المحاضرات القادمة حالياً. أضف بعض الدورات التعليمية لتظهر هنا." 
                else 
                    "Your study schedule is empty for now. Add some educational courses to get started.",
                icon = Icons.Rounded.EventAvailable,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Display top 3 upcoming sessions
            val topSessions = upcomingSessions.take(3)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                topSessions.forEach { session ->
                    UpcomingSessionItem(
                        session = session,
                        currentLanguage = currentLanguage,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
private fun UpcomingSessionItem(
    session: SessionInfo,
    currentLanguage: String,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upcoming_session_${session.courseId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Videocam,
                            contentDescription = "Session",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = session.courseName,
                            style = AppTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentLanguage == "ar") 
                                "${session.dayName} • ${session.formattedDate}" 
                            else 
                                "${session.dayName} • ${session.dateString}",
                            style = AppTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Show start time badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = session.timeStart,
                        style = AppTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }

            if (session.zoomAccount.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Button to copy zoom link
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Zoom Link", session.zoomAccount)
                            clipboard.setPrimaryClip(clip)
                            val copyMsg = if (currentLanguage == "ar") "تم نسخ الرابط بنجاح" else "Link copied to clipboard"
                            Toast.makeText(context, copyMsg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy Link",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Button to open meeting
                    SmartButton(
                        text = if (currentLanguage == "ar") "الانضمام إلى زووم" else "Join Meeting",
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(session.zoomAccount))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val errorMsg = if (currentLanguage == "ar") "خطأ في فتح الرابط" else "Failed to open link"
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        type = SmartButtonType.Filled,
                        icon = Icons.Rounded.Launch,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
