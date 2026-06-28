# Material 3 Foundation Design System

This directory houses the foundational components, properties, and theme integrations that compose the visual language and interaction model for the Smart Scheduler app.

## File Breakdown

- **Color.kt**: Declares accessible, highly readable light and dark Material 3 color palettes. Features warm semantic highlights (emerald, teal, sky blue).
- **Dimens.dp**: Houses space layouts, rounding radii, input heights, and standardized icon sizing following the 8dp design grid.
- **Elevation.kt**: Explicitly defines Material 3 shadow depth values (Level 0 through Level 5).
- **Motion.kt**: Standardizes duration intervals (Short, Medium, Long) and springs (Gentle, Bouncy) to facilitate playful, highly responsive transitions.
- **Shapes.kt**: Declares consistent Material 3 corner rounding schemes for card layout surfaces, buttons, and input outlines.
- **Typography.kt**: Implements precise typographic scaling for titles, body, and status labels, utilizing proportional letter tracking and lineHeight.
- **ThemeExtensions.kt**: Seamlessly extends basic Material 3 styling to incorporate modern gradients, background glowing spheres, and semantic colors (Completed, Active, Warning, Danger).
- **AppTheme.kt**: Exposes an intuitive global reference wrapper (e.g., `AppTheme.colors`, `AppTheme.dimens`, `AppTheme.gradients`) that simplifies styling lookups within screen composables.
- **Theme.kt**: Coordinates color scheme selections based on dynamic color availability (Android 12+) and dark theme states. Offers direct `CompositionLocalProvider` injections for consistent system properties.

## Standard Usage

To access customized design extensions in any Jetpack Compose view:

```kotlin
import com.example.ui.material3_foundation.AppTheme

@Composable
fun MyComponent() {
    Box(
        modifier = Modifier
            .background(AppTheme.gradients.primary)
            .padding(AppTheme.dimens.SpaceMedium)
    ) {
        Text(
            text = "Elegant Foundation Design",
            style = AppTheme.typography.titleMedium,
            color = AppTheme.colors.onPrimary
        )
    }
}
```
