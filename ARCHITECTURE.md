# Architecture: Smart Scheduler

## Pattern
The application utilizes the **MVVM (Model-View-ViewModel)** pattern.

## Components
- **View (Compose UI)**: Defines UI structure using Composables. Consumes state from ViewModels.
- **ViewModel**: Manages UI state using `StateFlow` and exposes it via `StateFlow` to Composables. Collects state using `collectAsStateWithLifecycle()`.
- **Repository**: Acts as a single source of truth for data. Coordinates between local (Room) and remote (Firebase) data sources.
- **Services/Managers**: Encapsulate background business logic (Scheduler, Sync, Alarms).

## Rules
- **No Business Logic in UI**: UI must only reflect state.
- **Immutability**: All UI state models must be immutable.
- **State Hoisting**: Hoist state to the lowest common ancestor.
- **Dependency Injection**: Use constructor injection for ViewModels and Repositories.
