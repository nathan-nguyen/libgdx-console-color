package com.noiprocs.android;

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

  // Set of commands currently pressed (for debouncing)
  private final Set<Character> commandsPressed = new HashSet<>();

  // Track if we're in HUD mode
  private boolean hudMode = false;

  // Track if movement was active in the previous frame
  private boolean wasMovementActive = false;

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
   * Clear all state (for when transitioning between modes).
   */
  public void clear() {
    activePointers.clear();
    commandsPressed.clear();
    wasMovementActive = false;
  }

  /** Enum representing different HUD types. */
  public enum HudType {
    NONE,
    EQUIPMENT,
    CRAFTING,
    CHEST
  }
}
