# Gradle command
GRADLE = ./gradlew

.PHONY: help build clean test run run-android format check-format

# Default target
help:
	@echo "Available targets:"
	@echo "  make build         - Clean and build the project"
	@echo "  make compile       - Compile without cleaning"
	@echo "  make test          - Run tests"
	@echo "  make format        - Format all Java source files"
	@echo "  make check-format  - Check if code is formatted correctly"
	@echo "  make run           - Run desktop version"
	@echo "  make run-android   - Build and install Android APK"
	@echo "  make clean         - Clean build artifacts"
	@echo ""
	@echo "Note: Username, hostname, and port are now configured via the in-game settings menu."

build:
	$(GRADLE) clean lwjgl3:build

compile:
	$(GRADLE) lwjgl3:build

test:
	$(GRADLE) test

clean:
	$(GRADLE) clean

run:
	$(GRADLE) lwjgl3:run

run-android:
	$(GRADLE) android:assembleDebug
	@echo "APK built at: android/build/outputs/apk/debug/android-debug.apk"
	@echo "To install: adb install -r android/build/outputs/apk/debug/android-debug.apk"

format:
	$(GRADLE) spotlessApply

check-format:
	$(GRADLE) spotlessCheck
