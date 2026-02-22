package com.noiprocs.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.control.command.InputCommand;
import com.noiprocs.input.InputController;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import com.noiprocs.ui.libgdx.hud.HUDManager;
import java.util.HashSet;
import java.util.Set;

/** Desktop keyboard input controller for LWJGL3 platform. */
public class DesktopKeyboardInputController implements InputController {
  private final Set<Character> keyPressedSet = new HashSet<>();

  @Override
  public void handleInput(LibGDXGameScreen gameScreen) {
    GameContext gameContext = GameContext.get();
    HUDManager hudManager = gameScreen.getHudManager();

    if (hudManager != null && hudManager.isOpen()) {
      // Graphical HUD handles input via Scene2D
      // ESC key closes the HUD
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        hudManager.close();
      }
      return; // Don't process game input when graphical HUD is open
    }

    // Check for 'E' key to open equipment HUD (graphical version)
    if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
      if (hudManager != null) {
        hudManager.openEquipmentHUD();
        return;
      }
    }

    // Check for 'C' key to open crafting HUD (graphical version)
    if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
      if (hudManager != null) {
        hudManager.openCraftingHUD();
        return;
      }
    }

    // Handle all game input keys
    boolean anyKeyPressed = false;
    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
      processKey(gameContext, 'w');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      processKey(gameContext, 'a');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S)) {
      processKey(gameContext, 's');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D)) {
      processKey(gameContext, 'd');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.F)) {
      processKey(gameContext, 'f');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.H)) {
      processKey(gameContext, 'h');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.T)) {
      processKey(gameContext, 't');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
      processKey(gameContext, '1');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
      processKey(gameContext, '2');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
      processKey(gameContext, '3');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) {
      processKey(gameContext, '4');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
      processKey(gameContext, ' ');
      anyKeyPressed = true;
    }

    // If no keys pressed, send halt command
    if (!anyKeyPressed && !keyPressedSet.isEmpty()) {
      keyPressedSet.clear();
      gameContext.controlManager.processInput(new InputCommand(gameContext.username, "h"));
    }
  }

  private void processKey(GameContext gameContext, char key) {
    if (!keyPressedSet.contains(key)) {
      keyPressedSet.add(key);
      gameContext.controlManager.processInput(new InputCommand(gameContext.username, key));
    }
  }
}
