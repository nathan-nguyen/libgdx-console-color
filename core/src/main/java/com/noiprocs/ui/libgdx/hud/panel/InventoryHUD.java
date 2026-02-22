package com.noiprocs.ui.libgdx.hud.panel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.model.InventoryContainerInterface;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.item.Equipment;
import com.noiprocs.core.model.item.Inventory;
import com.noiprocs.core.model.item.Item;
import com.noiprocs.core.model.item.ItemCategory;
import com.noiprocs.core.model.mob.character.HumanoidModel;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.resources.ItemTextureManager;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import com.noiprocs.ui.libgdx.hud.HUDManager;
import com.noiprocs.ui.libgdx.hud.ItemDragDropHandler;
import com.noiprocs.ui.libgdx.hud.widget.ItemIconRenderer;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotStyle;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotWidget;

/**
 * Inventory interaction HUD screen. Displays player inventory and external container (chest)
 * side-by-side for transferring items.
 */
public class InventoryHUD {
  private final LibGDXGameScreen gameScreen;
  private final BitmapFont font;
  private final Table rootTable;
  private final ItemSlotStyle slotStyle;
  private final Viewport viewport;
  private final ItemDragDropHandler dragDropManager;
  private final DragAndDrop dragAndDrop;
  private final ItemTextureManager itemTextureManager;
  private Texture backgroundTexture;
  private Texture panelTexture;
  private final Table mainContainer;

  private String containerModelId;

  // Player inventory slots
  private ItemSlotWidget[] playerInventorySlots;
  private Label[] playerInventoryNameLabels;
  private static final int PLAYER_INVENTORY_SIZE = 9; // 3x3 grid (9 slots)

  // Container slots
  private ItemSlotWidget[] containerSlots;
  private Label[] containerNameLabels;
  private static final int CONTAINER_SIZE = 9; // 3x3 grid (9 slots)

  // Equipment slots (for HumanoidModel)
  private ItemSlotWidget helmetSlot;
  private ItemSlotWidget chestPlateSlot;
  private ItemSlotWidget leggingSlot;
  private ItemSlotWidget bootSlot;
  private Label helmetNameLabel;
  private Label chestPlateNameLabel;
  private Label leggingNameLabel;
  private Label bootNameLabel;

  private Table equipmentPanel;
  private Label healthValueLabel;
  private Label speedValueLabel;

  public InventoryHUD(
      LibGDXGameScreen gameScreen,
      Viewport viewport,
      BitmapFont font,
      ItemSlotStyle slotStyle,
      ItemTextureManager itemTextureManager) {
    this.gameScreen = gameScreen;
    this.viewport = viewport;
    this.font = font;
    this.rootTable = new Table();
    this.rootTable.setFillParent(true);
    this.slotStyle = slotStyle;
    this.itemTextureManager = itemTextureManager;
    this.dragDropManager = new ItemDragDropHandler();
    this.dragAndDrop = new DragAndDrop();
    this.mainContainer = new Table();

    buildUI();
  }

