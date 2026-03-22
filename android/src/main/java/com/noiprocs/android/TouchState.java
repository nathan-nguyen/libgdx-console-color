package com.noiprocs.android;

import com.badlogic.gdx.math.Vector2;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks active touch pointers, their zones, and command state for debouncing. Handles HUD mode
 * detection and state management.
 */
public class TouchState {
  // Maps pointer ID to the zone it's currently touching
  private final Map<Integer, ControlZone> activePointers = new HashMap<>();

  // Set of commands currently pressed (for debouncing)
  private final Set<Character> commandsPressed = new HashSet<>();

  // Track if movement was active in the previous frame
  private boolean wasMovementActive = false;

  // Joystick state
  private final Vector2 joystickPosition = new Vector2(); // Current touch position on joystick
  private final Vector2 joystickOffset = new Vector2(); // Normalized offset from center
  private boolean joystickActive = false; // Whether joystick is being touched
  private Integer joystickPointerId = null; // Which pointer owns the joystick

  // Throw aim joystick state
  private final Vector2 throwAimOffset =
      new Vector2(); // Normalized offset from ACTION_TOGGLE center
  private Integer throwAimPointerId = null; // Which pointer owns the throw aim joystick

  /**
   * Update the touch state for a pointer.
   *
   * @param pointerId The touch pointer ID
   * @param zone The zone being touched, or null if no zone
   */
  public void updatePointer(int pointerId, ControlZone zone) {
    if (zone == null) {
      activePointers.remove(pointerId);
    } else {
      activePointers.put(pointerId, zone);
    }
  }

  /** Check if a command is currently pressed (for debouncing). */
  public boolean isCommandPressed(char command) {
    return commandsPressed.contains(command);
  }

  /** Mark a command as pressed. */
  public void pressCommand(char command) {
    commandsPressed.add(command);
  }

  /** Clear a specific command. */
  public void clearCommand(char command) {
    commandsPressed.remove(command);
  }

  /** Get all currently active zones. */
  public Set<ControlZone> getActiveZones() {
    return new HashSet<>(activePointers.values());
  }

  /** Update whether movement was active (call at end of frame). */
  public void updateMovementState(boolean isMovementActive) {
    wasMovementActive = isMovementActive;
  }

  /** Check if movement was active in the previous frame. */
  public boolean wasMovementActive() {
    return wasMovementActive;
  }

  /** Update joystick state. */
  public void updateJoystick(float x, float y, Vector2 offset) {
    joystickPosition.set(x, y);
    joystickOffset.set(offset);
    joystickActive = offset.len() > 0;
  }

  /** Clear joystick state. */
  public void clearJoystick() {
    joystickActive = false;
    joystickOffset.set(0, 0);
    joystickPointerId = null;
  }

  /** Get the joystick offset for rendering. */
  public Vector2 getJoystickOffset() {
    return joystickOffset;
  }

  /** Check if joystick is active. */
  public boolean isJoystickActive() {
    return joystickActive;
  }

  /** Set which pointer owns the joystick. */
  public void setJoystickPointer(int pointerId) {
    joystickPointerId = pointerId;
  }

  /** Get the pointer ID that owns the joystick. */
  public Integer getJoystickPointerId() {
    return joystickPointerId;
  }

  /** Check if a pointer is the joystick pointer. */
  public boolean isJoystickPointer(int pointerId) {
    return joystickPointerId != null && joystickPointerId == pointerId;
  }

  // --- Throw aim joystick ---

  /** Set which pointer owns the throw aim joystick. */
  public void setThrowAimPointer(int pointerId) {
    throwAimPointerId = pointerId;
  }

  /** Check if a pointer is the throw aim pointer. */
  public boolean isThrowAimPointer(int pointerId) {
    return throwAimPointerId != null && throwAimPointerId == pointerId;
  }

  /** Get the pointer ID that owns the throw aim joystick, or null if none. */
  public Integer getThrowAimPointerId() {
    return throwAimPointerId;
  }

  /** Returns true while the throw aim pointer is held down (even inside dead zone). */
  public boolean isThrowAimPointerActive() {
    return throwAimPointerId != null;
  }

  /** Get the current throw aim offset (normalized). */
  public Vector2 getThrowAimOffset() {
    return throwAimOffset;
  }

  /** Update throw aim state from current pointer position. */
  public void updateThrowAim(Vector2 offset) {
    throwAimOffset.set(offset);
  }

  /** Clear throw aim state (pointer released). */
  public void clearThrowAim() {
    throwAimOffset.set(0, 0);
    throwAimPointerId = null;
  }
}
