package com.noiprocs.input;

import com.noiprocs.core.GameContext;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;

/**
 * Platform-agnostic input controller interface.
 * Different platforms (desktop, mobile, web) implement this to handle
 * platform-specific input and translate it to game commands.
 */
public interface InputController {
  /**
   * Handle input for the current frame.
   *
   * @param gameContext The game context for sending commands
   * @param gameScreen The game screen for HUD state
   */
  void handleInput(GameContext gameContext, LibGDXGameScreen gameScreen);
}
