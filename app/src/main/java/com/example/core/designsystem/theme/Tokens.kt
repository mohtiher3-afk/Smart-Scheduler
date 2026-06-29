package com.example.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design Tokens
 * Centralized source of truth for all styling attributes.
 */

object ColorTokens {
    val Primary = Color(0xFF6750A4)
    val Secondary = Color(0xFF625B71)
    val Tertiary = Color(0xFF7D5260)
    
    val Background = Color(0xFFFFFBFE)
    val Surface = Color(0xFFFFFBFE)
    val SurfaceVariant = Color(0xFFE7E0EC)
    
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFFFFFFFF)
    val OnBackground = Color(0xFF1C1B1F)
    val OnSurface = Color(0xFF1C1B1F)
    
    val Error = Color(0xFFB3261E)
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
}

object SpacingTokens {
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
    val ExtraLarge = 32.dp
    val Huge = 48.dp
}

object RadiusTokens {
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
    val Full = 999.dp
}

object TypographyTokens {
    val HeadlineLarge = 32.sp
    val HeadlineMedium = 28.sp
    val HeadlineSmall = 24.sp
    
    val TitleLarge = 22.sp
    val TitleMedium = 16.sp
    val TitleSmall = 14.sp
    
    val BodyLarge = 16.sp
    val BodyMedium = 14.sp
    val BodySmall = 12.sp
}
