package com.example.ui.material3_foundation

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

object AppTheme {
    val colors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: androidx.compose.material3.Shapes
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.shapes

    val dimens: Dimens
        @Composable
        @ReadOnlyComposable
        get() = LocalDimens.current

    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current

    val gradients: CustomGradients
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomGradients.current

    val semanticColors: SemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSemanticColors.current
}
