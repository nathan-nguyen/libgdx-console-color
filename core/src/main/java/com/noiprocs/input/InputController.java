package com.noiprocs.input;

import com.noiprocs.ui.libgdx.hud.HUDManager;

/**
 * Platform-agnostic input controller interface. Different platforms (desktop, mobile, web)
 * implement this to handle platform-specific input and translate it to game commands.
 */
public interface InputController {
  /**
   * Handle input for the current frame.
   *
   * @param hudManager The HUD manager for checking and controlling HUD state
   */
  void handleInput(HUDManager hudManager);
}
