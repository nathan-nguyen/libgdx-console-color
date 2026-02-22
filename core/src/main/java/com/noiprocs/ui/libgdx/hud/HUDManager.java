package com.noiprocs.ui.libgdx.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.control.command.InputCommand;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.resources.GameResource;
import com.noiprocs.resources.ItemTextureManager;
import com.noiprocs.resources.ResourceLoader;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import com.noiprocs.ui.libgdx.hud.panel.CraftingHUD;
import com.noiprocs.ui.libgdx.hud.panel.EquipmentHUD;
import com.noiprocs.ui.libgdx.hud.panel.InventoryHUD;
import com.noiprocs.ui.libgdx.hud.panel.PlayerInfoHUD;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotStyle;
import com.noiprocs.ui.libgdx.util.UIStyleHelper;

/** Central coordinator for HUD system. Manages which HUD is active and handles rendering. */
public class HUDManager {
  private final LibGDXGameScreen gameScreen;
  private final Stage hudStage;
  private final BitmapFont font;
  private final ItemSlotStyle sharedSlotStyle;
  private final ItemTextureManager itemTextureManager;
  private final Skin skin;
  private final PlayerInfoHUD playerInfoHUD;
  private final Table buttonTable;
  private final SettingsManager settingsManager;

  private HUDMode currentMode;
  private Actor currentModalActor;

  // HUD screen instances (lazy initialized)
  private EquipmentHUD equipmentHUD;
  private CraftingHUD craftingHUD;
  private InventoryHUD inventoryHUD;

  public HUDManager(
      LibGDXGameScreen gameScreen,
      Viewport viewport,
      BitmapFont panelFont,
      BitmapFont hudFont,
      ItemTextureManager itemTextureManager,
      SettingsManager settingsManager,
      Runnable onMenuToggle) {
    this.gameScreen = gameScreen;
    this.font = panelFont;
    this.hudStage = new Stage(viewport);
    this.currentMode = HUDMode.NONE;
    this.sharedSlotStyle = ItemSlotStyle.createDefault();
    this.itemTextureManager = itemTextureManager;
    this.settingsManager = settingsManager;
    this.skin = UIStyleHelper.createSkin(hudFont);

    this.playerInfoHUD = new PlayerInfoHUD(hudFont, itemTextureManager);
    this.playerInfoHUD.setOnSlotSelected(
        slotIndex -> {
          GameContext ctx = GameContext.get();
          if (ctx != null) {
            ctx.controlManager.processInput(
                new InputCommand(ctx.username, String.valueOf(slotIndex + 1)));
          }
        });

    this.buttonTable = buildButtonTable(onMenuToggle);

    hudStage.addActor(playerInfoHUD);
    hudStage.addActor(buttonTable);
  }

  private Table buildButtonTable(Runnable onMenuToggle) {
    Texture menuIconTex = ResourceLoader.loadTexture(GameResource.ICON_MENU_BUTTON);
    skin.add("menu-icon", menuIconTex);
    TextureRegionDrawable menuNormal = new TextureRegionDrawable(new TextureRegion(menuIconTex));
    ImageButton.ImageButtonStyle menuStyle = new ImageButton.ImageButtonStyle();
    menuStyle.imageUp = menuNormal;
    menuStyle.imageDown = menuNormal.tint(new Color(1, 1, 1, 0.4f));
    ImageButton menuButton = new ImageButton(menuStyle);
    menuButton.getImage().setScaling(Scaling.fit);
    menuButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            onMenuToggle.run();
          }
        });

    Texture equipIconTex = ResourceLoader.loadTexture(GameResource.ICON_EQUIPMENT_BUTTON);
    skin.add("equipment-icon", equipIconTex);
    TextureRegionDrawable equipNormal = new TextureRegionDrawable(new TextureRegion(equipIconTex));
    ImageButton.ImageButtonStyle equipStyle = new ImageButton.ImageButtonStyle();
    equipStyle.imageUp = equipNormal;
    equipStyle.imageDown = equipNormal.tint(new Color(1, 1, 1, 0.4f));
    ImageButton equipmentButton = new ImageButton(equipStyle);
    equipmentButton.getImage().setScaling(Scaling.fit);
    equipmentButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            openEquipmentHUD();
          }
        });

    Table table = new Table();
    table.setFillParent(true);
    table.top().right().pad(10);
    table.add(equipmentButton).size(50, 50).padRight(10);
    table.add(menuButton).size(50, 50);
    return table;
  }

  /** Sets visibility of the persistent control buttons. */
  public void setButtonsVisible(boolean visible) {
    buttonTable.setVisible(visible);
  }

  /** Opens the equipment management HUD. */
  public void openEquipmentHUD() {
    closeModal();

    if (equipmentHUD == null) {
      equipmentHUD =
          new EquipmentHUD(
              gameScreen, hudStage.getViewport(), font, sharedSlotStyle, itemTextureManager);
    }

    equipmentHUD.refresh();
    currentModalActor = equipmentHUD.getRoot();
    hudStage.addActor(currentModalActor);
    currentMode = HUDMode.EQUIPMENT;
  }

  /** Opens the crafting HUD. */
  public void openCraftingHUD() {
    closeModal();

    if (craftingHUD == null) {
      craftingHUD =
          new CraftingHUD(
              gameScreen, hudStage.getViewport(), font, sharedSlotStyle, itemTextureManager);
    }

    craftingHUD.refresh();
    currentModalActor = craftingHUD.getRoot();
    hudStage.addActor(currentModalActor);
    currentMode = HUDMode.CRAFTING;
  }

  /**
   * Opens the inventory interaction HUD for a container (e.g., chest).
   *
   * @param containerModelId ID of the container model
   */
  public void openInventoryHUD(String containerModelId) {
    closeModal();

    if (inventoryHUD == null) {
      inventoryHUD =
          new InventoryHUD(
              gameScreen, hudStage.getViewport(), font, sharedSlotStyle, itemTextureManager);
    }

    inventoryHUD.setContainer(containerModelId);
    inventoryHUD.refresh();
    currentModalActor = inventoryHUD.getRoot();
    hudStage.addActor(currentModalActor);
    currentMode = HUDMode.INVENTORY_INTERACTION;
  }

  private void closeModal() {
    if (currentModalActor != null) {
      currentModalActor.remove();
      currentModalActor = null;
    }
    currentMode = HUDMode.NONE;
  }

  /** Closes the currently active modal HUD. */
  public void close() {
    closeModal();
  }

  /**
   * Renders the HUD. Always renders persistent UI; modal HUD renders when one is open.
   *
   * @param delta Time since last frame
   */
  public void render(float delta) {
    GameContext ctx = GameContext.get();
    if (ctx != null) {
      Model playerModel = ctx.modelManager.getModel(ctx.username);
      if (playerModel instanceof PlayerModel) {
        playerInfoHUD.update((PlayerModel) playerModel, settingsManager);
      }
    }

    hudStage.act(delta);
    hudStage.draw();
  }

  /**
   * Checks if any modal HUD is currently open.
   *
   * @return true if a modal HUD is open
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

    if (sharedSlotStyle != null) {
      sharedSlotStyle.dispose();
    }
    if (skin != null) {
      skin.dispose();
    }
    playerInfoHUD.dispose();
  }
}
