# TripMate

## About

TripMate is an Android travel planning application developed using **Kotlin** and **Jetpack Compose**. It provides a simple way to organise trips, plan daily activities, track travel expenses, and manage packing checklists in one place.

All data is stored locally on the device, allowing users to manage their travel plans offline without requiring an internet connection or user account.

## Features

| Feature | Description |
|---|---|
| **Trip Management** | Create, view, and delete trips. |
| **Trip Details** | Store trip names, destinations, travel dates, budgets, and notes. |
| **Itinerary Planning** | Add and delete activities for different days of a trip. |
| **Budget Tracking** | Set trip budgets, record expenses, and monitor the remaining balance. |
| **Packing Checklist** | Add packing items, mark them as packed, and delete items. |
| **Local Data Storage** | Store trip information and related data locally using Room Database. |
| **Navigation** | Navigate between the Home, Trips, Budget, and Packing sections. |
| **User Interface** | Enjoy a modern interface featuring pastel colours, rounded cards, and organised layouts. |

## Technologies Used

| Technology | Purpose |
|---|---|
| Kotlin | Main programming language |
| Jetpack Compose | UI development |
| Material Design 3 | UI design system |
| Room Database | Local data persistence |
| Kotlin Coroutines & Flow | Asynchronous operations and reactive data handling |
| ViewModel | UI-related data management |
| Android Lifecycle Components | Lifecycle-aware application components |
| Gradle (Kotlin DSL) | Build configuration and dependency management |
| Android Studio | Development environment |

## Requirements

Before running the project, ensure you have the following:

- **Android Studio**
- **Android SDK**
- **JDK 11** or a compatible JDK supported by the project's Gradle configuration
- An Android emulator or physical device running **Android 8.0 (API 26)** or higher
- **Git** (optional, for cloning the repository)

## Getting Started

Follow these steps to set up and run TripMate locally.

### 1. Clone the Repository

Open a terminal and run:

```bash
git clone https://github.com/YOUR-USERNAME/TripMate.git
```

### 2. Open the Project

1. Launch Android Studio.
2. Select **Open**.
3. Navigate to the cloned `TripMate` project folder.
4. Wait for Gradle Sync to complete.

### 3. Run the Application

1. Start an Android emulator or connect a physical Android device.
2. Select your device from the available deployment targets.
3. Click the **Run** button in Android Studio.
4. Wait for the application to build, install, and launch.

## Project Structure

The project follows a simple structure that separates data-related components from the user interface.

```text
TripMate/
│
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/tripmate/
│           │   ├── data/
│           │   │   ├── Trip.kt
│           │   │   ├── TripActivity.kt
│           │   │   ├── Expense.kt
│           │   │   ├── PackingItem.kt
│           │   ├── TripDao.kt
│           │   └── TripDatabase.kt
│           │
│           ├── ui/
│           │   └── theme/
│           │
│           ├── MainActivity.kt
│           │
│           └── AndroidManifest.xml
│
├── gradle/
│   └── libs.versions.toml
│
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Data Storage

TripMate uses **Room Database** to persist application data locally on the device.

The database consists of four main entities:

| Entity | Description |
|---|---|
| **Trip** | Stores general trip information, including destination, dates, budget, and notes. |
| **TripActivity** | Stores itinerary activities associated with a trip. |
| **Expense** | Stores expenses associated with a trip. |
| **PackingItem** | Stores packing checklist items associated with a trip. |

All application data is stored locally. No external backend, Firebase service, or API is required.

## Permissions and Connectivity

TripMate does not require an internet connection or user account registration for its core functionality. Users can manage their trips and related information directly on their devices.


