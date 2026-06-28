package com.example.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.material3_foundation.AppTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GreetingCard(
    userName: String,
    currentLanguage: String = "ar",
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val calendar = remember { Calendar.getInstance() }
    val hour = remember { calendar.get(Calendar.HOUR_OF_DAY) }

    val greeting = remember(hour, currentLanguage) {
        if (currentLanguage == "ar") {
            when {
                hour in 5..11 -> "صباح الخير"
                hour in 12..16 -> "طاب يومك"
                hour in 17..22 -> "مساء الخير"
                else -> "أهلاً بك"
            }
        } else {
            when {
                hour in 5..11 -> "Good Morning"
                hour in 12..16 -> "Good Afternoon"
                hour in 17..22 -> "Good Evening"
                else -> "Welcome Back"
            }
        }
    }

    val formattedDate = remember(currentLanguage) {
        val locale = if (currentLanguage == "ar") Locale("ar") else Locale.ENGLISH
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", locale)
        sdf.format(Date())
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .let { if (testTag != null) it.testTag(testTag) else it },
        shape = RoundedCornerShape(AppTheme.shapes.large.topStart),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppTheme.shapes.large.topStart))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(primaryColor, secondaryColor)
                    )
                )
                .drawBehind {
                    // Draw decorative soft glowing background circles
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = size.width * 0.4f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.2f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = size.width * 0.25f,
                        center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.8f)
                    )
                }
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$greeting، ",
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 20.sp
                            )
                        )
                        Text(
                            text = userName,
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontSize = 22.sp
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = formattedDate,
                        style = AppTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "Decor Sparkles",
                        tint = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
