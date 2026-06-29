# Project Context: Smart Scheduler

## Application Overview
Smart Scheduler is an Android application designed to manage courses, schedules, reminders, and tasks. It leverages advanced features like AI-based scheduling (Gemini API) and calendar integration.

## Key Features
- **Course Management**: Add, edit, and view courses.
- **Schedule Management**: Organized view of daily/weekly schedules.
- **Reminder System**: Alarm-based reminders for tasks and sessions.
- **Calendar Integration**: Sync with the system calendar.
- **AI Scheduler**: Gemini-powered scheduling assistance.
- **Data Persistence**: Local database using Room.
- **Cloud Sync**: Firebase integration for data synchronization.
- **Backup/Export**: Local storage backups and CSV export.

## Architecture
The application follows an MVVM (Model-View-ViewModel) architectural pattern, utilizing:
- **Repositories**: Data abstraction layer.
- **ViewModels**: UI state management and business logic orchestration.
- **Room**: Local database for entities like `Course`, `ReminderEntity`.
- **Services**: Background tasks (`AlarmReceiver`, `CloudSyncManager`).

## Project Structure
- `com.example.models`: Data entities.
- `com.example.screens`: Main UI screens and ViewModels.
- `com.example.services`: Business logic, database, and background services.
- `com.example.ui`: UI features, themes, and design system.
- `com.example.widgets`: Reusable UI components.

## Constraints
- **Maintain Current Architecture**: Do not replace MVVM or Room unless technically justified.
- **Business Logic**: Must remain intact.
- **Compatibility**: Ensure compatibility with existing Firebase and Gemini implementations.
