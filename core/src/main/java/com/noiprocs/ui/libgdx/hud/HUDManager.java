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
import com.noiprocs.core.model.InventoryContainerInterface;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.action.Action;
import com.noiprocs.core.model.action.InteractAction;
import com.noiprocs.core.model.mob.character.HumanoidModel;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.gameplay.model.mob.BlackSmithModel;
import com.noiprocs.gameplay.model.mob.MerchantModel;
import com.noiprocs.resources.GameResource;
import com.noiprocs.resources.RenderResources;
import com.noiprocs.resources.ResourceLoader;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.libgdx.hud.panel.BlackSmithHUD;
import com.noiprocs.ui.libgdx.hud.panel.CraftingHUD;
import com.noiprocs.ui.libgdx.hud.panel.EquipmentHUD;
import com.noiprocs.ui.libgdx.hud.panel.InventoryHUD;
import com.noiprocs.ui.libgdx.hud.panel.MerchantHUD;
import com.noiprocs.ui.libgdx.hud.panel.PlayerInfoHUD;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotStyle;
import com.noiprocs.ui.libgdx.util.UIStyleHelper;

/** Central coordinator for HUD system. Manages which HUD is active and handles rendering. */
public class HUDManager {
  private final Stage hudStage;
  private final ItemSlotStyle sharedSlotStyle;
  private final Skin skin;
  private final PlayerInfoHUD playerInfoHUD;
  private final Table buttonTable;

  private HUDMode currentMode;
  private Actor currentModalActor;

  // HUD screen instances (lazy initialized)
  private EquipmentHUD equipmentHUD;
  private CraftingHUD craftingHUD;
  private InventoryHUD inventoryHUD;
  private MerchantHUD merchantHUD;
  private BlackSmithHUD blackSmithHUD;

  public HUDManager(Viewport viewport, SettingsManager settingsManager, Runnable onMenuToggle) {
    RenderResources renderResources = RenderResources.get();
    BitmapFont hudFont = renderResources.getHudFont();

    this.hudStage = new Stage(viewport);
    this.currentMode = HUDMode.NONE;
    this.sharedSlotStyle = ItemSlotStyle.createDefault();
    this.skin = UIStyleHelper.createSkin(renderResources.getHudFont());

    this.playerInfoHUD = new PlayerInfoHUD(settingsManager);
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
      equipmentHUD = new EquipmentHUD(this, hudStage.getViewport(), sharedSlotStyle);
    }

    equipmentHUD.refresh();
    currentModalActor = equipmentHUD.getRoot();
    hudStage.addActor(currentModalActor);
    currentMode = HUDMode.EQUIPMENT;
  }

  /** Opens the crafting HUD. */
  public void openCraftingHUD() {
    RenderResources renderResources = RenderResources.get();
    closeModal();

    if (craftingHUD == null) {
      craftingHUD = new CraftingHUD(this, hudStage.getViewport(), sharedSlotStyle);
    }

    craftingHUD.refresh();
    currentModalActor = craftingHUD.getRoot();
    hudStage.addActor(currentModalActor);
    currentMode = HUDMode.CRAFTING;
  }

  /** Opens the merchant shop HUD. */
  public void openMerchantHUD(String merchantModelId) {
    closeModal();

    if (merchantHUD == null) {
      merchantHUD = new MerchantHUD(this, hudStage.getViewport(), sharedSlotStyle);
    }

    merchantHUD.setMerchant(merchantModelId);
    merchantHUD.refresh();
    currentModalActor = merchantHUD.getRoot();
    hudStage.addActor(currentModalActor);
    currentMode = HUDMode.MERCHANT;
  }

  /** Opens the blacksmith upgrade HUD. */
  public void openBlackSmithHUD(String blacksmithModelId) {
    closeModal();

    if (blackSmithHUD == null) {
      blackSmithHUD = new BlackSmithHUD(this, hudStage.getViewport(), sharedSlotStyle);
    }

    blackSmithHUD.setBlacksmith(blacksmithModelId);
    blackSmithHUD.refresh();
    currentModalActor = blackSmithHUD.getRoot();
    hudStage.addActor(currentModalActor);
    currentMode = HUDMode.BLACKSMITH;
  }

  /**
   * Opens the inventory interaction HUD for a container (e.g., chest).
   *
   * @param containerModelId ID of the container model
   */
  public void openInventoryHUD(String containerModelId) {
    closeModal();

    if (inventoryHUD == null) {
      inventoryHUD = new InventoryHUD(this, hudStage.getViewport(), sharedSlotStyle);
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
        PlayerModel player = (PlayerModel) playerModel;
        playerInfoHUD.update(player);
        syncInventoryHUD(ctx, player);
      }
    }

    hudStage.act(delta);
    hudStage.draw();
  }

  private void syncInventoryHUD(GameContext ctx, PlayerModel playerModel) {
    Action playerAction = playerModel.getAction();
    if (playerAction instanceof InteractAction) {
      InteractAction interactAction = (InteractAction) playerAction;
      Model model = ctx.modelManager.getModel(interactAction.targetId);
      if (model instanceof BlackSmithModel) {
        if (!isOpen()) {
          openBlackSmithHUD(interactAction.targetId);
        }
      } else if (model instanceof MerchantModel) {
        if (!isOpen()) {
          openMerchantHUD(interactAction.targetId);
        }
      } else if (model instanceof InventoryContainerInterface || isHumanoidButNotPlayer(model)) {
        if (!isOpen()) {
          openInventoryHUD(interactAction.targetId);
        }
      }
    }
  }

  private static boolean isHumanoidButNotPlayer(Model model) {
    return model instanceof HumanoidModel && !(model instanceof PlayerModel);
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
    if (merchantHUD != null) {
      merchantHUD.dispose();
    }
    if (blackSmithHUD != null) {
      blackSmithHUD.dispose();
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
