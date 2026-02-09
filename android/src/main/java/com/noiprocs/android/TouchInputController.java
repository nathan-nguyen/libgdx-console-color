package com.noiprocs.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.control.command.InputCommand;
import com.noiprocs.input.InputController;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;

/**
 * Touch-based input controller for Android platform.
 * Implements multi-touch virtual controls including D-pad, action buttons,
 * and HUD navigation.
 */
public class TouchInputController implements InputController {
  private final TouchState touchState;
  private final Viewport viewport;
  private final Vector3 touchPoint; // Reusable vector for coordinate conversion

  public TouchInputController(Viewport viewport) {
    this.touchState = new TouchState();
    this.viewport = viewport;
    this.touchPoint = new Vector3();
  }

  @Override
  public void handleInput(GameContext gameContext, LibGDXGameScreen gameScreen) {
    // Update HUD mode state
    touchState.updateHudMode(gameScreen);
    boolean hudMode = touchState.isHudMode();

    // Track which zones are currently touched this frame
    boolean[] zonesTouched = new boolean[ControlZone.values().length];

    // Process all active touch pointers (up to 5 simultaneous touches)
    for (int i = 0; i < 5; i++) {
      if (Gdx.input.isTouched(i)) {
        // Convert screen coordinates to virtual coordinates
        touchPoint.set(Gdx.input.getX(i), Gdx.input.getY(i), 0);
        viewport.unproject(touchPoint);

        float x = touchPoint.x;
        float y = touchPoint.y;

        // Find which zone this touch is in
        ControlZone zone = ControlZone.findZone(x, y, hudMode);
        if (zone != null) {
          touchState.updatePointer(i, zone);
          zonesTouched[zone.ordinal()] = true;
        } else {
          touchState.updatePointer(i, null);
        }
      } else {
        // Pointer is not active
        touchState.updatePointer(i, null);
      }
    }

    // Process commands based on active zones
    if (hudMode) {
      handleHudInput(gameContext, gameScreen, zonesTouched);
    } else {
      handleGameInput(gameContext, gameScreen, zonesTouched);
    }
  }

  /**
   * Handle game input (movement, actions, quick slots).
   */
  private void handleGameInput(GameContext gameContext, LibGDXGameScreen gameScreen, boolean[] zonesTouched) {
    // Track if any movement is currently active
    boolean anyMovementActive = false;

    // Check for equipment button (E) to open inventory - handle separately
    if (zonesTouched[ControlZone.QUICK_EQUIPMENT.ordinal()]) {
      if (!touchState.isCommandPressed('e')) {
        touchState.pressCommand('e');
        gameScreen.hud.equipmentHud.open();
      }
    } else {
      if (touchState.isCommandPressed('e')) {
        touchState.clearCommand('e');
      }
    }

    // Process each zone and send commands for newly pressed zones
    for (ControlZone zone : ControlZone.values()) {
      if (!zone.isGameControl() || zone == ControlZone.QUICK_EQUIPMENT) {
        continue; // Skip HUD controls and equipment button (already handled)
      }

      char command = zone.getCommand();
      boolean isTouched = zonesTouched[zone.ordinal()];

      // Track if this is a movement zone
      if (zone.isDpad() && isTouched) {
        anyMovementActive = true;
      }

      if (isTouched) {
        // For all controls, send command if not already pressed
        if (!touchState.isCommandPressed(command)) {
          touchState.pressCommand(command);
          gameContext.controlManager.processInput(
              new InputCommand(gameContext.username, command));
        }
      } else {
        // Clear specific command if no longer touched
        if (touchState.isCommandPressed(command)) {
          touchState.clearCommand(command);
        }
      }
    }

    // Send halt command if movement stopped (D-pad released)
    if (touchState.wasMovementActive() && !anyMovementActive) {
      gameContext.controlManager.processInput(new InputCommand(gameContext.username, "h"));
    }

    // Update movement state for next frame
    touchState.updateMovementState(anyMovementActive);
  }

  /**
   * Handle HUD input (navigation, confirm, close, tab).
   */
  private void handleHudInput(
      GameContext gameContext, LibGDXGameScreen gameScreen, boolean[] zonesTouched) {
    TouchState.HudType hudType = touchState.getHudType(gameScreen);

    // Handle crafting HUD
    if (hudType == TouchState.HudType.CRAFTING) {
      handleCraftingHudInput(gameScreen, zonesTouched);
      return;
    }

    // Handle equipment HUD
    if (hudType == TouchState.HudType.EQUIPMENT) {
      handleEquipmentHudInput(gameContext, gameScreen, zonesTouched);
      return;
    }

    // Handle chest interaction HUD
    if (hudType == TouchState.HudType.CHEST) {
      handleChestHudInput(gameScreen, zonesTouched);
      return;
    }
  }