  private void buildUI() {
    rootTable.setFillParent(true);
    rootTable.setTouchable(Touchable.enabled);
    rootTable.center();

    mainContainer.setBackground(createBackground());
    mainContainer.setTouchable(Touchable.enabled);

    // Use responsive sizing - 90% of screen width/height, max 650x550 for humanoid
    // Size will be adjusted in rebuildUI based on container type
    rootTable.add(mainContainer);

    // Close overlay when clicking outside
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

  private void rebuildUI() {
    mainContainer.clear();

    Model containerModel = GameContext.get().modelManager.getModel(containerModelId);
    if (containerModel == null) return;

    // Header with title and close button
    Table header = createHeader();
    mainContainer.add(header).expandX().fillX().pad(5);
    mainContainer.row();

    // Content area: Player inventory on left, Container/Equipment on right
    Table content = new Table();

    // Player inventory panel
    Table playerInventoryPanel = createPlayerInventoryPanel();
    playerInventoryPanel.setBackground(getPanelDrawable());
    playerInventoryPanel.pad(8, 0, 8, 0);
    content.add(playerInventoryPanel).pad(8).top();

    // Add appropriate right panel based on container type
    Table containerPanel;
    if (isHumanoidButNotPlayer(containerModel)) {
      // Add equipment panel and humanoid inventory for HumanoidModel
      Table rightPanel = new Table();
      rightPanel.top();

      equipmentPanel = createEquipmentPanel();
      rightPanel.add(equipmentPanel).padBottom(5);
      rightPanel.row();

      // Add stats display
      Table statsPanel = createStatsPanel((HumanoidModel) containerModel);
      rightPanel.add(statsPanel).padBottom(5);
      rightPanel.row();

      // Add humanoid inventory
      Label.LabelStyle labelStyle = new Label.LabelStyle();
      labelStyle.font = font;
      labelStyle.fontColor = Color.WHITE;
      Label inventoryLabel = new Label("Humanoid Inventory", labelStyle);
      inventoryLabel.setFontScale(0.9f);
      rightPanel.add(inventoryLabel).padBottom(5);
      rightPanel.row();

      containerPanel = createContainerPanel();
      rightPanel.add(containerPanel);

      // Wrap right panel in scroll pane
      ScrollPane scrollPane = new ScrollPane(rightPanel);
      scrollPane.setScrollingDisabled(true, false);
      scrollPane.setFadeScrollBars(false);
      scrollPane.setOverscroll(false, false);

      Table rightBox = new Table();
      rightBox.setBackground(getPanelDrawable());
      rightBox.pad(8, 0, 8, 0);
      rightBox.add(scrollPane).expand().fill();
      content.add(rightBox).pad(8).top();
    } else {
      // Use regular container panel for chests - align with player inventory
      Table chestColumn = new Table();
      chestColumn.top();

      Label.LabelStyle labelStyle = new Label.LabelStyle();
      labelStyle.font = font;
      labelStyle.fontColor = Color.WHITE;
      Label chestLabel = new Label("Chest", labelStyle);
      chestLabel.setFontScale(0.9f);
      chestColumn.add(chestLabel).padBottom(5);
      chestColumn.row();

      containerPanel = createContainerPanel();
      chestColumn.add(containerPanel);

      chestColumn.setBackground(getPanelDrawable());
      chestColumn.pad(8, 0, 8, 0);
      content.add(chestColumn).pad(8).top();
    }

    mainContainer.add(content).expand().fill().pad(5);

    // Update responsive sizing - 90% of screen width/height, max 650x550 for humanoid
    float maxWidth =
        isHumanoidButNotPlayer(containerModel)
            ? Math.min(viewport.getWorldWidth() * 0.9f, 650)
            : Math.min(viewport.getWorldWidth() * 0.9f, 500);
    float maxHeight = Math.min(viewport.getWorldHeight() * 0.9f, 550);
    rootTable.getCell(mainContainer).width(maxWidth).height(maxHeight);
  }

  private boolean isHumanoidButNotPlayer(Model model) {
    return model instanceof HumanoidModel && !(model instanceof PlayerModel);
  }

  private Table createHeader() {
    Table header = new Table();

    // Title
    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = Color.WHITE;

    String title = "CONTAINER";
    if (containerModelId != null) {
      Model containerModel = GameContext.get().modelManager.getModel(containerModelId);
      if (isHumanoidButNotPlayer(containerModel)) {
        title = "HUMANOID";
      }
    }

    Label titleLabel = new Label(title, labelStyle);
    titleLabel.setFontScale(1.2f);

    header.add(titleLabel).expandX().center().padLeft(10);

    return header;
  }

  private Table createPlayerInventoryPanel() {
    Table panel = new Table();
    panel.top();

    // Label
    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = Color.WHITE;
    Label label = new Label("Player Inventory", labelStyle);
    label.setFontScale(0.9f);
    panel.add(label).colspan(3).padBottom(5);
    panel.row();

    Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
    nameLabelStyle.font = font;
    nameLabelStyle.fontColor = Color.WHITE;

    // Create inventory slots in a 3x3 grid (9 slots total)
    playerInventorySlots = new ItemSlotWidget[PLAYER_INVENTORY_SIZE];
    playerInventoryNameLabels = new Label[PLAYER_INVENTORY_SIZE];
    for (int i = 0; i < PLAYER_INVENTORY_SIZE; i++) {
      ItemSlotWidget slot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
      playerInventorySlots[i] = slot;

      Label nameLabel = new Label("", nameLabelStyle);
      nameLabel.setFontScale(0.6f);
      nameLabel.setAlignment(Align.center);
      nameLabel.setWrap(true);
      playerInventoryNameLabels[i] = nameLabel;

      Table slotEntry = new Table();
      slotEntry.add(slot).size(48, 48);
      slotEntry.row();
      slotEntry.add(nameLabel).width(48).height(28).padTop(2);
      panel.add(slotEntry).pad(1);

      // New row every 3 slots
      if ((i + 1) % 3 == 0) {
        panel.row();
      }
    }

    return panel;
  }

  private Table createContainerPanel() {
    Table panel = new Table();

    Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
    nameLabelStyle.font = font;
    nameLabelStyle.fontColor = Color.WHITE;

    // Create container slots in a 3x3 grid (9 slots total)
    containerSlots = new ItemSlotWidget[CONTAINER_SIZE];
    containerNameLabels = new Label[CONTAINER_SIZE];
    for (int i = 0; i < CONTAINER_SIZE; i++) {
      ItemSlotWidget slot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
      containerSlots[i] = slot;

      Label nameLabel = new Label("", nameLabelStyle);
      nameLabel.setFontScale(0.6f);
      nameLabel.setAlignment(Align.center);
      nameLabel.setWrap(true);
      containerNameLabels[i] = nameLabel;

      Table slotEntry = new Table();
      slotEntry.add(slot).size(48, 48);
      slotEntry.row();
      slotEntry.add(nameLabel).width(48).height(28).padTop(2);
      panel.add(slotEntry).pad(1);

      // New row every 3 slots
      if ((i + 1) % 3 == 0) {
        panel.row();
      }
    }

    return panel;
  }

  private Table createEquipmentPanel() {
    Table panel = new Table();

    // Label
    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = Color.WHITE;
    Label label = new Label("Equipment", labelStyle);
    label.setFontScale(0.9f);
    panel.add(label).colspan(4).padBottom(5);
    panel.row();

    // Create equipment slots
    helmetSlot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
    chestPlateSlot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
    leggingSlot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
    bootSlot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);

    // Set slot icons as placeholders
    TextureRegion helmetRegion = itemTextureManager.getEquipmentSlotTexture("HELMET");
    if (helmetRegion != null) helmetSlot.setEmptySlotTexture(helmetRegion);
    TextureRegion chestRegion = itemTextureManager.getEquipmentSlotTexture("CHEST PLATE");
    if (chestRegion != null) chestPlateSlot.setEmptySlotTexture(chestRegion);
    TextureRegion leggingRegion = itemTextureManager.getEquipmentSlotTexture("LEGGING");
    if (leggingRegion != null) leggingSlot.setEmptySlotTexture(leggingRegion);
    TextureRegion bootRegion = itemTextureManager.getEquipmentSlotTexture("BOOT");
    if (bootRegion != null) bootSlot.setEmptySlotTexture(bootRegion);

    Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
    nameLabelStyle.font = font;
    nameLabelStyle.fontColor = Color.WHITE;

    helmetNameLabel = new Label("", nameLabelStyle);
    helmetNameLabel.setFontScale(0.6f);
    helmetNameLabel.setAlignment(Align.center);
    helmetNameLabel.setWrap(true);

    chestPlateNameLabel = new Label("", nameLabelStyle);
    chestPlateNameLabel.setFontScale(0.6f);
    chestPlateNameLabel.setAlignment(Align.center);
    chestPlateNameLabel.setWrap(true);

    leggingNameLabel = new Label("", nameLabelStyle);
    leggingNameLabel.setFontScale(0.6f);
    leggingNameLabel.setAlignment(Align.center);
    leggingNameLabel.setWrap(true);

    bootNameLabel = new Label("", nameLabelStyle);
    bootNameLabel.setFontScale(0.6f);
    bootNameLabel.setAlignment(Align.center);
    bootNameLabel.setWrap(true);

    // Horizontal layout: all 4 slots in a row with icons as placeholders
    Table helmetEntry = new Table();
    helmetEntry.add(helmetSlot).size(48, 48);
    helmetEntry.row();
    helmetEntry.add(helmetNameLabel).width(48).height(28).padTop(2);
    panel.add(helmetEntry).pad(1);

    Table chestEntry = new Table();
    chestEntry.add(chestPlateSlot).size(48, 48);
    chestEntry.row();
    chestEntry.add(chestPlateNameLabel).width(48).height(28).padTop(2);
    panel.add(chestEntry).pad(1);

    Table leggingEntry = new Table();
    leggingEntry.add(leggingSlot).size(48, 48);
    leggingEntry.row();
    leggingEntry.add(leggingNameLabel).width(48).height(28).padTop(2);
    panel.add(leggingEntry).pad(1);

    Table bootEntry = new Table();
    bootEntry.add(bootSlot).size(48, 48);
    bootEntry.row();
    bootEntry.add(bootNameLabel).width(48).height(28).padTop(2);
    panel.add(bootEntry).pad(1);

    return panel;
  }

