package com.example.ui.theme

import androidx.compose.animation.core.tween

object Motion {
    val durationShort = 200
    val durationMedium = 300
    val durationLong = 450
    
    val easing = tween<Float>(durationMillis = durationMedium)
}
