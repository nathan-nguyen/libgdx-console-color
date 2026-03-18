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
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
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
import com.noiprocs.core.model.item.Equipment;
import com.noiprocs.core.model.item.Inventory;
import com.noiprocs.core.model.item.Item;
import com.noiprocs.core.model.item.ItemCategory;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.resources.ItemTextureManager;
import com.noiprocs.ui.libgdx.hud.HUDManager;
import com.noiprocs.ui.libgdx.hud.ItemDragDropHandler;
import com.noiprocs.ui.libgdx.hud.widget.ItemIconRenderer;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotStyle;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotWidget;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Equipment management HUD screen. Displays armor slots and inventory for equipping/unequipping
 * items.
 */
public class EquipmentHUD {
  private static final Logger logger = LoggerFactory.getLogger(EquipmentHUD.class);

  private final HUDManager hudManager;
  private final BitmapFont font;
  private final Table rootTable;
  private final ItemSlotStyle slotStyle;
  private final Viewport viewport;
  private final ItemDragDropHandler dragDropManager;
  private final DragAndDrop dragAndDrop;
  private final ItemTextureManager itemTextureManager;
  private Texture backgroundTexture;

  // Equipment slots
  private final Map<String, ItemSlotWidget> equipmentSlots;
  private final Map<String, Label> equipmentNameLabels = new HashMap<>();
  private final Map<String, Long> slotTypeToCategory;
  private static final String[] EQUIPMENT_SLOT_TYPES = {"HELMET", "CHEST PLATE", "LEGGING", "BOOT"};

  // Trash slot for disposing items
  private ItemSlotWidget trashSlot;

  // Inventory slots
  private ItemSlotWidget[] inventorySlots;
  private Label[] inventoryNameLabels;
  private static final int INVENTORY_GRID_COLUMNS = 3; // 3x3 grid for 9 slots
  private static final int INVENTORY_SIZE = 9; // Player inventory has 9 slots
  private static final int HOTBAR_SIZE = 4; // First 4 slots are hotbar slots

