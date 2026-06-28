package com.example.ui.material3_foundation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Dimens {
    val SpaceExtraSmall: Dp = 4.dp
    val SpaceSmall: Dp = 8.dp
    val SpaceMedium: Dp = 16.dp
    val SpaceLarge: Dp = 24.dp
    val SpaceExtraLarge: Dp = 32.dp

    val RadiusSmall: Dp = 8.dp
    val RadiusMedium: Dp = 12.dp
    val RadiusLarge: Dp = 16.dp
    val RadiusExtraLarge: Dp = 24.dp

    val IconSmall: Dp = 18.dp
    val IconMedium: Dp = 24.dp
    val IconLarge: Dp = 32.dp
    val IconExtraLarge: Dp = 48.dp

    val InputFieldHeight: Dp = 54.dp
    val ButtonHeight: Dp = 50.dp
    val StandardButtonHeight: Dp = 50.dp
}

val LocalDimens = staticCompositionLocalOf { Dimens }
