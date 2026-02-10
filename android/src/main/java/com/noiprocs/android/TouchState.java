package com.noiprocs.android;

import com.badlogic.gdx.math.Vector2;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks active touch pointers, their zones, and command state for debouncing.
 * Handles HUD mode detection and state management.
 */
public class TouchState {
  // Maps pointer ID to the zone it's currently touching
  private final Map<Integer, ControlZone> activePointers = new HashMap<>();

  // Maps pointer ID to the zone it was touching in the previous frame
  private final Map<Integer, ControlZone> previousPointers = new HashMap<>();

  // Set of commands currently pressed (for debouncing)
  private final Set<Character> commandsPressed = new HashSet<>();

  // Track if we're in HUD mode
  private boolean hudMode = false;

  // Track if movement was active in the previous frame
  private boolean wasMovementActive = false;

  // Joystick state
  private Vector2 joystickPosition = new Vector2(); // Current touch position on joystick
  private Vector2 joystickOffset = new Vector2();   // Normalized offset from center
  private boolean joystickActive = false;           // Whether joystick is being touched

  /**
   * Save current pointer state before processing new frame.
   * Call this at the start of each input frame.
   */
  public void savePointerState() {
    previousPointers.clear();
    previousPointers.putAll(activePointers);
  }

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

  /**
   * Check if a pointer was just released from a specific zone.
   * Returns true if the pointer was in this zone last frame but is not touched this frame.
   *
   * @param zone The zone to check
   * @return true if any pointer was released from this zone
   */
  public boolean wasZoneReleased(ControlZone zone) {
    for (Map.Entry<Integer, ControlZone> entry : previousPointers.entrySet()) {
      if (entry.getValue() == zone && !activePointers.containsKey(entry.getKey())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if a command is currently pressed (for debouncing).
   */
  public boolean isCommandPressed(char command) {
    return commandsPressed.contains(command);
  }

  /**
   * Mark a command as pressed.
   */
  public void pressCommand(char command) {
    commandsPressed.add(command);
  }

  /**
   * Clear all pressed commands.
   */
  public void clearCommands() {
    commandsPressed.clear();
  }

  /**
   * Clear a specific command.
   */
  public void clearCommand(char command) {
    commandsPressed.remove(command);
  }

  /**
   * Get all currently active zones.
   */
  public Set<ControlZone> getActiveZones() {
    return new HashSet<>(activePointers.values());
  }

  /**
   * Check if any pointers are active.
   */
  public boolean hasActivePointers() {
    return !activePointers.isEmpty();
  }

  /**
   * Check if any movement commands are active (WASD).
   */
  public boolean hasMovementActive() {
    for (ControlZone zone : activePointers.values()) {
      char cmd = zone.getCommand();
      if (cmd == 'w' || cmd == 'a' || cmd == 's' || cmd == 'd') {
        return true;
      }
    }
    return false;
  }

  /**
   * Update HUD mode based on game screen state.
   */
  public void updateHudMode(LibGDXGameScreen gameScreen) {
    hudMode =
        gameScreen.hud.equipmentHud.isOpen()
            || gameScreen.hud.craftingHud.isOpen()
            || gameScreen.hud.inventoryInteractionHud.isChestOpen();
  }

  /**
   * Check if we're currently in HUD mode.
   */
  public boolean isHudMode() {
    return hudMode;
  }

  /**
   * Get the specific HUD type currently open.
   */
  public HudType getHudType(LibGDXGameScreen gameScreen) {
    if (gameScreen.hud.equipmentHud.isOpen()) {
      return HudType.EQUIPMENT;
    } else if (gameScreen.hud.craftingHud.isOpen()) {
      return HudType.CRAFTING;
    } else if (gameScreen.hud.inventoryInteractionHud.isChestOpen()) {
      return HudType.CHEST;
    }
    return HudType.NONE;
  }

  /**
   * Update whether movement was active (call at end of frame).
   */
  public void updateMovementState(boolean isMovementActive) {
    wasMovementActive = isMovementActive;
  }

  /**
   * Check if movement was active in the previous frame.
   */
  public boolean wasMovementActive() {
    return wasMovementActive;
  }

  /**
   * Update joystick state.
   */
  public void updateJoystick(float x, float y, Vector2 offset) {
    joystickPosition.set(x, y);
    joystickOffset.set(offset);
    joystickActive = offset.len() > 0;
  }

  /**
   * Clear joystick state.
   */
  public void clearJoystick() {
    joystickActive = false;
    joystickOffset.set(0, 0);
  }

  /**
   * Get the joystick offset for rendering.
   */
  public Vector2 getJoystickOffset() {
    return joystickOffset;
  }

  /**
   * Check if joystick is active.
   */
  public boolean isJoystickActive() {
    return joystickActive;
  }

  /**
   * Clear all state (for when transitioning between modes).
   */
  public void clear() {
    activePointers.clear();
    commandsPressed.clear();
    wasMovementActive = false;
    clearJoystick();
  }

  /** Enum representing different HUD types. */
  public enum HudType {
    NONE,
    EQUIPMENT,
    CRAFTING,
    CHEST
  }
}
