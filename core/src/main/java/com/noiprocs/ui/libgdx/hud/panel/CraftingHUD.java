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
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.model.item.Inventory;
import com.noiprocs.core.model.item.receipt.Crafter;
import com.noiprocs.core.model.item.receipt.CraftingReceipt;
import com.noiprocs.core.model.item.receipt.CraftingReceipt.MaterialEntry;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import com.noiprocs.ui.libgdx.hud.HUDManager;
import com.noiprocs.ui.libgdx.hud.ItemDragDropHandler;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotStyle;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotWidget;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Crafting HUD screen. Displays recipe list and required materials for crafting items. */
public class CraftingHUD {

  private final GameContext gameContext;
  private final LibGDXGameScreen gameScreen;
  private final BitmapFont font;
  private final Table rootTable;
  private final ItemSlotStyle slotStyle;
  private final Viewport viewport;
  private final ItemDragDropHandler dragDropManager;
  private Texture backgroundTexture;

  // UI Components
  private TextButton craftButton;
  private Table recipeListTable;
  private Table materialsPanel;

  // Recipe data
  private final List<Class<?>> recipeList;
  private final List<TextButton> recipeButtons;
  private int selectedRecipeIndex = -1;

  public CraftingHUD(
      GameContext gameContext,
      LibGDXGameScreen gameScreen,
      Viewport viewport,
      BitmapFont font,
      ItemSlotStyle slotStyle) {
    this.gameContext = gameContext;
    this.gameScreen = gameScreen;
    this.viewport = viewport;
    this.font = font;
    this.rootTable = new Table();
    this.rootTable.setFillParent(true);
    this.slotStyle = slotStyle; // Use shared style
    this.dragDropManager = new ItemDragDropHandler(gameContext, gameContext.username);
    this.recipeList = new ArrayList<>();
    this.recipeButtons = new ArrayList<>();

    buildUI();
  }

