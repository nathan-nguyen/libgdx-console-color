package com.noiprocs.ui.libgdx.hud;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.resources.ItemTextureManager;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import com.noiprocs.ui.libgdx.hud.panel.CraftingHUD;
import com.noiprocs.ui.libgdx.hud.panel.EquipmentHUD;
import com.noiprocs.ui.libgdx.hud.panel.InventoryHUD;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotStyle;

/** Central coordinator for HUD system. Manages which HUD is active and handles rendering. */
public class HUDManager {

  private final GameContext gameContext;
  private final LibGDXGameScreen gameScreen;
  private final Stage hudStage;
  private final BitmapFont font;
  private final ItemSlotStyle sharedSlotStyle;
  private final ItemTextureManager itemTextureManager;

  private HUDMode currentMode;

  // HUD screen instances (lazy initialized)
  private EquipmentHUD equipmentHUD;
  private CraftingHUD craftingHUD;
  private InventoryHUD inventoryHUD;

  /**
   * Creates a new HUDManager.
   *
   * @param gameContext Game context for accessing game state
   * @param gameScreen Game screen for accessing models
   * @param viewport Viewport for the HUD stage
   * @param font Font for rendering text
   */
  public HUDManager(
      GameContext gameContext,
      LibGDXGameScreen gameScreen,
      Viewport viewport,
      BitmapFont font,
      ItemTextureManager itemTextureManager) {
    this.gameContext = gameContext;
    this.gameScreen = gameScreen;
    this.font = font;
    this.hudStage = new Stage(viewport);
    this.currentMode = HUDMode.NONE;
    this.sharedSlotStyle = ItemSlotStyle.createDefault();
    this.itemTextureManager = itemTextureManager;
  }

  /** Opens the equipment management HUD. */
  public void openEquipmentHUD() {
    close(); // Close any existing HUD

    // Lazy initialization
    if (equipmentHUD == null) {
      equipmentHUD =
          new EquipmentHUD(
              gameContext,
              gameScreen,
              hudStage.getViewport(),
              font,
              sharedSlotStyle,
              itemTextureManager);
    }

    equipmentHUD.refresh();
    hudStage.clear();
    hudStage.addActor(equipmentHUD.getRoot());
    currentMode = HUDMode.EQUIPMENT;
  }

  /** Opens the crafting HUD. */
  public void openCraftingHUD() {
    close(); // Close any existing HUD

    // Lazy initialization
    if (craftingHUD == null) {
      craftingHUD =
          new CraftingHUD(
              gameContext,
              gameScreen,
              hudStage.getViewport(),
              font,
              sharedSlotStyle,
              itemTextureManager);
    }

    craftingHUD.refresh();
    hudStage.clear();
    hudStage.addActor(craftingHUD.getRoot());
    currentMode = HUDMode.CRAFTING;
  }

  /**
   * Opens the inventory interaction HUD for a container (e.g., chest).
   *
   * @param containerModelId ID of the container model
   */
  public void openInventoryHUD(String containerModelId) {
    close(); // Close any existing HUD

    // Lazy initialization
    if (inventoryHUD == null) {
      inventoryHUD =
          new InventoryHUD(
              gameContext,
              gameScreen,
              hudStage.getViewport(),
              font,
              sharedSlotStyle,
              itemTextureManager);
    }

    inventoryHUD.setContainer(containerModelId);
    inventoryHUD.refresh();
    hudStage.clear();
    hudStage.addActor(inventoryHUD.getRoot());
    currentMode = HUDMode.INVENTORY_INTERACTION;
  }

  /** Closes the currently active HUD. */
  public void close() {
    if (currentMode != HUDMode.NONE) {
      hudStage.clear();
      currentMode = HUDMode.NONE;
    }
  }

  /**
   * Renders the active HUD.
   *
   * @param delta Time since last frame
   */
  public void render(float delta) {
    if (currentMode != HUDMode.NONE) {
      hudStage.act(delta);
      hudStage.draw();
    }
  }

  /**
   * Checks if any HUD is currently open.
   *
   * @return true if a HUD is open
   */
  public boolean isOpen() {
    return currentMode != HUDMode.NONE;
  }

  /**
   * Gets the HUD stage for input processing.
   *
   * @return Stage used for HUD rendering
   */
  public Stage getHudStage() {
    return hudStage;
  }

  /** Disposes of resources used by the HUD manager. */
  public void dispose() {
    hudStage.dispose();

    if (equipmentHUD != null) {
      equipmentHUD.dispose();
    }
    if (craftingHUD != null) {
      craftingHUD.dispose();
    }
    if (inventoryHUD != null) {
      inventoryHUD.dispose();
    }

    // Dispose shared slot style
    if (sharedSlotStyle != null) {
      sharedSlotStyle.dispose();
    }
  }
}
