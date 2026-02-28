package com.noiprocs.resources;

/** Configuration constants for UI dimensions. */
public class UIConfig {
  // Base virtual screen dimensions (desktop default: 440x690)
  public static final float BASE_VIRTUAL_WIDTH = 330;
  public static final float BASE_VIRTUAL_HEIGHT = 690;

  // Square character size in virtual pixels (used for isometric tile rendering)
  public static final float CHAR_SIZE = 16f;

  // Text textures come in two types:
  //   - Isometric: each character maps to one tile, positioned using CHAR_SIZE
  //   - Non-isometric: characters are laid out on a flat grid, stepped by CHAR_WIDTH/CHAR_HEIGHT
  public static final float CHAR_WIDTH = CHAR_SIZE / 2;
  public static final float CHAR_HEIGHT = CHAR_SIZE;
}
