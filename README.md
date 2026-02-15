# LibGDX Console Color

A cross-platform UI implementation for the console-color game using libGDX. This implementation provides a reusable UI that works on Desktop and Android.

## Features

- **Cross-platform**: Runs on Desktop (LWJGL3) and Android
- **Main menu and settings**: Configure username, hostname, and port via in-game UI
- **Settings persistence**: Configuration saved across sessions
- **Color-coded rendering**: Supports the color mapping system from console-color
- **HUD system**: Includes crafting, equipment, and inventory interaction HUDs
- **Input handling**: Full keyboard support with arrow key navigation (Desktop)
- **Touch controls**: Virtual joystick and buttons for Android

## Project Structure

```
libgdx-console-color/
├── core/                    # Shared game logic and UI (reusable across platforms)
│   └── src/main/java/
│       ├── com/noiprocs/LibGDXApp.java                     # Main application
│       └── com/noiprocs/ui/libgdx/LibGDXGameScreen.java    # libGDX UI implementation
├── lwjgl3/                  # Desktop launcher (LWJGL3)
│   └── src/main/java/com/noiprocs/lwjgl3/Lwjgl3Launcher.java
├── android/                 # Android launcher
│   └── src/main/java/com/noiprocs/android/AndroidLauncher.java
├── assets/                  # Shared assets (fonts, images)
├── .env.template            # Environment variable template
└── Makefile                 # Build and run commands
```

## Setup

### 1. Install Dependencies

Install the required dependencies to your local Maven repository:

```bash
cd ../console-color-core && mvn install
cd ../console-color && mvn install
cd ../libgdx-console-color
```

**Required dependencies:**
- `console-color-core`: Core game logic
- `console-color`: Console UI components and sprite system

### 2. Build the Project

```bash
make build
```

## Configuration

The game features an in-game settings menu accessible from the main menu. Configure your username, hostname, and port via the "Settings" button. Settings are automatically saved and persisted across sessions.

## Running

### Desktop

```bash
make run
```

### Android

```bash
# Build and view APK location
make run-android

# Install to connected device
adb install -r android/build/outputs/apk/debug/android-debug.apk
```

## Controls

### In-Game
- **W/A/S/D**: Movement
- **Space**: Action/Attack
- **E**: Open equipment HUD

### HUD Navigation
- **Arrow Keys** or **W/A/S/D**: Navigate menu items
- **Enter**: Select/Confirm action
- **Tab**: Toggle between crafting and equipment HUDs
- **Escape**: Close current HUD
- **1-4** (in equipment HUD): Swap inventory slots

## Architecture

### Core Module (`core/`)
The core module contains platform-independent code:
- **LibGDXApp**: Main application class extending `Game`, manages screen lifecycle and shared resources
- **MainMenuScreen**: Scene2D-based main menu with Play, Settings, and Exit options
- **SettingsScreen**: Scene2D-based settings UI for configuring username, hostname, and port
- **GameScreen**: Wrapper screen that integrates the game rendering
- **LibGDXGameScreen**: Implements `GameScreenInterface` and renders the game using libGDX's SpriteBatch and BitmapFont
- **SettingsManager**: Manages persistent settings using LibGDX Preferences

### Desktop Module (`lwjgl3/`)
Desktop implementation using LWJGL3 backend with keyboard input support.

### Android Module (`android/`)
Android implementation with touch controls, including virtual joystick and on-screen buttons.

## Building for Production

### Desktop JAR
```bash
./gradlew lwjgl3:jar
# Output: lwjgl3/build/libs/libgdx-console-color-1.0.0.jar
```

### Android APK
```bash
./gradlew android:assembleRelease
# Output: android/build/outputs/apk/release/android-release-unsigned.apk
```

## Gradle Tasks

Useful Gradle tasks:
- `./gradlew lwjgl3:build`: Build desktop module
- `./gradlew lwjgl3:run`: Run desktop version
- `./gradlew android:assembleDebug`: Build Android debug APK
- `./gradlew clean`: Clean build artifacts
- `./gradlew test`: Run tests
- `./gradlew spotlessApply`: Format all Java code
- `./gradlew spotlessCheck`: Check code formatting

## Troubleshooting

### Dependencies not found
Run the dependency installation steps from the Setup section above.

### Game doesn't connect
Check that the server is running and the hostname/port are correct.

### Android build issues
Ensure you have Android SDK installed and `ANDROID_HOME` environment variable set.
