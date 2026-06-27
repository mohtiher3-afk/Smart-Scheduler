package com.example.widgets

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.InAppToastData
import kotlinx.coroutines.delay

@Composable
fun CustomInAppToast(
    toastData: InAppToastData,
    currentLang: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var progress by remember(toastData.id) { mutableStateOf(1f) }

    // Coroutine to animate progress down to 0 and auto-dismiss
    LaunchedEffect(toastData.id) {
        val steps = 100
        val delayPerStep = toastData.durationMillis / steps
        progress = 1f
        for (i in 1..steps) {
            delay(delayPerStep)
            progress = 1f - (i.toFloat() / steps)
        }
        onDismiss()
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 150, easing = LinearEasing),
        label = "toast_progress"
    )

    val surfaceColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("custom_in_app_toast"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(
            1.5.dp, 
            Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.6f), secondaryColor.copy(alpha = 0.4f)))
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bell / Notification glowing icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    primaryColor.copy(alpha = 0.15f),
                                    secondaryColor.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (toastData.isTest) Icons.Rounded.AutoAwesome else Icons.Rounded.NotificationsActive,
                        contentDescription = "Notification Icon",
                        tint = if (toastData.isTest) tertiaryColor else primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Text details
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (toastData.isTest) {
                                if (currentLang == "ar") "تنبيه ذكي تجريبي 🔔" else "Smart Test Alert 🔔"
                            } else {
                                if (currentLang == "ar") "اقتراب موعد اللقاء! ⏱️" else "Meeting Approaching! ⏱️"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (toastData.isTest) tertiaryColor else primaryColor
                        )

                        if (toastData.isTest) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(tertiaryColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (currentLang == "ar") "تجريبي" else "Test",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tertiaryColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = toastData.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = "${if (currentLang == "ar") "الوقت:" else "Time:"} ${toastData.timeStr}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Dismiss X button (touch target handled nicely)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("toast_dismiss_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Dismiss",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Interactive Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Zoom Account label
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = "Zoom Account",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = toastData.zoomLink,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Action buttons: Launch or copy Zoom link
                Button(
                    onClick = {
                        val zoomLink = toastData.zoomLink
                        if (zoomLink.startsWith("http://") || zoomLink.startsWith("https://") || zoomLink.contains(".zoom.us") || zoomLink.contains("zoom")) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(zoomLink))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    if (currentLang == "ar") "لا يمكن فتح الرابط تلقائياً" else "Cannot open link automatically",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Copy Zoom account/link to clipboard
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Zoom Link", zoomLink)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(
                                context,
                                if (currentLang == "ar") "تم نسخ الحساب/الرابط! 📋" else "Link/Account copied! 📋",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("toast_join_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Videocam,
                        contentDescription = "Join Zoom",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentLang == "ar") "انضمام الآن" else "Join Now",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Elegant Bottom Animated Progress/Timer Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = if (toastData.isTest) tertiaryColor else primaryColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
