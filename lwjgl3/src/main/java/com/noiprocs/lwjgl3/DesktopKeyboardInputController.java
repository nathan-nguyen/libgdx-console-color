package com.noiprocs.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Direction;
import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.control.command.InputCommand;
import com.noiprocs.core.control.command.SetMovingDirectionCommand;
import com.noiprocs.input.InputController;
import com.noiprocs.ui.libgdx.hud.HUDManager;
import java.util.HashSet;
import java.util.Set;

/** Desktop keyboard input controller for LWJGL3 platform. */
public class DesktopKeyboardInputController implements InputController {
  private final Set<Character> keyPressedSet = new HashSet<>();
  private Vector3D lastMovingDirection = Vector3D.ZERO;

  @Override
  public void handleInput(HUDManager hudManager) {
    GameContext gameContext = GameContext.get();

    if (hudManager != null && hudManager.isOpen()) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        hudManager.close();
      }
      return;
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
      if (hudManager != null) {
        hudManager.openEquipmentHUD();
        return;
      }
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
      if (hudManager != null) {
        hudManager.openCraftingHUD();
        return;
      }
    }

    // Compute 8-directional movement from currently held WASD keys
    Vector3D movingDirection = Vector3D.ZERO;
    if (Gdx.input.isKeyPressed(Input.Keys.W))
      movingDirection = movingDirection.add(Direction.NORTH);
    if (Gdx.input.isKeyPressed(Input.Keys.A)) movingDirection = movingDirection.add(Direction.WEST);
    if (Gdx.input.isKeyPressed(Input.Keys.S))
      movingDirection = movingDirection.add(Direction.SOUTH);
    if (Gdx.input.isKeyPressed(Input.Keys.D)) movingDirection = movingDirection.add(Direction.EAST);
    if (!movingDirection.equals(lastMovingDirection)) {
      lastMovingDirection = movingDirection;
      gameContext.controlManager.processInput(
          new SetMovingDirectionCommand(gameContext.username, movingDirection));
    }

    // Handle action keys (fire once per press)
    boolean anyActionKeyPressed = false;
    if (Gdx.input.isKeyPressed(Input.Keys.F)) {
      processKey(gameContext, 'f');
      anyActionKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.H)) {
      processKey(gameContext, 'h');
      anyActionKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.T)) {
      processKey(gameContext, 't');
      anyActionKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.NUM_1)) {
      processKey(gameContext, '1');
      anyActionKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.NUM_2)) {
      processKey(gameContext, '2');
      anyActionKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.NUM_3)) {
      processKey(gameContext, '3');
      anyActionKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.NUM_4)) {
      processKey(gameContext, '4');
      anyActionKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
      processKey(gameContext, ' ');
      anyActionKeyPressed = true;
    }

    if (!anyActionKeyPressed && !keyPressedSet.isEmpty()) {
      keyPressedSet.clear();
    }
  }

  private void processKey(GameContext gameContext, char key) {
    if (!keyPressedSet.contains(key)) {
      keyPressedSet.add(key);
      gameContext.controlManager.processInput(new InputCommand(gameContext.username, key));
    }
  }
}
