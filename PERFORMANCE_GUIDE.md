# Performance Guide: Smart Scheduler

## Compose Optimization
- **Recomposition**: Use `remember` and `derivedStateOf` to prevent unnecessary recompositions.
- **Stability**: Mark models as `@Stable` or `@Immutable` if necessary.
- **Lists**: Use `LazyColumn` and `LazyGrid` for lists, leveraging `key` in items to optimize updates.
- **State**: Use `collectAsStateWithLifecycle()` to prevent redundant state collection.

## Application Optimization
- **Startup**: Minimize work in `Application.onCreate()`.
- **Database**: Perform database operations on background threads using Room's native Coroutine/Flow support.
- **Images**: Use Coil for efficient image loading and caching.
- **Animations**: Use `Animatable` or `updateTransition` to offload work from the main thread.
- **Memory**: Proactively clear resources in `onCleared` or `onDestroy`.
