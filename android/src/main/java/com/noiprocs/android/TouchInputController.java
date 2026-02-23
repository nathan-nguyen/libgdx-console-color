package com.noiprocs.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.control.command.InputCommand;
import com.noiprocs.core.control.command.SetMovingDirectionCommand;
import com.noiprocs.input.InputController;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import com.noiprocs.ui.libgdx.hud.HUDManager;

/**
 * Touch-based input controller for Android platform. Implements multi-touch virtual controls
 * including D-pad, action buttons, and HUD navigation.
 */
public class TouchInputController implements InputController {
  private final TouchState touchState;
  private final Viewport viewport;
  private final Vector3 touchPoint; // Reusable vector for coordinate conversion
  private Vector3D lastSentDirection = Vector3D.ZERO;

  public TouchInputController(Viewport viewport) {
    this.touchState = new TouchState();
    this.viewport = viewport;
    this.touchPoint = new Vector3();
  }

  @Override
  public void handleInput(LibGDXGameScreen gameScreen) {
    // Check for graphical HUD (takes priority over text-based HUDs)
    HUDManager hudManager = gameScreen.getHudManager();

    if (hudManager != null && hudManager.isOpen()) {
      // Graphical HUD handles touch input via Scene2D
      return; // Don't process game input when graphical HUD is open
    }

    // Track which zones are currently touched this frame
    boolean[] zonesTouched = new boolean[ControlZone.values().length];
    float joystickTouchX = 0;
    float joystickTouchY = 0;

    // Process all active touch pointers (up to 5 simultaneous touches)
    for (int i = 0; i < 5; i++) {
      if (Gdx.input.isTouched(i)) {
        // Convert screen coordinates to virtual coordinates
        touchPoint.set(Gdx.input.getX(i), Gdx.input.getY(i), 0);
        viewport.unproject(touchPoint);

        float x = touchPoint.x;
        float y = touchPoint.y;

        // Find which zone this touch is in
        ControlZone zone = ControlZone.findZone(x, y, false);

        // If this pointer is the joystick pointer, keep it as joystick even if outside the zone
        if (touchState.isJoystickPointer(i)) {
          zone = ControlZone.JOYSTICK;
        }

        if (zone != null) {
          touchState.updatePointer(i, zone);
          zonesTouched[zone.ordinal()] = true;

          // Track joystick touch position for rendering and set joystick pointer
          if (zone == ControlZone.JOYSTICK) {
            joystickTouchX = x;
            joystickTouchY = y;
            // Claim this pointer for the joystick if not already claimed
            if (touchState.getJoystickPointerId() == null) {
              touchState.setJoystickPointer(i);
            }
          }
        } else {
          touchState.updatePointer(i, null);
        }
      } else {
        // Pointer is not active - if this was the joystick pointer, clear it
        if (touchState.isJoystickPointer(i)) {
          touchState.clearJoystick();
        }
        touchState.updatePointer(i, null);
      }
    }

    // Process game input commands
    handleGameInput(zonesTouched, joystickTouchX, joystickTouchY);
  }

  /** Handle game input (movement, actions, quick slots). */
  private void handleGameInput(boolean[] zonesTouched, float joystickTouchX, float joystickTouchY) {
    GameContext gameContext = GameContext.get();
    boolean anyMovementActive = false;

    // Handle joystick input
    if (zonesTouched[ControlZone.JOYSTICK.ordinal()]) {
      Vector2 offset = ControlZone.JOYSTICK.getJoystickOffset(joystickTouchX, joystickTouchY);
      touchState.updateJoystick(joystickTouchX, joystickTouchY, offset);

      if (offset.x != 0 || offset.y != 0) {
        anyMovementActive = true;
        // Isometric: screen-right = northeast, screen-up = northwest
        // game.x = -(offset.x + offset.y), game.y = (offset.x - offset.y)
        Vector3D direction = new Vector3D((int) ((-offset.x - offset.y) * 100), (int) ((offset.x - offset.y) * 100), 0);
        if (!direction.equals(lastSentDirection)) {
          lastSentDirection = direction;
          gameContext.controlManager.processInput(
              new SetMovingDirectionCommand(gameContext.username, direction));
        }
      }
    } else {
      touchState.clearJoystick();
    }

    // Process each zone and send commands for newly pressed zones
    for (ControlZone zone : ControlZone.values()) {
      if (!zone.isGameControl() || zone.isJoystick()) {
        continue; // Skip HUD controls and joystick (handled separately)
      }

      char command = zone.getCommand();
      boolean isTouched = zonesTouched[zone.ordinal()];

      if (isTouched) {
        if (!touchState.isCommandPressed(command)) {
          touchState.pressCommand(command);
          gameContext.controlManager.processInput(new InputCommand(gameContext.username, command));
        }
      } else {
        if (touchState.isCommandPressed(command)) {
          touchState.clearCommand(command);
        }
      }
    }

    // Send halt command if movement stopped (joystick released)
    if (touchState.wasMovementActive() && !anyMovementActive) {
      lastSentDirection = Vector3D.ZERO;
      gameContext.controlManager.processInput(new InputCommand(gameContext.username, "h"));
    }

    // Update movement state for next frame
    touchState.updateMovementState(anyMovementActive);
  }

  /** Get the touch state for rendering purposes. */
  public TouchState getTouchState() {
    return touchState;
  }
}
