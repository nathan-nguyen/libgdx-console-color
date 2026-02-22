package com.noiprocs.resources;

/** Enumeration of all file-based game resources (textures). */
public enum GameResource {
  FONT_DEJA_VU_SANS_MONO("DejaVuSansMono.ttf"),
  BACKGROUND_MAIN_MENU("main_menu_background.png"),
  ICON_MENU_BUTTON("icons/menu_button.png"),
  ICON_EQUIPMENT_BUTTON("icons/equipment_button.png"),
  ICON_ATTACK_BUTTON("icons/attack_button.png"),
  ICON_INTERACT_BUTTON("icons/interact_button.png"),
  ICON_USE_ITEM_BUTTON("icons/use_item_button.png");

  private final String path;

  GameResource(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}