  private Table createStatsPanel(HumanoidModel humanoidModel) {
    Table panel = new Table();

    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = Color.WHITE;

    Label.LabelStyle valueLabelStyle = new Label.LabelStyle();
    valueLabelStyle.font = font;
    valueLabelStyle.fontColor = Color.YELLOW;

    // Health stat
    Label healthLabel = new Label("Health: ", labelStyle);
    healthLabel.setFontScale(0.8f);
    healthValueLabel = new Label(String.valueOf(humanoidModel.getHealth()), valueLabelStyle);
    healthValueLabel.setFontScale(0.8f);

    // Speed stat
    Label speedLabel = new Label("  Speed: ", labelStyle);
    speedLabel.setFontScale(0.8f);
    speedValueLabel = new Label(humanoidModel.getSpeed().toString(), valueLabelStyle);
    speedValueLabel.setFontScale(0.8f);

    // Add stats in a horizontal layout
    panel.add(healthLabel).padRight(2);
    panel.add(healthValueLabel).padRight(5);
    panel.add(speedLabel).padRight(2);
    panel.add(speedValueLabel);

    return panel;
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
    // Create semi-transparent dark background
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(0.1f, 0.1f, 0.1f, 0.9f);
    pixmap.fill();
    backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    return new TextureRegionDrawable(backgroundTexture);
  }

