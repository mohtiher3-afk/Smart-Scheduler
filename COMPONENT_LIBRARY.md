# Component Library: Smart Scheduler

This library defines the reusable UI components for the Smart Scheduler application. All custom UI components MUST leverage M3 components internally.

## Core Components
- `SmartButton`: Wrapper for `Button` (Filled/Tonal/Outlined).
- `SmartCard`: Wrapper for `Card`.
- `SmartFAB`: Wrapper for `FloatingActionButton`.
- `SmartTopBar`: Wrapper for `TopAppBar`.
- `SmartBottomBar`: Wrapper for `NavigationBar`.
- `SmartSearchBar`: Wrapper for `SearchBar`.
- `SmartTextField`: Wrapper for `OutlinedTextField`.
- `SmartDialog`: Wrapper for `AlertDialog` / `ModalBottomSheet`.
- `SmartChip`: Wrapper for `FilterChip` / `InputChip`.

## State Components
- `LoadingState`: Standard circular progress indicator layout.
- `EmptyState`: Standard illustration/text layout for empty lists.
- `ErrorState`: Standard error message layout with retry button.

## Usage Rule
- **REUSE BEFORE CREATE**: Before creating a new component, check if an existing one in this library fits the requirement.
- **CONSISTENCY**: All components must share design tokens (spacing, typography, color).