  public EquipmentHUD(
      HUDManager hudManager,
      Viewport viewport,
      BitmapFont font,
      ItemSlotStyle slotStyle,
      ItemTextureManager itemTextureManager) {
    this.hudManager = hudManager;
    this.viewport = viewport;
    this.font = font;
    this.rootTable = new Table();
    this.rootTable.setFillParent(true);
    this.slotStyle = slotStyle;
    this.itemTextureManager = itemTextureManager;
    this.equipmentSlots = new HashMap<>();
    this.slotTypeToCategory = new HashMap<>();
    this.dragDropManager = new ItemDragDropHandler();
    this.dragAndDrop = new DragAndDrop();

    // Map slot types to ItemCategory constants
    slotTypeToCategory.put("HELMET", ItemCategory.HELMET);
    slotTypeToCategory.put("CHEST PLATE", ItemCategory.CHEST_PLATE);
    slotTypeToCategory.put("LEGGING", ItemCategory.LEGGING);
    slotTypeToCategory.put("BOOT", ItemCategory.BOOT);

    buildUI();
    setupDragAndDrop();
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

    // Content area: Equipment slots on left, Inventory on right
    Table content = new Table();

    // Equipment panel
    // UI Components
    Table equipmentPanel = createEquipmentPanel();
    content.add(equipmentPanel).pad(10).top();

    // Inventory panel
    Table inventoryPanel = createInventoryPanel();
    content.add(inventoryPanel).pad(10).top();

    mainContainer.add(content).expand().fill();

    // Use responsive sizing - 90% of screen width/height, max 500x400
    float maxWidth = Math.min(viewport.getWorldWidth() * 0.9f, 500);
    float maxHeight = Math.min(viewport.getWorldHeight() * 0.9f, 500);
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
    Label titleLabel = new Label("EQUIPMENT", labelStyle);
    titleLabel.setFontScale(1.2f);

    header.add(titleLabel).expandX().center().padLeft(10);

    // Crafting button
    TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
    buttonStyle.font = font;
    buttonStyle.fontColor = Color.WHITE;
    buttonStyle.up = slotStyle.emptyBackground;
    buttonStyle.over = slotStyle.hoverBackground;
    buttonStyle.down = slotStyle.draggingBackground;

    TextButton craftingButton = new TextButton("Crafting", buttonStyle);
    craftingButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            openCraftingHUD();
          }
        });

    header.add(craftingButton).width(100).height(35).padRight(10);

    return header;
  }

  private void openCraftingHUD() {
    hudManager.openCraftingHUD();
  }

  private Table createEquipmentPanel() {
    Table panel = new Table();

    // Label
    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = Color.WHITE;
    Label label = new Label("Armor Slots", labelStyle);
    panel.add(label).colspan(2).padBottom(10);
    panel.row();

    Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
    nameLabelStyle.font = font;
    nameLabelStyle.fontColor = Color.WHITE;

    // Create equipment slots for each armor piece
    for (String slotType : EQUIPMENT_SLOT_TYPES) {
      ItemSlotWidget slot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
      TextureRegion slotRegion = itemTextureManager.getEquipmentSlotTexture(slotType);
      if (slotRegion != null) {
        slot.setEmptySlotTexture(slotRegion);
      }
      equipmentSlots.put(slotType, slot);

      Label nameLabel = new Label("", nameLabelStyle);
      nameLabel.setFontScale(0.6f);
      nameLabel.setAlignment(Align.center);
      nameLabel.setWrap(true);
      equipmentNameLabels.put(slotType, nameLabel);

      Table slotEntry = new Table();
      slotEntry.add(slot).size(52, 52);
      slotEntry.row();
      slotEntry.add(nameLabel).width(52).height(28).padTop(2);
      panel.add(slotEntry);
      panel.row().padBottom(5);
    }

    return panel;
  }

  private Table createInventoryPanel() {
    Table panel = new Table();

    // Label
    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = Color.WHITE;
    Label label = new Label("Inventory", labelStyle);
    panel.add(label).colspan(INVENTORY_GRID_COLUMNS).padBottom(10);
    panel.row();

    Label.LabelStyle nameLabelStyle = new Label.LabelStyle();
    nameLabelStyle.font = font;
    nameLabelStyle.fontColor = Color.WHITE;

    // Create inventory slots in a 3x3 grid (9 slots total)
    inventorySlots = new ItemSlotWidget[INVENTORY_SIZE];
    inventoryNameLabels = new Label[INVENTORY_SIZE];
    for (int i = 0; i < INVENTORY_SIZE; i++) {
      ItemSlotWidget slot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
      inventorySlots[i] = slot;

      // Mark first 4 slots as hotbar slots
      if (i < HOTBAR_SIZE) {
        slot.setHotbarSlot(true);
      }

      Label nameLabel = new Label("", nameLabelStyle);
      nameLabel.setFontScale(0.6f);
      nameLabel.setAlignment(Align.center);
      nameLabel.setWrap(true);
      inventoryNameLabels[i] = nameLabel;

      Table slotEntry = new Table();
      slotEntry.add(slot).size(52, 52);
      slotEntry.row();
      slotEntry.add(nameLabel).width(52).height(28).padTop(2);
      panel.add(slotEntry).pad(2);

      // New row every 3 slots (3x3 grid)
      if ((i + 1) % INVENTORY_GRID_COLUMNS == 0) {
        panel.row();
      }
    }

    // Trash slot
    Label.LabelStyle trashLabelStyle = new Label.LabelStyle();
    trashLabelStyle.font = font;
    trashLabelStyle.fontColor = Color.RED;
    Label trashLabel = new Label("Dispose", trashLabelStyle);
    trashLabel.setFontScale(0.7f);
    panel.add(trashLabel).colspan(INVENTORY_GRID_COLUMNS).padTop(6).padBottom(2);
    panel.row();

    trashSlot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
    panel.add(trashSlot).size(52, 52).colspan(INVENTORY_GRID_COLUMNS);

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
    GameContext gameContext = GameContext.get();
    // Get player model
    PlayerModel player = (PlayerModel) gameContext.modelManager.getModel(gameContext.username);
    if (player == null) {
      return;
    }

    // Refresh equipment slots with actual equipment data
    Equipment equipment = player.getEquipment();
    for (String slotType : EQUIPMENT_SLOT_TYPES) {
      Long categoryId = slotTypeToCategory.get(slotType);
      if (categoryId != null) {
        Item item = equipment.getItem(categoryId);
        ItemSlotWidget slot = equipmentSlots.get(slotType);
        Label nameLabel = equipmentNameLabels.get(slotType);
        if (item != null) {
          slot.setItem(item, item.amount);
          if (nameLabel != null) nameLabel.setText(getItemDisplayName(item));
        } else {
          slot.clear();
          if (nameLabel != null) nameLabel.setText("");
        }
      }
    }

    // Get current selected slot
    int currentSlot = player.getCurrentInventorySlot();

    // Refresh inventory slots with actual inventory data
    Inventory inventory = player.getInventory();
    for (int i = 0; i < inventorySlots.length; i++) {
      Item item = inventory.getItem(i);
      if (item != null) {
        inventorySlots[i].setItem(item, item.amount);
        inventoryNameLabels[i].setText(getItemDisplayName(item));
      } else {
        inventorySlots[i].clear();
        inventoryNameLabels[i].setText("");
      }

      // Highlight current selected slot
      inventorySlots[i].setSelected(i == currentSlot);
    }
  }

  private void close() {
    hudManager.close();
  }

  public Actor getRoot() {
    return rootTable;
  }

  private void setupDragAndDrop() {
    // Setup drag sources and drop targets for equipment slots
    for (Map.Entry<String, ItemSlotWidget> entry : equipmentSlots.entrySet()) {
      String slotType = entry.getKey();
      ItemSlotWidget slot = entry.getValue();

      // Make equipment slot a drag source (when it has an item)
      dragAndDrop.addSource(
          new Source(slot) {
            @Override
            public Payload dragStart(InputEvent event, float x, float y, int pointer) {
              if (slot.isEmpty()) {
                return null; // Can't drag from empty slot
              }

              Payload payload = new Payload();
              payload.setObject(
                  new DragPayload(slot.getItem(), slotType, true, -1)); // isEquipmentSlot = true

              // Visual feedback
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

      // Make equipment slot a drop target
      dragAndDrop.addTarget(
          new Target(slot) {
            @Override
            public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
              DragPayload dragPayload = (DragPayload) payload.getObject();
              Object item = dragPayload.item;

              // Check if item can be equipped in this slot
              boolean canDrop = dragDropManager.canEquip(item, slotType);
              slot.setHovered(canDrop);
              return canDrop;
            }

            @Override
            public void drop(Source source, Payload payload, float x, float y, int pointer) {
              DragPayload dragPayload = (DragPayload) payload.getObject();

              if (dragPayload.isEquipmentSlot) {
                // Dragging from equipment to equipment: not directly supported
                // Would need to unequip first then equip from inventory
                logger.warn("Cannot drag directly between equipment slots");
              } else {
                // Dragging from inventory to equipment: equip the item
                logger.info(
                    "Equipping item from inventory slot {}", dragPayload.sourceInventoryIndex);
                dragDropManager.executeEquip(dragPayload.sourceInventoryIndex, slotType);
                // Delay refresh to allow server to process command
                scheduleRefresh();
              }
            }

            @Override
            public void reset(Source source, Payload payload) {
              slot.setHovered(false);
            }
          });
    }

    // Setup drag sources and drop targets for inventory slots
    for (int i = 0; i < inventorySlots.length; i++) {
      final int slotIndex = i;
      ItemSlotWidget slot = inventorySlots[i];

      // Make inventory slot a drag source
      dragAndDrop.addSource(
          new Source(slot) {
            @Override
            public Payload dragStart(InputEvent event, float x, float y, int pointer) {
              if (slot.isEmpty()) {
                return null; // Can't drag from empty slot
              }

              Payload payload = new Payload();
              payload.setObject(
                  new DragPayload(
                      slot.getItem(), null, false, slotIndex)); // isEquipmentSlot = false

              // Visual feedback
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

      // Make inventory slot a drop target
      dragAndDrop.addTarget(
          new Target(slot) {
            @Override
            public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
              DragPayload dragPayload = (DragPayload) payload.getObject();
              Object item = dragPayload.item;

              // Inventory accepts any item
              boolean canDrop = dragDropManager.canPlaceInInventory(item);
              slot.setHovered(canDrop);
              return canDrop;
            }

            @Override
            public void drop(Source source, Payload payload, float x, float y, int pointer) {
              DragPayload dragPayload = (DragPayload) payload.getObject();

              if (dragPayload.isEquipmentSlot) {
                // Unequipping from equipment to inventory
                logger.info("Unequipping from {}", dragPayload.sourceSlotType);
                dragDropManager.executeUnequip(dragPayload.sourceSlotType);
              } else {
                // Swapping between inventory slots
                logger.info(
                    "Swapping inventory slots {} and {}",
                    dragPayload.sourceInventoryIndex,
                    slotIndex);
                dragDropManager.executeSwap(dragPayload.sourceInventoryIndex, slotIndex);
              }

              // Delay refresh to allow server to process command
              scheduleRefresh();
            }

            @Override
            public void reset(Source source, Payload payload) {
              slot.setHovered(false);
            }
          });
    }

    // Setup trash slot as a drop target for disposing inventory items
    dragAndDrop.addTarget(
        new Target(trashSlot) {
          @Override
          public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
            DragPayload dragPayload = (DragPayload) payload.getObject();
            boolean accept = !dragPayload.isEquipmentSlot;
            trashSlot.setHovered(accept);
            return accept;
          }

          @Override
          public void drop(Source source, Payload payload, float x, float y, int pointer) {
            DragPayload dragPayload = (DragPayload) payload.getObject();
            if (!dragPayload.isEquipmentSlot) {
              dragDropManager.executeDispose(dragPayload.sourceInventoryIndex);
              scheduleRefresh();
            }
          }

          @Override
          public void reset(Source source, Payload payload) {
            trashSlot.setHovered(false);
          }
        });
  }

  /** Payload data for drag-drop operations. */
  private static class DragPayload {
    Object item;
    String sourceSlotType; // Equipment slot type if dragging from equipment
    boolean isEquipmentSlot;
    int sourceInventoryIndex; // Inventory index if dragging from inventory

    DragPayload(
        Object item, String sourceSlotType, boolean isEquipmentSlot, int sourceInventoryIndex) {
      this.item = item;
      this.sourceSlotType = sourceSlotType;
      this.isEquipmentSlot = isEquipmentSlot;
      this.sourceInventoryIndex = sourceInventoryIndex;
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

  public void dispose() {
    // Don't dispose slotStyle - it's shared across all HUDs
    if (backgroundTexture != null) {
      backgroundTexture.dispose();
    }
  }
}
