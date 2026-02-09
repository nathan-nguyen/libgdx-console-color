# Load environment variables from .env file if it exists
-include .env

# Export variables for sub-make calls
export

# Gradle command
GRADLE = ./gradlew

.PHONY: help build clean test run run-client run-server run-android format check-format

# Default target
help:
	@echo "Available targets:"
	@echo "  make build         - Clean and build the project"
	@echo "  make compile       - Compile without cleaning"
	@echo "  make test          - Run tests"
	@echo "  make format        - Format all Java source files"
	@echo "  make check-format  - Check if code is formatted correctly"
	@echo "  make run           - Run desktop version with current settings (TYPE=$(TYPE))"
	@echo "  make run-client    - Run as client (desktop)"
	@echo "  make run-server    - Run as server (desktop)"
	@echo "  make run-android   - Build and install Android APK"
	@echo "  make clean         - Clean build artifacts"
	@echo ""
	@echo "Configuration variables:"
	@echo "  PLATFORM=$(PLATFORM) USERNAME=$(USERNAME) TYPE=$(TYPE)"
	@echo "  HOSTNAME=$(HOSTNAME) PORT=$(PORT)"
	@echo ""
	@echo "Example: make run USERNAME=alice HOSTNAME=192.168.1.100 PORT=9090"

build:
	$(GRADLE) clean lwjgl3:build

compile:
	$(GRADLE) lwjgl3:build

test:
	$(GRADLE) test

clean:
	$(GRADLE) clean

run:
	$(GRADLE) lwjgl3:run --args="$(PLATFORM) $(USERNAME) $(TYPE) $(HOSTNAME) $(PORT)"

run-client:
	$(MAKE) run TYPE=client

run-server:
	$(MAKE) run TYPE=server

run-android:
	$(GRADLE) android:assembleDebug
	@echo "APK built at: android/build/outputs/apk/debug/android-debug.apk"
	@echo "To install: adb install -r android/build/outputs/apk/debug/android-debug.apk"

format:
	$(GRADLE) spotlessApply

check-format:
	$(GRADLE) spotlessCheck
