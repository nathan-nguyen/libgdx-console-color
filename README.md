# LibGDX Console Color

A cross-platform UI implementation for the console-color game using libGDX. This implementation provides a reusable UI that works on Desktop and Android.

## Features

- **Cross-platform**: Runs on Desktop (LWJGL3) and Android
- **Color-coded rendering**: Supports the color mapping system from console-color
- **HUD system**: Includes crafting, equipment, and inventory interaction HUDs
- **Input handling**: Full keyboard support with arrow key navigation (Desktop)

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

Copy `.env.template` to `.env` and customize your defaults:

```bash
PLATFORM=pc
USERNAME=noiprocs
TYPE=client
HOSTNAME=localhost
PORT=8080
```

The Makefile will automatically load these values from `.env` if it exists.

## Running

### Desktop

```bash
# Run as client (default)
make run-client

# Run as server
make run-server

# Run with custom configuration
make run USERNAME=alice HOSTNAME=192.168.1.100 PORT=9090
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
- **LibGDXApp**: Main application class that handles game lifecycle, input, and rendering
- **LibGDXGameScreen**: Implements `GameScreenInterface` and renders the game using libGDX's SpriteBatch and BitmapFont

### Desktop Module (`lwjgl3/`)
Desktop implementation using LWJGL3 backend. The launcher accepts command-line arguments and instantiates the core `LibGDXApp` class.

### Android Module (`android/`)
Android implementation using Android backend. The launcher receives configuration via Intent extras or uses defaults.

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
- `./gradlew lwjgl3:run --args="pc noiprocs client localhost 8080"`: Run desktop version
- `./gradlew android:assembleDebug`: Build Android debug APK
- `./gradlew clean`: Clean build artifacts
- `./gradlew test`: Run tests

## Troubleshooting

### Dependencies not found
Run the dependency installation steps from the Setup section above.

### Game doesn't connect
Check that the server is running and the hostname/port are correct.

### Android build issues
Ensure you have Android SDK installed and `ANDROID_HOME` environment variable set.
