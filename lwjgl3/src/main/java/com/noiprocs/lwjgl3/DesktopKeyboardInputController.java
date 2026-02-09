package com.noiprocs.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.control.command.InputCommand;
import com.noiprocs.input.InputController;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import java.util.HashSet;
import java.util.Set;

/** Desktop keyboard input controller for LWJGL3 platform. */
public class DesktopKeyboardInputController implements InputController {
  private final Set<Character> keyPressedSet = new HashSet<>();

  @Override
  public void handleInput(GameContext gameContext, LibGDXGameScreen gameScreen) {
    // Handle crafting HUD
    if (gameScreen.hud.craftingHud.isOpen()) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        gameScreen.hud.craftingHud.close();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
        gameScreen.hud.craftingHud.close();
        gameScreen.hud.equipmentHud.open();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        gameScreen.hud.craftingHud.craftSelectedItem();
        return;
      }
      // Arrow key navigation
      if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        gameScreen.hud.craftingHud.handleNavigation('w');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        gameScreen.hud.craftingHud.handleNavigation('s');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
        gameScreen.hud.craftingHud.handleNavigation('a');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
        gameScreen.hud.craftingHud.handleNavigation('d');
        return;
      }
      return;
    }

    // Handle equipment HUD
    if (gameScreen.hud.equipmentHud.isOpen()) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        gameScreen.hud.equipmentHud.close();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
        gameScreen.hud.equipmentHud.close();
        gameScreen.hud.craftingHud.open();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        gameScreen.hud.equipmentHud.handleEquipmentAction();
        return;
      }
      // Arrow key navigation
      if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        gameScreen.hud.equipmentHud.handleNavigation('w');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        gameScreen.hud.equipmentHud.handleNavigation('s');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
        gameScreen.hud.equipmentHud.handleNavigation('a');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
        gameScreen.hud.equipmentHud.handleNavigation('d');
        return;
      }
      // Handle number keys 1-4 for inventory swapping
      if (!gameScreen.hud.equipmentHud.isEquipmentSelected()) {
        for (int i = 0; i < 4; i++) {
          if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
            int currentSlot = gameScreen.hud.equipmentHud.getSelectedSlot();
            if (currentSlot != i) {
              gameContext.controlManager.swapInventorySlots(currentSlot, i);
            }
            return;
          }
        }
      }
      return;
    }

    // Handle chest interaction HUD
    if (gameScreen.hud.inventoryInteractionHud.isChestOpen()) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        gameScreen.hud.inventoryInteractionHud.close();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        gameScreen.hud.inventoryInteractionHud.transferSelectedItem();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
        gameScreen.hud.inventoryInteractionHud.handleEquipmentAction();
        return;
      }
      // Arrow key navigation
      if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        gameScreen.hud.inventoryInteractionHud.handleNavigation('w');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        gameScreen.hud.inventoryInteractionHud.handleNavigation('s');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
        gameScreen.hud.inventoryInteractionHud.handleNavigation('a');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
        gameScreen.hud.inventoryInteractionHud.handleNavigation('d');
        return;
      }
      return;
    }

    // Check for 'e' key to open equipment HUD
    if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
      gameScreen.hud.equipmentHud.open();
      return;
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
