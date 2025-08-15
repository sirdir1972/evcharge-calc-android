# EVCharge Calc - Android

A mobile app for calculating EV charging energy requirements and timing.

## Purpose

This app is designed for **Electric Vehicle owners whose cars do not have built-in charge limiting features**. Many older EVs or basic models don't allow you to set a target charge level (like 80%) directly in the vehicle.

Instead, EV owners need to:
1. Calculate how much energy is needed to reach their desired charge level
2. Configure their EVSE (Electric Vehicle Supply Equipment/charging station) accordingly
3. Monitor and stop charging manually when the target is reached

## Features

- **Interactive SOC Controls**: Sliders and text fields to set current and target state of charge
- **Real-time Calculations**: Instant calculation of required energy including charging losses
- **Customizable Settings**: Adjust battery capacity, state of health, and charge losses
- **Quick Presets**: One-tap buttons for common charging scenarios (Daily, Road Trip, Top Up)
- **Material Design 3**: Modern Android UI with dynamic colors and smooth animations
- **Persistent Settings**: Settings are saved using SharedPreferences

## Architecture

### Core Components

1. **MainActivity.kt**: Entry point that hosts the Compose UI
2. **MainScreen.kt**: Main composable with all UI components and functionality
3. **SettingsScreen.kt**: Settings configuration screen
4. **SettingsManager.kt**: Handles data persistence and calculations
5. **Theme files**: Material Design 3 theming

### Key Features Ported from iOS

- **Battery Configuration**: Same calculation logic as iOS version
- **UI Layout**: Card-based design matching the iOS aesthetic
- **Preset Buttons**: Identical quick preset functionality
- **Validation**: Same input validation and error handling

## Technical Details

- **Minimum SDK**: Android 7.0 (API level 24)
- **Target SDK**: Android 14 (API level 34)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Data Persistence**: SharedPreferences
- **Architecture**: MVVM pattern with Compose state management

## Building the App

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK with API 34
- Kotlin 1.9.10 or later

### Build Instructions

1. Open Android Studio
2. Select "Open an existing project"
3. Navigate to the `EVChargeCalculatorAndroid` folder
4. Let Android Studio sync the project
5. Build and run the app on an emulator or physical device

### Gradle Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install and run on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

## Functionality Comparison with iOS

| Feature | iOS (SwiftUI) | Android (Compose) | Status |
|---------|---------------|-------------------|---------|
| SOC Sliders | ✅ | ✅ | Complete |
| Text Input Fields | ✅ | ✅ | Complete |
| Real-time Calculations | ✅ | ✅ | Complete |
| Settings Screen | ✅ | ✅ | Complete |
| Data Persistence | UserDefaults | SharedPreferences | Complete |
| Quick Presets | ✅ | ✅ | Complete |
| Material/Cupertino Design | ✅ | ✅ | Complete |
| Responsive Layout | ✅ | ✅ | Complete |
| Input Validation | ✅ | ✅ | Complete |

## Screenshots

The Android app features a clean, card-based design with:
- Header with app icon and title
- Main calculation card with SOC controls
- Results card showing energy requirements
- Settings preview card
- Quick preset buttons
- Full-screen settings with sliders and calculations

## Settings

The app allows users to configure:
- **Battery Capacity**: 10-200 kWh (adjustable in 0.5 kWh increments)
- **State of Health**: 50-100% (battery degradation factor)
- **Charge Losses**: 5-25% (charging inefficiency factor)

## Calculations

The app performs the same calculations as the iOS version:
- **Effective Capacity** = Battery Capacity × (State of Health / 100)
- **Base Energy** = Effective Capacity × (SOC Difference / 100)
- **Required Energy** = Base Energy × (1 + Charge Losses / 100)

## Future Enhancements

- Dark theme support
- Additional preset configurations
- Export/import settings
- Charging time estimation
- Multiple vehicle profiles
- Unit conversion (kWh/miles/km)

## License

This Android app is part of the EV Charge Calculator project and follows the same licensing as the iOS version.
