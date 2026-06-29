# Code Style: Smart Scheduler

## Principles
- Follow official **Kotlin Coding Conventions**.
- **Naming**:
  - Files: `PascalCase`.kt
  - Classes: `PascalCase`
  - Functions: `camelCase`
  - Variables/Properties: `camelCase`
  - Composables: `PascalCase`
- **KDoc**: All public functions, classes, and properties must have KDoc.
- **Imports**: Group imports (standard library, third-party, project-specific).
- **Compose**:
  - `@Composable` functions start with uppercase.
  - State hoisted as parameters.
  - Minimal state in `remember`.
- **Logging**: Use `DiagnosticLogger` for consistent application logging.
- **Coroutines**: Use `viewModelScope` for UI-related tasks, `CoroutineScope` with proper lifecycle management for background services.