  /**
   * Handle crafting HUD input.
   */
  private void handleCraftingHudInput(LibGDXGameScreen gameScreen, boolean[] zonesTouched) {
    // Check for close button
    if (zoneWasTapped(ControlZone.HUD_CLOSE, zonesTouched)) {
      gameScreen.hud.craftingHud.close();
      touchState.clear();
      return;
    }

    // Check for tab button
    if (zoneWasTapped(ControlZone.HUD_TAB, zonesTouched)) {
      gameScreen.hud.craftingHud.close();
      gameScreen.hud.equipmentHud.open();
      touchState.clear();
      return;
    }

    // Check for confirm button
    if (zoneWasTapped(ControlZone.HUD_CONFIRM, zonesTouched)) {
      gameScreen.hud.craftingHud.craftSelectedItem();
      return;
    }

    // Navigation
    if (zoneWasTapped(ControlZone.HUD_UP, zonesTouched)) {
      gameScreen.hud.craftingHud.handleNavigation('w');
    }
    if (zoneWasTapped(ControlZone.HUD_DOWN, zonesTouched)) {
      gameScreen.hud.craftingHud.handleNavigation('s');
    }
    if (zoneWasTapped(ControlZone.HUD_LEFT, zonesTouched)) {
      gameScreen.hud.craftingHud.handleNavigation('a');
    }
    if (zoneWasTapped(ControlZone.HUD_RIGHT, zonesTouched)) {
      gameScreen.hud.craftingHud.handleNavigation('d');
    }
  }

  /**
   * Handle equipment HUD input.
   */
  private void handleEquipmentHudInput(
      GameContext gameContext, LibGDXGameScreen gameScreen, boolean[] zonesTouched) {
    // Check for close button
    if (zoneWasTapped(ControlZone.HUD_CLOSE, zonesTouched)) {
      gameScreen.hud.equipmentHud.close();
      touchState.clear();
      return;
    }

    // Check for tab button
    if (zoneWasTapped(ControlZone.HUD_TAB, zonesTouched)) {
      gameScreen.hud.equipmentHud.close();
      gameScreen.hud.craftingHud.open();
      touchState.clear();
      return;
    }

    // Check for confirm button
    if (zoneWasTapped(ControlZone.HUD_CONFIRM, zonesTouched)) {
      gameScreen.hud.equipmentHud.handleEquipmentAction();
      return;
    }

    // Navigation
    if (zoneWasTapped(ControlZone.HUD_UP, zonesTouched)) {
      gameScreen.hud.equipmentHud.handleNavigation('w');
    }
    if (zoneWasTapped(ControlZone.HUD_DOWN, zonesTouched)) {
      gameScreen.hud.equipmentHud.handleNavigation('s');
    }
    if (zoneWasTapped(ControlZone.HUD_LEFT, zonesTouched)) {
      gameScreen.hud.equipmentHud.handleNavigation('a');
    }
    if (zoneWasTapped(ControlZone.HUD_RIGHT, zonesTouched)) {
      gameScreen.hud.equipmentHud.handleNavigation('d');
    }

    // Handle inventory slot swapping (1-4 keys)
    if (!gameScreen.hud.equipmentHud.isEquipmentSelected()) {
      for (int i = 0; i < 4; i++) {
        ControlZone slotZone = ControlZone.values()[ControlZone.QUICK_SLOT_1.ordinal() + i];
        if (zoneWasTapped(slotZone, zonesTouched)) {
          int currentSlot = gameScreen.hud.equipmentHud.getSelectedSlot();
          if (currentSlot != i) {
            gameContext.controlManager.swapInventorySlots(currentSlot, i);
          }
        }
      }
    }
  }

  /**
   * Handle chest interaction HUD input.
   */
  private void handleChestHudInput(LibGDXGameScreen gameScreen, boolean[] zonesTouched) {
    // Check for close button
    if (zoneWasTapped(ControlZone.HUD_CLOSE, zonesTouched)) {
      gameScreen.hud.inventoryInteractionHud.close();
      touchState.clear();
      return;
    }

    // Check for confirm button
    if (zoneWasTapped(ControlZone.HUD_CONFIRM, zonesTouched)) {
      gameScreen.hud.inventoryInteractionHud.transferSelectedItem();
      return;
    }

    // Check for equipment action button
    if (zoneWasTapped(ControlZone.HUD_EQUIPMENT_ACTION, zonesTouched)) {
      gameScreen.hud.inventoryInteractionHud.handleEquipmentAction();
      return;
    }

    // Navigation
    if (zoneWasTapped(ControlZone.HUD_UP, zonesTouched)) {
      gameScreen.hud.inventoryInteractionHud.handleNavigation('w');
    }
    if (zoneWasTapped(ControlZone.HUD_DOWN, zonesTouched)) {
      gameScreen.hud.inventoryInteractionHud.handleNavigation('s');
    }
    if (zoneWasTapped(ControlZone.HUD_LEFT, zonesTouched)) {
      gameScreen.hud.inventoryInteractionHud.handleNavigation('a');
    }
    if (zoneWasTapped(ControlZone.HUD_RIGHT, zonesTouched)) {
      gameScreen.hud.inventoryInteractionHud.handleNavigation('d');
    }
  }

  /**
   * Check if a zone was tapped (touched this frame and not pressed before).
   */
  private boolean zoneWasTapped(ControlZone zone, boolean[] zonesTouched) {
    boolean touched = zonesTouched[zone.ordinal()];
    char command = zone.getCommand();

    if (touched && !touchState.isCommandPressed(command)) {
      touchState.pressCommand(command);
      return true;
    }

    // Clear specific command if no longer touched
    if (!touched && touchState.isCommandPressed(command)) {
      touchState.clearCommand(command);
    }

    return false;
  }

  /**
   * Get the touch state for rendering purposes.
   */
  public TouchState getTouchState() {
    return touchState;
  }
}
