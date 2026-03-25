package com.noiprocs.ui.libgdx.hud.panel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.model.item.Inventory;
import com.noiprocs.core.model.item.Item;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.gameplay.control.command.UpgradeItemCommand;
import com.noiprocs.gameplay.world.BlackSmithConfigLoader;
import com.noiprocs.gameplay.world.BlackSmithConfigLoader.UpgradeRecipe;
import com.noiprocs.gameplay.world.BlackSmithConfigLoader.UpgradeRecipe.MaterialEntry;
import com.noiprocs.resources.ItemTextureManager;
import com.noiprocs.ui.libgdx.hud.HUDManager;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotStyle;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotWidget;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Blacksmith upgrade HUD. Left panel shows the player's inventory; right panel lists recipes. */
public class BlackSmithHUD {
  private static final Logger logger = LoggerFactory.getLogger(BlackSmithHUD.class);

  private static final int PLAYER_INVENTORY_SIZE = 9;

  private final HUDManager hudManager;
  private final BitmapFont font;
  private final Table rootTable;
  private final ItemSlotStyle slotStyle;
  private final Viewport viewport;
  private final ItemTextureManager itemTextureManager;
  private final Table mainContainer;

  private String blacksmithModelId;

  private Table confirmDialog;

  private ItemSlotWidget[] playerInventorySlots;
  private Label[] playerInventoryNameLabels;

  private Label goldLabel;

  private Texture backgroundTexture;
  private Texture panelTexture;

  public BlackSmithHUD(
      HUDManager hudManager,
      Viewport viewport,
      BitmapFont font,
      ItemSlotStyle slotStyle,
      ItemTextureManager itemTextureManager) {
    this.hudManager = hudManager;
    this.viewport = viewport;
    this.font = font;
    this.slotStyle = slotStyle;
    this.itemTextureManager = itemTextureManager;
    this.mainContainer = new Table();
    this.rootTable = new Table();
    this.rootTable.setFillParent(true);
    buildUI();
  }

  // ── Build ─────────────────────────────────────────────────────────────────