  private void setupDragAndDrop() {
    // Clear existing drag and drop bindings
    dragAndDrop.clear();

    // Check if we're interacting with a HumanoidModel
    boolean isHumanoidInteraction = false;
    if (containerModelId != null) {
      Model containerModel = GameContext.get().modelManager.getModel(containerModelId);
      if (isHumanoidButNotPlayer(containerModel)) {
        isHumanoidInteraction = true;
        setupEquipmentDragAndDrop();
      }
    }

    // Setup click handlers for inventory transfers
    setupInventoryClickHandlers();

    // Only setup drag-and-drop for inventory if interacting with humanoid (for equipment)
    if (!isHumanoidInteraction) {
      return;
    }

    // Setup drag sources and drop targets for container slots (for equipment interaction)
    for (int i = 0; i < containerSlots.length; i++) {
      final int slotIndex = i;
      ItemSlotWidget slot = containerSlots[i];

      // Make container slot a drag source (for equipping)
      dragAndDrop.addSource(
          new Source(slot) {
            @Override
            public Payload dragStart(InputEvent event, float x, float y, int pointer) {
              if (slot.isEmpty()) {
                return null;
              }

              Payload payload = new Payload();
              payload.setObject(new DragPayload(slot.getItem(), "CONTAINER", slotIndex));

              slot.setDragging(true);

              payload.setDragActor(
                  ItemSlotWidget.createDragActor(slot.getItem(), font, itemTextureManager));

              return payload;
            }

            @Override
            public void dragStop(
                InputEvent event, float x, float y, int pointer, Payload payload, Target target) {
              slot.setDragging(false);
            }
          });

      // Make container slot a drop target (for unequipping)
      dragAndDrop.addTarget(
          new Target(slot) {
            @Override
            public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
              DragPayload dragPayload = (DragPayload) payload.getObject();

              // Only accept drops from equipment
              if ("EQUIPMENT".equals(dragPayload.sourceContainer)) {
                slot.setHovered(true);
                return true;
              }
              return false;
            }

            @Override
            public void drop(Source source, Payload payload, float x, float y, int pointer) {
              DragPayload dragPayload = (DragPayload) payload.getObject();

              if ("EQUIPMENT".equals(dragPayload.sourceContainer)) {
                // Unequipping from equipment to humanoid inventory
                dragDropManager.executeUnequip(containerModelId, dragPayload.equipmentSlotType);
                scheduleRefresh();
              }
            }

            @Override
            public void reset(Source source, Payload payload) {
              slot.setHovered(false);
            }
          });
    }
  }

  private void setupInventoryClickHandlers() {
    // Setup click handlers for player inventory
    for (int i = 0; i < playerInventorySlots.length; i++) {
      final int slotIndex = i;
      ItemSlotWidget slot = playerInventorySlots[i];

      slot.clearListeners();
      slot.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              if (slot.isEmpty()) {
                return;
              }

              // Transfer from player to container
              Object itemObj = slot.getItem();
              if (!(itemObj instanceof Item)) {
                return;
              }
              Item item = (Item) itemObj;
              dragDropManager.executeTransfer(
                  item, "PLAYER", "CONTAINER", slotIndex, 0, containerModelId);
              scheduleRefresh();
            }
          });
    }

    // Setup click handlers for container inventory
    // ClickListener only fires on actual clicks, not drags
    for (int i = 0; i < containerSlots.length; i++) {
      final int slotIndex = i;
      ItemSlotWidget slot = containerSlots[i];

      slot.clearListeners();
      slot.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              if (slot.isEmpty()) {
                return;
              }

              // Transfer from container to player
              Object itemObj = slot.getItem();
              if (!(itemObj instanceof Item)) {
                return;
              }
              Item item = (Item) itemObj;
              dragDropManager.executeTransfer(
                  item, "CONTAINER", "PLAYER", slotIndex, 0, containerModelId);
              scheduleRefresh();
            }
          });
    }
  }

  private void setupEquipmentDragAndDrop() {
    if (equipmentPanel == null
        || helmetSlot == null
        || chestPlateSlot == null
        || leggingSlot == null
        || bootSlot == null) {
      return;
    }

    ItemSlotWidget[] equipmentSlots = {helmetSlot, chestPlateSlot, leggingSlot, bootSlot};
    String[] equipmentSlotTypes = {"HELMET", "CHEST PLATE", "LEGGING", "BOOT"};

    for (int i = 0; i < equipmentSlots.length; i++) {
      final ItemSlotWidget slot = equipmentSlots[i];
      final String slotType = equipmentSlotTypes[i];

      // Make equipment slot a drag source (for unequipping)
      dragAndDrop.addSource(
          new Source(slot) {
            @Override
            public Payload dragStart(InputEvent event, float x, float y, int pointer) {
              if (slot.isEmpty()) {
                return null;
              }

              Payload payload = new Payload();
              payload.setObject(new DragPayload(slot.getItem(), "EQUIPMENT", -1, slotType));

              slot.setDragging(true);

              payload.setDragActor(
                  ItemSlotWidget.createDragActor(slot.getItem(), font, itemTextureManager));

              return payload;
            }

            @Override
            public void dragStop(
                InputEvent event, float x, float y, int pointer, Payload payload, Target target) {
              slot.setDragging(false);
            }
          });

      // Make equipment slot a drop target (for equipping)
      dragAndDrop.addTarget(
          new Target(slot) {
            @Override
            public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
              DragPayload dragPayload = (DragPayload) payload.getObject();
              Object item = dragPayload.item;

              boolean canDrop = dragDropManager.canEquip(item, slotType);
              slot.setHovered(canDrop);
              return canDrop;
            }

            @Override
            public void drop(Source source, Payload payload, float x, float y, int pointer) {
              DragPayload dragPayload = (DragPayload) payload.getObject();

              if ("CONTAINER".equals(dragPayload.sourceContainer)) {
                // Equipping from humanoid's inventory to humanoid's equipment
                dragDropManager.executeEquip(
                    containerModelId, dragPayload.sourceSlotIndex, slotType);
                scheduleRefresh();
              }
            }

            @Override
            public void reset(Source source, Payload payload) {
              slot.setHovered(false);
            }
          });
    }
  }

  /** Payload data for drag-drop operations. */
  private static class DragPayload {
    Object item;
    String sourceContainer; // "PLAYER", "CONTAINER", or "EQUIPMENT"
    int sourceSlotIndex;
    String equipmentSlotType; // For equipment slots

    DragPayload(Object item, String sourceContainer, int sourceSlotIndex) {
      this(item, sourceContainer, sourceSlotIndex, null);
    }

    DragPayload(
        Object item, String sourceContainer, int sourceSlotIndex, String equipmentSlotType) {
      this.item = item;
      this.sourceContainer = sourceContainer;
      this.sourceSlotIndex = sourceSlotIndex;
      this.equipmentSlotType = equipmentSlotType;
    }
  }

  public void setContainer(String containerModelId) {
    this.containerModelId = containerModelId;
    rebuildUI();
    setupDragAndDrop();
  }

  public void refresh() {
    GameContext gameContext = GameContext.get();
    // Get player model
    PlayerModel player = (PlayerModel) gameContext.modelManager.getModel(gameContext.username);
    if (player == null) {
      return;
    }

    // Refresh player inventory slots with actual inventory data
    Inventory playerInventory = player.getInventory();
    for (int i = 0; i < PLAYER_INVENTORY_SIZE; i++) {
      Item item = playerInventory.getItem(i);
      if (item != null) {
        playerInventorySlots[i].setItem(item, item.amount);
        playerInventoryNameLabels[i].setText(getItemDisplayName(item));
      } else {
        playerInventorySlots[i].clear();
        playerInventoryNameLabels[i].setText("");
      }
    }

    // Refresh container slots with actual container inventory data
    if (containerModelId != null) {
      Model containerModel = gameContext.modelManager.getModel(containerModelId);

      if (isHumanoidButNotPlayer(containerModel)) {
        // Refresh equipment and inventory for HumanoidModel
        HumanoidModel humanoidModel = (HumanoidModel) containerModel;

        // Refresh stats
        if (healthValueLabel != null) {
          healthValueLabel.setText(String.valueOf(humanoidModel.getHealth()));
        }
        if (speedValueLabel != null) {
          speedValueLabel.setText(humanoidModel.getSpeed().toString());
        }

        // Refresh equipment slots
        Equipment equipment = humanoidModel.getEquipment();
        if (helmetSlot != null) {
          Item helmetItem = equipment.getItem(ItemCategory.HELMET);
          if (helmetItem != null) {
            helmetSlot.setItem(helmetItem, helmetItem.amount);
            helmetNameLabel.setText(getItemDisplayName(helmetItem));
          } else {
            helmetSlot.clear();
            helmetNameLabel.setText("");
          }
        }

        if (chestPlateSlot != null) {
          Item chestItem = equipment.getItem(ItemCategory.CHEST_PLATE);
          if (chestItem != null) {
            chestPlateSlot.setItem(chestItem, chestItem.amount);
            chestPlateNameLabel.setText(getItemDisplayName(chestItem));
          } else {
            chestPlateSlot.clear();
            chestPlateNameLabel.setText("");
          }
        }

        if (leggingSlot != null) {
          Item leggingItem = equipment.getItem(ItemCategory.LEGGING);
          if (leggingItem != null) {
            leggingSlot.setItem(leggingItem, leggingItem.amount);
            leggingNameLabel.setText(getItemDisplayName(leggingItem));
          } else {
            leggingSlot.clear();
            leggingNameLabel.setText("");
          }
        }

        if (bootSlot != null) {
          Item bootItem = equipment.getItem(ItemCategory.BOOT);
          if (bootItem != null) {
            bootSlot.setItem(bootItem, bootItem.amount);
            bootNameLabel.setText(getItemDisplayName(bootItem));
          } else {
            bootSlot.clear();
            bootNameLabel.setText("");
          }
        }

        // Refresh humanoid's inventory
        Inventory humanoidInventory = humanoidModel.getInventory();
        for (int i = 0; i < CONTAINER_SIZE && i < humanoidInventory.getMaxInventorySize(); i++) {
          Item item = humanoidInventory.getItem(i);
          if (item != null) {
            containerSlots[i].setItem(item, item.amount);
            containerNameLabels[i].setText(getItemDisplayName(item));
          } else {
            containerSlots[i].clear();
            containerNameLabels[i].setText("");
          }
        }
      } else if (containerModel instanceof InventoryContainerInterface) {
        // Refresh regular container inventory
        Inventory containerInventory =
            ((InventoryContainerInterface) containerModel).getInventory();
        for (int i = 0; i < CONTAINER_SIZE && i < containerInventory.getMaxInventorySize(); i++) {
          Item item = containerInventory.getItem(i);
          if (item != null) {
            containerSlots[i].setItem(item, item.amount);
            containerNameLabels[i].setText(getItemDisplayName(item));
          } else {
            containerSlots[i].clear();
            containerNameLabels[i].setText("");
          }
        }
      }
    }
  }

  private static String getItemDisplayName(Object item) {
    if (item == null) return "";
    ItemIconRenderer.ItemIcon icon = ItemIconRenderer.renderIcon(item);
    if (icon != null) return icon.displayName;
    String name = item.getClass().getSimpleName();
    if (name.endsWith("Item")) name = name.substring(0, name.length() - 4);
    return name.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
  }

  private void close() {
    // Close HUD by calling manager
    HUDManager hudManager = gameScreen.getHudManager();
    if (hudManager != null) {
      hudManager.close();
    }
  }

  /** Schedules a UI refresh after a short delay to allow server to process commands. */
  private void scheduleRefresh() {
    new Thread(
            () -> {
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
              refresh();
            })
        .start();
  }

  public Actor getRoot() {
    return rootTable;
  }

  public void dispose() {
    // Don't dispose slotStyle - it's shared across all HUDs
    if (backgroundTexture != null) {
      backgroundTexture.dispose();
    }
    if (panelTexture != null) {
      panelTexture.dispose();
    }
  }
}
