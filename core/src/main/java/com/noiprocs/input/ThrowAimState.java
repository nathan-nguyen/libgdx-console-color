package com.noiprocs.input;

import com.noiprocs.core.common.Vector3D;

/** Shared state for the throw aim joystick direction, read by PlayerSprite to drive the preview. */
public class ThrowAimState {
  private static Vector3D aimDirection = null;

  public static void setAimDirection(Vector3D direction) {
    aimDirection = direction;
  }

  public static void clearAimDirection() {
    aimDirection = null;
  }

  /** Returns the current throw aim direction, or null if no aim is active. */
  public static Vector3D getAimDirection() {
    return aimDirection;
  }
}
