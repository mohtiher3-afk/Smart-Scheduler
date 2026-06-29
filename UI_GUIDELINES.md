# UI Guidelines: Smart Scheduler

## Design Philosophy
The application follows Material Design 3 (M3) principles, ensuring a consistent, accessible, and modern user experience.

## Design System Principles
- **Material You**: Leverage dynamic color systems (`dynamicLightColorScheme`/`dynamicDarkColorScheme`).
- **Typography**: Use M3 type scale.
- **Shapes**: Consistent use of rounded corners (extra small, small, medium, large, extra large).
- **Spacing**: Follow the 8dp grid system.
- **Elevation**: Use M3 elevation tokens (`surfaceContainerLow`, `surfaceContainer`, `surfaceContainerHigh`, etc.).

## Motion
- Use Material Motion for transitions between screens and component state changes.
- Consistent duration and easing (standard, linear, emphasized).

## Layout
- **Edge-to-Edge**: Enable edge-to-edge support.
- **Adaptive UI**: Implement responsive layouts using Window Size Classes (Compact, Medium, Expanded).
- **Navigation**: Use NavigationBar (compact) and NavigationRail (expanded) for adaptive navigation.

## Component Standards
- Buttons: Filled, Tonal, Outlined, Text (M3 standard).
- Search: Use M3 SearchBar.
- Cards: M3 elevated/outlined cards.
- FAB: Standard M3 Floating Action Button.