  private void buildUI() {
    rootTable.setFillParent(true);
    rootTable.setTouchable(Touchable.enabled);
    rootTable.center();

    // Main container with background
    final Table mainContainer = new Table();
    mainContainer.setBackground(createBackground());
    mainContainer.setTouchable(Touchable.enabled);

    // Header with title and close button
    Table header = createHeader();
    mainContainer.add(header).expandX().fillX().pad(10);
    mainContainer.row();

    // Content area: Recipe list on left, Materials on right
    Table content = new Table();

    // Recipe list panel - use expand instead of fixed width
    Table recipePanel = createRecipePanel();
    content.add(recipePanel).expand().fill().pad(10).top();

    // Materials panel - use expand instead of fixed width
    materialsPanel = createMaterialsPanel();
    content.add(materialsPanel).expand().fill().pad(10).top();

    mainContainer.add(content).expand().fill();

    // Use responsive sizing - 90% of screen width/height, max 500x400
    float maxWidth = Math.min(viewport.getWorldWidth() * 0.9f, 500);
    float maxHeight = Math.min(viewport.getWorldHeight() * 0.9f, 400);
    rootTable.add(mainContainer).width(maxWidth).height(maxHeight);

    // Close overlay when clicking outside - use capture listener like MenuOverlay
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

  private boolean isDescendantOf(Actor actor, Actor parent) {
    if (actor == null) return false;
    Actor current = actor;
    while (current != null) {
      if (current == parent) return true;
      current = current.getParent();
    }
    return false;
  }

  private Table createHeader() {
    Table header = new Table();

    // Title
    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = Color.WHITE;
    Label titleLabel = new Label("CRAFTING", labelStyle);
    titleLabel.setFontScale(1.2f);

    header.add(titleLabel).expandX().center().padLeft(10);

    return header;
  }

  private Table createRecipePanel() {
    Table panel = new Table();

    // Label
    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = Color.WHITE;
    Label label = new Label("Recipes", labelStyle);
    panel.add(label).padBottom(10);
    panel.row();

    // Recipe list table (scrollable)
    recipeListTable = new Table();
    recipeListTable.top();

    // Create scroll pane
    ScrollPane recipeScrollPane = new ScrollPane(recipeListTable);
    recipeScrollPane.setFadeScrollBars(false);
    recipeScrollPane.setScrollingDisabled(true, false);

    panel.add(recipeScrollPane).expand().fill();

    return panel;
  }

  private Table createMaterialsPanel() {
    Table panel = new Table();

    // Label
    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = Color.WHITE;
    Label label = new Label("Required Materials", labelStyle);
    panel.add(label).padBottom(10);
    panel.row();

    // Materials container (will be populated when a recipe is selected)
    Table materialsContainer = new Table();
    materialsContainer.center();

    Label noSelectionLabel = new Label("Select a recipe", labelStyle);
    noSelectionLabel.setFontScale(0.8f);
    noSelectionLabel.setColor(Color.GRAY);
    materialsContainer.add(noSelectionLabel);

    panel.add(materialsContainer).expand().fill();
    panel.row();

    // Craft button
    TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
    buttonStyle.font = font;
    buttonStyle.fontColor = Color.WHITE;
    buttonStyle.up = slotStyle.emptyBackground;
    buttonStyle.over = slotStyle.hoverBackground;
    buttonStyle.down = slotStyle.draggingBackground;

    craftButton = new TextButton("Craft", buttonStyle);
    craftButton.setDisabled(true);
    craftButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (!craftButton.isDisabled()) {
              craftSelectedRecipe();
            }
          }
        });

    panel.add(craftButton).width(150).height(40).padTop(10);

    return panel;
  }

  private Drawable createBackground() {
    // Create semi-transparent dark background
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(0.1f, 0.1f, 0.1f, 0.9f);
    pixmap.fill();
    backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    return new TextureRegionDrawable(backgroundTexture);
  }

  public void refresh() {
    // Get player model
    PlayerModel player = (PlayerModel) gameContext.modelManager.getModel(gameContext.username);
    if (player == null) {
      return;
    }

    // Get player inventory
    Inventory inventory = player.getInventory();

    // Build list of craftable recipes from Crafter
    Map<Class<?>, CraftingReceipt> receiptMap = Crafter.get().getAllReceipts();
    recipeList.clear();

    for (Map.Entry<Class<?>, CraftingReceipt> entry : receiptMap.entrySet()) {
      CraftingReceipt receipt = entry.getValue();
      boolean canCraft = true;

      // Check if player has enough materials for this recipe
      for (MaterialEntry material : receipt.materials) {
        int available = inventory.countItems(material.itemClass);
        if (available < material.amount) {
          canCraft = false;
          break;
        }
      }

      if (canCraft) {
        recipeList.add(entry.getKey());
      }
    }

    // Sort alphabetically by simple class name for consistent display
    recipeList.sort(Comparator.comparing(Class::getSimpleName));

    refreshRecipeList();

    // Refresh materials panel if a recipe is currently selected
    if (selectedRecipeIndex >= 0 && selectedRecipeIndex < recipeList.size()) {
      refreshMaterialsPanel();
    }
  }

  private void refreshRecipeList() {
    recipeListTable.clear();
    recipeButtons.clear();

    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = Color.WHITE;

    for (int i = 0; i < recipeList.size(); i++) {
      final int recipeIndex = i;
      Class<?> itemClass = recipeList.get(i);

      // Get recipe name (remove "Item" suffix if present)
      String recipeName = itemClass.getSimpleName();
      if (recipeName.endsWith("Item")) {
        recipeName = recipeName.substring(0, recipeName.length() - 4);
      }

      // Recipe button - use selected style if this is the selected recipe
      TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
      buttonStyle.font = font;
      buttonStyle.fontColor = Color.WHITE;
      buttonStyle.up =
          (i == selectedRecipeIndex) ? slotStyle.selectedBackground : slotStyle.emptyBackground;
      buttonStyle.over = slotStyle.hoverBackground;
      buttonStyle.down = slotStyle.filledBackground;

      TextButton recipeButton = new TextButton(recipeName, buttonStyle);
      recipeButton.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              selectRecipe(recipeIndex);
            }
          });

      recipeButtons.add(recipeButton);
      recipeListTable.add(recipeButton).expandX().fillX().height(35).padBottom(5);
      recipeListTable.row();
    }
  }

  private void selectRecipe(int index) {
    if (index < 0 || index >= recipeList.size()) {
      return;
    }

    // Update selected index
    int previousIndex = selectedRecipeIndex;
    selectedRecipeIndex = index;

    // Update button styles without rebuilding entire list
    if (previousIndex >= 0 && previousIndex < recipeButtons.size()) {
      TextButton previousButton = recipeButtons.get(previousIndex);
      TextButton.TextButtonStyle style = previousButton.getStyle();
      style.up = slotStyle.emptyBackground;
    }

    if (selectedRecipeIndex >= 0 && selectedRecipeIndex < recipeButtons.size()) {
      TextButton selectedButton = recipeButtons.get(selectedRecipeIndex);
      TextButton.TextButtonStyle style = selectedButton.getStyle();
      style.up = slotStyle.selectedBackground;
    }

    refreshMaterialsPanel();
  }

  private void refreshMaterialsPanel() {
    // Clear materials panel and rebuild
    materialsPanel.clear();

    // Label
    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = Color.WHITE;
    Label label = new Label("Required Materials", labelStyle);
    materialsPanel.add(label).padBottom(10);
    materialsPanel.row();

    if (selectedRecipeIndex < 0 || selectedRecipeIndex >= recipeList.size()) {
      Label noSelectionLabel = new Label("Select a recipe", labelStyle);
      noSelectionLabel.setFontScale(0.8f);
      noSelectionLabel.setColor(Color.GRAY);
      materialsPanel.add(noSelectionLabel).expand().center();
      materialsPanel.row();

      craftButton.setDisabled(true);
      return;
    }

    // Get player inventory
    PlayerModel player = (PlayerModel) gameContext.modelManager.getModel(gameContext.username);
    if (player == null) {
      return;
    }
    Inventory inventory = player.getInventory();

    // Show required materials for selected recipe
    Class<?> itemClass = recipeList.get(selectedRecipeIndex);
    Map<Class<?>, CraftingReceipt> receiptMap = Crafter.get().getAllReceipts();
    CraftingReceipt receipt = receiptMap.get(itemClass);

    Table materialsContainer = new Table();
    materialsContainer.center();

    boolean canCraft = true;

    for (MaterialEntry material : receipt.materials) {
      int available = inventory.countItems(material.itemClass);
      int needed = material.amount;

      // Check if player has enough of this material
      if (available < needed) {
        canCraft = false;
      }

      // Get material name (remove "Item" suffix if present)
      String materialName = material.itemClass.getSimpleName();
      if (materialName.endsWith("Item")) {
        materialName = materialName.substring(0, materialName.length() - 4);
      }

      // Create a slot showing the required material
      ItemSlotWidget materialSlot = new ItemSlotWidget(slotStyle, font, true);
      materialSlot.setItem(materialName, needed);

      // Create label showing available/needed amounts
      String amountText = String.format("%d/%d", available, needed);
      Label amountLabel = new Label(amountText, labelStyle);
      amountLabel.setFontScale(0.7f);
      amountLabel.setColor(available >= needed ? Color.GREEN : Color.RED);

      // Add material slot and amount label vertically
      Table materialEntry = new Table();
      materialEntry.add(materialSlot).size(52, 52);
      materialEntry.row();
      materialEntry.add(amountLabel).padTop(2);

      materialsContainer.add(materialEntry).pad(5);

      // Add every 3 materials per row
      if (materialsContainer.getCells().size % 3 == 0) {
        materialsContainer.row();
      }
    }

    materialsPanel.add(materialsContainer).expand().fill();
    materialsPanel.row();

    // Enable/disable craft button based on material availability
    craftButton.setDisabled(!canCraft);

    // Re-add craft button
    materialsPanel.add(craftButton).width(150).height(40).padTop(10);
  }

  private void craftSelectedRecipe() {
    if (selectedRecipeIndex < 0 || selectedRecipeIndex >= recipeList.size()) {
      return;
    }

    Class<?> itemClass = recipeList.get(selectedRecipeIndex);
    String className = itemClass.getName();
    dragDropManager.executeCraft(className);

    // Delay refresh to allow server to process command
    scheduleRefresh();
  }

  /** Schedules a UI refresh after a short delay to allow server to process commands. */
  private void scheduleRefresh() {
    new Thread(
            () -> {
              try {
                Thread.sleep(100); // Wait 100ms for server to process
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
              refresh();
            })
        .start();
  }

  private void close() {
    // Close HUD by calling manager
    HUDManager hudManager = gameScreen.getHudManager();
    if (hudManager != null) {
      hudManager.close();
    }
  }

  public Actor getRoot() {
    return rootTable;
  }

  public void dispose() {
    // Don't dispose slotStyle - it's shared across all HUDs
    // Dispose background texture
    if (backgroundTexture != null) {
      backgroundTexture.dispose();
    }
  }
}
