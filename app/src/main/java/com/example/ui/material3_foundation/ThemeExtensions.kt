package com.example.ui.material3_foundation

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomGradients(
    val primary: Brush = Brush.horizontalGradient(listOf(Color(0xFF1E3A8A), Color(0xFF0EA5E9))),
    val secondary: Brush = Brush.horizontalGradient(listOf(Color(0xFF0EA5E9), Color(0xFF34D399))),
    val warm: Brush = Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444))),
    val success: Brush = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
    val cardBackground: Brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC)))
)

@Immutable
data class SemanticColors(
    val success: Color = Color(0xFF10B981),
    val warning: Color = Color(0xFFF59E0B),
    val info: Color = Color(0xFF3B82F6),
    val completed: Color = Color(0xFF10B981),
    val active: Color = Color(0xFF10B981),
    val inactive: Color = Color(0xFF64748B)
)

val LocalCustomGradients = staticCompositionLocalOf { CustomGradients() }
val LocalSemanticColors = staticCompositionLocalOf { SemanticColors() }
