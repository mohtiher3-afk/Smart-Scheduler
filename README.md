# Smart Scheduler 📚✨

An advanced, feature-rich Android application built with modern Kotlin and Jetpack Compose to orchestrate, schedule, and track academic and professional courses. Powered by the Gemini API, it dynamically translates natural-language prompts into perfectly organized, structured course timetables.

## 🌟 Key Features

### 1. 🤖 AI-Powered Course Scheduling
- **Natural Language Parsing**: Just input standard scheduling sentences like *"Add Python course on Sundays and Tuesdays from 4 PM to 6 PM with 12 lectures"*, and the AI engine automatically parses it into discrete sessions.
- **Robust Fallback Mechanism**: Built-in intelligent model selection and fallback logic to guarantee seamless schedule generation.

### 2. 📅 Course & Lecture Calculator
- Plan, compute, and visualize exact starting dates, intermediate milestones, and final graduation timings.
- Automatically handles recurring days of the week and counts holidays or custom pauses gracefully.

### 3. 🎥 Zoom Live Broadcast Organizer
- Connect and assign dedicated Zoom links, accounts, and streaming details to each course.
- Quick-launch links directly from the dashboard card.

### 4. 🔔 Smart Notification & Calendar Sync
- **Local Reminder Engine**: Built-in notifications tailored to alert you before broadcasts begin.
- **Android Calendar Integration**: Seamlessly export class times as events directly to your device's native system calendar.

### 5. 🌐 Fully Bilingual Support (العربية & English)
- Fluid and context-aware localization engine switching beautifully between Arabic and English text layouts.

### 6. 🎨 Premium Material 3 Interface
- **Cosmic Dark Theme**: Built using a modern, eyesafe color palette prioritizing comfortable negative space and precise text tracking.
- **Adaptive Screen Design**: Fluid, responsive layout structured with Canonical List-Detail view guidelines.

---

## 🛠️ Built With

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose (Material 3)
- **Local Database**: Room DB for local persistence of course logs and history
- **AI Model**: Gemini API (via server-side or local integration)
- **Architecture**: MVVM (Model-View-ViewModel) + Coroutines Flow for responsive state updates

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer.
- JDK 17+
- Android SDK 34+

### Setup API Keys
The application utilizes the Gemini API for natural-language parsing. 
1. Obtain an API Key from Google AI Studio.
2. Put the key inside your `.env` file (or set `GEMINI_API_KEY` environment variable):
   ```env
   GEMINI_API_KEY=your_actual_api_key_here
   ```

### Compile & Build
To build and compile the APK directly:
```bash
gradle assembleDebug
```
