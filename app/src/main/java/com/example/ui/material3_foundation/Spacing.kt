package com.example.ui.material3_foundation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Spacing {
    val None: Dp = 0.dp
    val Tiny: Dp = 2.dp
    val ExtraSmall: Dp = 4.dp
    val Small: Dp = 8.dp
    val Medium: Dp = 16.dp
    val Large: Dp = 24.dp
    val ExtraLarge: Dp = 32.dp
    val Huge: Dp = 48.dp
    val Massive: Dp = 64.dp
}

val LocalSpacing = staticCompositionLocalOf { Spacing }
