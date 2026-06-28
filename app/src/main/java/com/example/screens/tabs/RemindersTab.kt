package com.example.screens.tabs

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.models.ReminderEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun RemindersTab(
    reminders: List<ReminderEntity>,
    onDeleteReminder: (ReminderEntity) -> Unit,
    onClearAllReminders: () -> Unit,
    selectedSound: String,
    onSoundChange: (String) -> Unit,
    onPlaySoundPreview: (String) -> Unit,
    onStopSoundPreview: () -> Unit,
    onTestInstantAlert: (String, String) -> Unit,
    context: Context
) {
    val currentLang = com.example.screens.LocalAppLanguage.current
    val loc = remember(currentLang) { com.example.screens.Loc(currentLang) }

    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var playingPreviewSound by remember { mutableStateOf<String?>(null) }
    var showSoundSettings by remember { mutableStateOf(false) }

    val locale = remember(currentLang) { if (currentLang == "ar") Locale("ar") else Locale.US }
    val dateFormat = remember(locale) { SimpleDateFormat("yyyy-MM-dd", locale) }
    val timeFormat = remember(locale) { SimpleDateFormat("hh:mm a", locale) }

    val soundOptions = remember(currentLang) {
        listOf(
            "default" to (if (currentLang == "ar") "النظام الافتراضي 📱" else "System Default 📱"),
            "digital_beep" to (if (currentLang == "ar") "رنين رقمي ثنائي 📟" else "Digital Beep 📟"),
            "soft_chime" to (if (currentLang == "ar") "جرس هادئ مزدوج 🔔" else "Soft Chime 🔔"),
            "classic_bell" to (if (currentLang == "ar") "نغمة كلاسيكية 🎼" else "Classic Bell 🎼"),
            "tech_alert" to (if (currentLang == "ar") "تنبيه تقني متصاعد ⚡" else "Tech Alert ⚡")
        )
    }

    // Confirmation dialog for clearing all reminders
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = {
                Text(
                    text = if (currentLang == "ar") "إلغاء جميع التنبيهات؟" else "Cancel All Alerts?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = if (currentLang == "ar") {
                        "هل أنت متأكد من رغبتك في إلغاء وحذف كافة التنبيهات المجدولة لجميع محاضراتك ودوراتك؟"
                    } else {
                        "Are you sure you want to clear and delete all scheduled notification alerts for all courses?"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllReminders()
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (currentLang == "ar") "نعم، إلغاء الكل" else "Yes, Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text(if (currentLang == "ar") "تراجع" else "Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Notification Settings Header Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reminders_settings_header"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsActive,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (currentLang == "ar") "التحكم في جرس التنبيهات 🔔" else "Alert & Sound Control 🔔",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (currentLang == "ar") "إدارة الأصوات والتنبيهات المباشرة" else "Manage sounds and notification tests",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = { showSoundSettings = !showSoundSettings },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        ) {
                            Icon(
                                imageVector = if (showSoundSettings) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Toggle Sound Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = showSoundSettings || reminders.isEmpty(),
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (currentLang == "ar") "اختر جرس التنبيه الافتراضي:" else "Choose Default Alert Sound:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // List of sound options as neat selectable badges
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                soundOptions.forEach { (soundKey, soundLabel) ->
                                    val isSelected = selectedSound == soundKey
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onSoundChange(soundKey) },
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = soundLabel,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )

                                            Icon(
                                                imageVector = if (playingPreviewSound == soundKey) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                                                contentDescription = "Preview Sound",
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clickable {
                                                        if (playingPreviewSound == soundKey) {
                                                            onStopSoundPreview()
                                                            playingPreviewSound = null
                                                        } else {
                                                            onPlaySoundPreview(soundKey)
                                                            playingPreviewSound = soundKey
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick trigger simulator
                            Button(
                                onClick = {
                                    onTestInstantAlert(
                                        if (currentLang == "ar") "تنبيه تجريبي فوري" else "Instant Test Alert",
                                        "https://zoom.us/demo-academic-session"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Alarm,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (currentLang == "ar") "إرسال تنبيه تجريبي حقيقي (بعد 5 ثوانٍ)" else "Trigger Test Alert (In 5 Seconds)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // List of Reminders
        if (reminders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.img_study_empty),
                            contentDescription = "No alerts illustration",
                            modifier = Modifier
                                .size(190.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = loc.noReminders,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = loc.noRemindersDesc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            }
        } else {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${loc.scheduledRemindersTitle} (${reminders.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = { showDeleteAllDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteSweep,
                            contentDescription = "Clear All",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLang == "ar") "إلغاء الكل" else "Clear All",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            items(reminders, key = { it.id }) { reminder ->
                val formattedDate = remember(reminder.timeInMillis) {
                    try {
                        dateFormat.format(Date(reminder.timeInMillis))
                    } catch (e: Exception) {
                        reminder.sessionDate
                    }
                }

                val formattedTime = remember(reminder.timeInMillis) {
                    try {
                        timeFormat.format(Date(reminder.timeInMillis))
                    } catch (e: Exception) {
                        "⏰"
                    }
                }

                Box(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reminder_card_${reminder.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Alarm,
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = reminder.courseName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    text = "⏰ $formattedTime",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                            ),
                                            border = null
                                        )
                                        SuggestionChip(
                                            onClick = {},
                                            label = {
                                                Text(
                                                    text = "📅 $formattedDate",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            border = null
                                        )
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Test trigger icon button for playing sound preview
                                IconButton(
                                    onClick = {
                                        if (playingPreviewSound == selectedSound) {
                                            onStopSoundPreview()
                                            playingPreviewSound = null
                                        } else {
                                            onPlaySoundPreview(selectedSound)
                                            playingPreviewSound = selectedSound
                                        }
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (playingPreviewSound == selectedSound) Icons.Rounded.Stop else Icons.Rounded.VolumeUp,
                                        contentDescription = "Test sound preview",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Delete button
                                IconButton(
                                    onClick = { onDeleteReminder(reminder) },
                                    colors = IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.NotificationsOff,
                                        contentDescription = loc.cancelReminder,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