  private void buildUI() {
    rootTable.setTouchable(Touchable.enabled);
    rootTable.center();
    mainContainer.setBackground(createBackground());
    mainContainer.setTouchable(Touchable.enabled);
    rootTable.add(mainContainer);
    rootTable.addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();
            if (target != mainContainer && !isDescendantOf(target, mainContainer)) {
              close();
              event.stop();
              return true;
            }
            return false;
          }
        });
  }

  private void rebuildUI() {
    mainContainer.clear();

    Table header = buildHeader();
    mainContainer.add(header).expandX().fillX().pad(5);
    mainContainer.row();

    Label.LabelStyle labelStyle = makeLabelStyle(Color.YELLOW);
    goldLabel = new Label("", labelStyle);
    goldLabel.setFontScale(0.85f);
    mainContainer.add(goldLabel).padBottom(4);
    mainContainer.row();

    Table content = new Table();

    Table playerPanel = buildPlayerInventoryPanel();
    playerPanel.setBackground(getPanelDrawable());
    playerPanel.pad(8, 0, 8, 0);
    content.add(playerPanel).pad(8).top();

    Table recipesPanel = buildRecipesPanel();
    recipesPanel.setBackground(getPanelDrawable());
    recipesPanel.pad(8, 0, 8, 0);
    content.add(recipesPanel).pad(8).top().expandY().fillY();

    mainContainer.add(content).expand().fill().pad(5);

    float maxWidth = Math.min(viewport.getWorldWidth() * 0.9f, 680);
    float maxHeight = Math.min(viewport.getWorldHeight() * 0.9f, 560);
    rootTable.getCell(mainContainer).width(maxWidth).height(maxHeight);
  }

  private Table buildHeader() {
    Table header = new Table();
    Label title = new Label("BLACKSMITH", makeLabelStyle(Color.WHITE));
    title.setFontScale(1.2f);
    header.add(title).expandX().center();
    return header;
  }

  private Table buildPlayerInventoryPanel() {
    Table panel = new Table();
    panel.top();

    Label heading = new Label("Your Inventory", makeLabelStyle(Color.WHITE));
    heading.setFontScale(0.9f);
    panel.add(heading).colspan(3).padBottom(5);
    panel.row();

    playerInventorySlots = new ItemSlotWidget[PLAYER_INVENTORY_SIZE];
    playerInventoryNameLabels = new Label[PLAYER_INVENTORY_SIZE];

    for (int i = 0; i < PLAYER_INVENTORY_SIZE; i++) {
      ItemSlotWidget slot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
      playerInventorySlots[i] = slot;

      Label nameLabel = new Label("", makeLabelStyle(Color.WHITE));
      nameLabel.setFontScale(0.6f);
      nameLabel.setAlignment(Align.center);
      nameLabel.setWrap(true);
      playerInventoryNameLabels[i] = nameLabel;

      Table entry = new Table();
      entry.add(slot).size(48, 48);
      entry.row();
      entry.add(nameLabel).width(48).height(28).padTop(2);
      panel.add(entry).pad(1);

      if ((i + 1) % 3 == 0) panel.row();
    }

    return panel;
  }

  private Table buildRecipesPanel() {
    Table panel = new Table();
    panel.top();

    Label heading = new Label("Upgrades", makeLabelStyle(Color.WHITE));
    heading.setFontScale(0.9f);
    panel.add(heading).padBottom(5);
    panel.row();

    Table colHeader = new Table();
    colHeader.add(new Label("", makeLabelStyle(Color.LIGHT_GRAY))).width(48).padRight(4);
    Label arrowHeader = new Label("", makeLabelStyle(Color.LIGHT_GRAY));
    colHeader.add(arrowHeader).width(20).padRight(4);
    colHeader.add(new Label("", makeLabelStyle(Color.LIGHT_GRAY))).width(48).padRight(4);
    Label descHeader = new Label("Recipe / Cost", makeLabelStyle(Color.LIGHT_GRAY));
    descHeader.setFontScale(0.75f);
    colHeader.add(descHeader).width(140);
    panel.add(colHeader).padBottom(3);
    panel.row();

    Table recipeListTable = new Table();
    recipeListTable.top().left();

    List<UpgradeRecipe> recipes = BlackSmithConfigLoader.get().getRecipes();
    for (UpgradeRecipe recipe : recipes) {
      Table row = buildRecipeRow(recipe);
      recipeListTable.add(row).expandX().fillX().padBottom(4);
      recipeListTable.row();
    }

    ScrollPane scrollPane = new ScrollPane(recipeListTable);
    scrollPane.setScrollingDisabled(true, false);
    scrollPane.setFadeScrollBars(false);
    scrollPane.setOverscroll(false, false);

    panel.add(scrollPane).expand().fill();

    return panel;
  }

  private Table buildRecipeRow(UpgradeRecipe recipe) {
    Table row = new Table();
    row.left();

    ItemSlotWidget inputSlot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
    try {
      Class<?> inputCls = Class.forName(recipe.inputItemClass);
      inputSlot.setItem(inputCls, simpleItemName(recipe.inputItemClass), 1);
    } catch (ClassNotFoundException e) {
      logger.warn("Unknown input item class: {}", recipe.inputItemClass);
    }
    row.add(inputSlot).size(48, 48).padRight(4);

    Label arrow = new Label("->", makeLabelStyle(Color.LIGHT_GRAY));
    arrow.setFontScale(0.85f);
    row.add(arrow).width(20).padRight(4);

    ItemSlotWidget outputSlot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
    try {
      Class<?> outputCls = Class.forName(recipe.outputItemClass);
      String outputName = simpleItemName(recipe.outputItemClass);
      if (recipe.variantName != null) outputName += " (" + recipe.variantName + ")";
      outputSlot.setItem(outputCls, outputName, recipe.outputAmount);
    } catch (ClassNotFoundException e) {
      logger.warn("Unknown output item class: {}", recipe.outputItemClass);
    }
    row.add(outputSlot).size(48, 48).padRight(8);

    Table descColumn = new Table();
    descColumn.left();

    String outputLabel = simpleItemName(recipe.outputItemClass);
    if (recipe.variantName != null) outputLabel += " (" + recipe.variantName + ")";
    Label nameLabel = new Label(outputLabel, makeLabelStyle(Color.WHITE));
    nameLabel.setFontScale(0.75f);
    descColumn.add(nameLabel).left();
    descColumn.row();

    for (MaterialEntry mat : recipe.materials) {
      Label matLabel =
          new Label(
              mat.amount + "x " + simpleItemName(mat.itemClass),
              makeLabelStyle(new Color(0.8f, 0.8f, 0.8f, 1f)));
      matLabel.setFontScale(0.65f);
      descColumn.add(matLabel).left();
      descColumn.row();
    }

    Label goldLabel =
        new Label(recipe.goldCost + "g", makeLabelStyle(new Color(1f, 0.85f, 0.2f, 1f)));
    goldLabel.setFontScale(0.65f);
    descColumn.add(goldLabel).left();

    row.add(descColumn).width(140).top().left();

    row.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            showUpgradeDialog(recipe);
          }
        });

    return row;
  }

  // ── Dialog ────────────────────────────────────────────────────────────────

  private void showUpgradeDialog(UpgradeRecipe recipe) {
    if (confirmDialog != null) confirmDialog.remove();

    String outputLabel = simpleItemName(recipe.outputItemClass);
    if (recipe.variantName != null) outputLabel += " (" + recipe.variantName + ")";

    Table overlay = new Table();
    overlay.setFillParent(true);
    Pixmap dimPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    dimPixmap.setColor(0f, 0f, 0f, 0.55f);
    dimPixmap.fill();
    Texture dimTexture = new Texture(dimPixmap);
    dimPixmap.dispose();
    overlay.setBackground(new TextureRegionDrawable(dimTexture));
    overlay.setTouchable(Touchable.enabled);

    Table dialog = new Table();
    Pixmap boxPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    boxPixmap.setColor(0.15f, 0.15f, 0.15f, 0.97f);
    boxPixmap.fill();
    Texture boxTexture = new Texture(boxPixmap);
    boxPixmap.dispose();
    dialog.setBackground(new TextureRegionDrawable(boxTexture));
    dialog.pad(32);

    Label titleLabel = new Label("Upgrade: " + outputLabel, makeLabelStyle(Color.WHITE));
    titleLabel.setFontScale(1.0f);
    dialog.add(titleLabel).colspan(2).center().padBottom(12);
    dialog.row();

    // Cost summary
    Table costTable = new Table();
    costTable.left();
    Label inputLine =
        new Label(
            "1x " + simpleItemName(recipe.inputItemClass),
            makeLabelStyle(new Color(1f, 0.5f, 0.5f, 1f)));
    inputLine.setFontScale(0.85f);
    costTable.add(inputLine).left();
    costTable.row();
    for (MaterialEntry mat : recipe.materials) {
      Label matLine =
          new Label(
              mat.amount + "x " + simpleItemName(mat.itemClass),
              makeLabelStyle(new Color(1f, 0.5f, 0.5f, 1f)));
      matLine.setFontScale(0.85f);
      costTable.add(matLine).left();
      costTable.row();
    }
    Label goldLine =
        new Label(recipe.goldCost + "g", makeLabelStyle(new Color(1f, 0.85f, 0.2f, 1f)));
    goldLine.setFontScale(0.85f);
    costTable.add(goldLine).left();
    dialog.add(costTable).colspan(2).center().padBottom(24);
    dialog.row();

    Label cancelBtn = new Label("  Cancel  ", makeLabelStyle(Color.LIGHT_GRAY));
    cancelBtn.setFontScale(0.85f);
    cancelBtn.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            overlay.remove();
            confirmDialog = null;
          }
        });

    Label confirmBtn = new Label("  Upgrade  ", makeLabelStyle(new Color(0.4f, 1f, 0.4f, 1f)));
    confirmBtn.setFontScale(0.85f);
    confirmBtn.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            executeUpgrade(recipe.id);
            overlay.remove();
            confirmDialog = null;
          }
        });

    dialog.add(cancelBtn).padRight(16);
    dialog.add(confirmBtn);

    overlay.add(dialog);
    overlay.addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();
            if (target != dialog && !isDescendantOf(target, dialog)) {
              overlay.remove();
              confirmDialog = null;
              event.stop();
              return true;
            }
            return false;
          }
        });

    confirmDialog = overlay;
    rootTable.getStage().addActor(overlay);
  }

  // ── Commands ─────────────────────────────────────────────────────────────

  private void executeUpgrade(String recipeId) {
    GameContext ctx = GameContext.get();
    ctx.controlManager.processInput(
        new UpgradeItemCommand(ctx.username, blacksmithModelId, recipeId));
    scheduleRefresh();
  }

  // ── Refresh ──────────────────────────────────────────────────────────────

  public void refresh() {
    GameContext ctx = GameContext.get();
    PlayerModel player = (PlayerModel) ctx.modelManager.getModel(ctx.username);
    if (player == null) return;

    int gold = countGold(player);
    if (goldLabel != null) goldLabel.setText("Gold: " + gold);

    Inventory inv = player.getInventory();
    for (int i = 0; i < PLAYER_INVENTORY_SIZE; i++) {
      Item item = inv.getItem(i);
      if (item != null) {
        playerInventorySlots[i].setItem(item, item.amount);
        playerInventoryNameLabels[i].setText(simpleItemName(item.getClass().getName()));
      } else {
        playerInventorySlots[i].clear();
        playerInventoryNameLabels[i].setText("");
      }
    }
  }

  // ── Public API ───────────────────────────────────────────────────────────

  public void setBlacksmith(String blacksmithModelId) {
    this.blacksmithModelId = blacksmithModelId;
    rebuildUI();
  }

  public Actor getRoot() {
    return rootTable;
  }

  public void dispose() {
    if (backgroundTexture != null) backgroundTexture.dispose();
    if (panelTexture != null) panelTexture.dispose();
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private int countGold(PlayerModel player) {
    try {
      Class<?> goldClass = Class.forName("com.noiprocs.gameplay.model.item.GoldCoinItem");
      return player.getInventory().countItems(goldClass);
    } catch (ClassNotFoundException e) {
      return 0;
    }
  }

  private static String simpleItemName(String fullClass) {
    int dot = fullClass.lastIndexOf('.');
    String name = dot >= 0 ? fullClass.substring(dot + 1) : fullClass;
    if (name.endsWith("Item")) name = name.substring(0, name.length() - 4);
    return name.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
  }

  private Label.LabelStyle makeLabelStyle(Color color) {
    Label.LabelStyle style = new Label.LabelStyle();
    style.font = font;
    style.fontColor = color;
    return style;
  }

  private Drawable getPanelDrawable() {
    if (panelTexture == null) {
      Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
      pixmap.setColor(0.18f, 0.18f, 0.18f, 0.9f);
      pixmap.fill();
      panelTexture = new Texture(pixmap);
      pixmap.dispose();
    }
    return new TextureRegionDrawable(panelTexture);
  }

  private Drawable createBackground() {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(0.1f, 0.1f, 0.1f, 0.9f);
    pixmap.fill();
    backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    return new TextureRegionDrawable(backgroundTexture);
  }

  private void close() {
    hudManager.close();
  }

  private void scheduleRefresh() {
    new Thread(
            () -> {
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
              rebuildUI();
              refresh();
            })
        .start();
  }

  private static boolean isDescendantOf(Actor actor, Actor parent) {
    Actor current = actor;
    while (current != null) {
      if (current == parent) return true;
      current = current.getParent();
    }
    return false;
  }
}
