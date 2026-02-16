package com.noiprocs.resources;

/** Configuration constants for UI dimensions. */
public class UIConfig {
  // Base virtual screen dimensions (desktop default: 440x690)
  public static final float BASE_VIRTUAL_WIDTH = 440f;
  public static final float BASE_VIRTUAL_HEIGHT = 690f;

  // Fixed character dimensions based on base virtual screen
  // Total lines: 44 (2 player info + 40 map + 2 border), window height: 690
  public static final float CHAR_HEIGHT = BASE_VIRTUAL_HEIGHT / 44;
  // Total columns: 62 (2 border + 60 map), window width: 440
  public static final float CHAR_WIDTH = BASE_VIRTUAL_WIDTH / 62;
}
