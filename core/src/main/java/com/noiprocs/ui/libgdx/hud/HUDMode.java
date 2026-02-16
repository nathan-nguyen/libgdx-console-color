package com.noiprocs.ui.libgdx.hud;

/** Represents the active HUD mode. */
public enum HUDMode {
  /** No HUD is currently open */
  NONE,

  /** Equipment management HUD is open */
  EQUIPMENT,

  /** Crafting HUD is open */
  CRAFTING,

  /** Inventory interaction HUD (chest/container) is open */
  INVENTORY_INTERACTION
}
