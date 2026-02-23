package com.noiprocs.resources;

/** Configuration constants for UI dimensions. */
public class UIConfig {
  // Base virtual screen dimensions (desktop default: 440x690)
  public static final float BASE_VIRTUAL_WIDTH = 330;
  public static final float BASE_VIRTUAL_HEIGHT = 690;

  // Square character size in virtual pixels (used for isometric tile rendering)
  public static final float CHAR_SIZE = 16f;

  // Non-isometric character dimensions matching the original console grid
  public static final float CHAR_WIDTH = 440f / 62;
  public static final float CHAR_HEIGHT = 690f / 44;
}
