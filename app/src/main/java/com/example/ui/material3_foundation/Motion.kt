package com.example.ui.material3_foundation

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object Motion {
    // Standard durations
    const val ShortDuration = 150
    const val MediumDuration = 300
    const val LongDuration = 450

    // Standard tweens
    val FastTween = TweenSpec<Float>(durationMillis = ShortDuration)
    val NormalTween = TweenSpec<Float>(durationMillis = MediumDuration)
    val SlowTween = TweenSpec<Float>(durationMillis = LongDuration)

    // Spring animations for tactile, playful feel
    val GentleSpring = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = 300f
    )
    
    val BouncySpring = spring<Float>(
        dampingRatio = 0.6f,
        stiffness = 200f
    )

    val ResponsiveSpring = spring<Float>(
        dampingRatio = 0.7f,
        stiffness = 300f
    )
}
