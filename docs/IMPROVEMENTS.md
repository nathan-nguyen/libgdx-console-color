# Future Improvements

## 2. ThrowAimState is static mutable global state

**Files**: `core/src/main/java/com/noiprocs/input/ThrowAimState.java`, `android/src/main/java/com/noiprocs/android/TouchInputController.java`, `core/src/main/java/com/noiprocs/ui/libgdx/sprite/mob/character/PlayerSprite.java`

**Issue**: `ThrowAimState` is a static mutable class acting as a global variable to share the throw aim direction from `TouchInputController` (writer) to `PlayerSprite` (reader). Static mutable state is bad practice.

---

## 1. Hardcoded Configuration Values in AndroidLauncher

**File**: `android/src/main/java/com/noiprocs/android/AndroidLauncher.java`

**Issue**: Username ("player") and hostname ("192.168.50.49") are hardcoded as default values (lines 26-27). These should be configurable by users instead of being hardcoded in the source.
