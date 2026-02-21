package com.noiprocs.ui.libgdx.hud.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.noiprocs.resources.ItemTextureManager;

/**
 * Reusable draggable slot widget for inventory items, equipment slots, and crafting materials.
 * Displays item using character-based icon with color coding and quantity.
 */
public class ItemSlotWidget extends Table {

  private final ItemSlotStyle style;
  private final BitmapFont font;
  private final ItemTextureManager itemTextureManager;

  private Object item; // Item object (from console-color-core dependency)
  private Class<?> itemClass; // Explicit item class for texture lookup (used when item is a String)
  private int quantity;
  private final boolean showItemName; // Whether to show full item name instead of icon character
  private boolean isHotbarSlot; // Whether this is a hotbar slot (first 4 inventory slots)
  private boolean isSelected; // Whether this is the currently selected slot

  private TextureRegion emptySlotTexture; // Shown blurry when slot is empty
  private Image iconImage;
  private Label iconLabel;
  private Label quantityLabel;
  private boolean isHovered;
  private boolean isDragging;

  /**
   * Creates an empty item slot widget.
   *
   * @param style Visual style for the slot
   * @param font Font for rendering text
   * @param showItemName Whether to show full item name below the slot
   */
  public ItemSlotWidget(
      ItemSlotStyle style,
      BitmapFont font,
      boolean showItemName,
      ItemTextureManager itemTextureManager) {
    this.style = style;
    this.font = font;
    this.itemTextureManager = itemTextureManager;
    this.showItemName = showItemName;
    this.item = null;
    this.quantity = 0;
    this.isHovered = false;
    this.isDragging = false;

    setupUI();
  }

  private void setupUI() {
    // Set fixed size for slot (48x48 minimum for touch targets)
    this.setSize(52, 52);
    this.pad(2);
    this.setTouchable(Touchable.enabled); // Enable touch for entire slot

    // Create icon image (shown when a texture is available for the item)
    iconImage = new Image();
    iconImage.setScaling(Scaling.fit);
    iconImage.setVisible(false);

    // Create icon/name label (shown when no texture or in name mode)
    Label.LabelStyle iconStyle = new Label.LabelStyle();
    iconStyle.font = font;
    iconStyle.fontColor = Color.WHITE;
    iconLabel = new Label("", iconStyle);
    iconLabel.setAlignment(Align.center);
    iconLabel.setFontScale(showItemName ? 0.5f : 1.5f);
    if (showItemName) {
      iconLabel.setWrap(true);
    }

    // Create quantity label (bottom-right corner)
    Label.LabelStyle qtyStyle = new Label.LabelStyle();
    qtyStyle.font = font;
    qtyStyle.fontColor = Color.WHITE;
    quantityLabel = new Label("", qtyStyle);
    quantityLabel.setFontScale(0.7f);

    // Quantity overlaid in bottom-right corner
    Table quantityOverlay = new Table();
    quantityOverlay.add(quantityLabel).expand().right().bottom().pad(1);

    // Single Stack: icon fills the whole slot, quantity overlays on top
    Stack contentStack = new Stack();
    contentStack.setTouchable(Touchable.enabled);
    contentStack.add(iconImage);
    contentStack.add(iconLabel);
    contentStack.add(quantityOverlay);

    this.add(contentStack).expand().fill();

    // Set initial background
    updateBackground();
  }

  /**
   * Sets the item displayed in this slot.
   *
   * @param item Item object or null for empty slot
   * @param quantity Item quantity (for stackable items)
   */
  public void setItem(Object item, int quantity) {
    this.item = item;
    this.itemClass = null;
    this.quantity = quantity;
    updateDisplay();
  }

  /**
   * Sets the item displayed in this slot using an explicit class for texture lookup.
   *
   * @param itemClass The item's class (used for texture lookup)
   * @param displayName The name to display in the slot
   * @param quantity Item quantity
   */
  public void setItem(Class<?> itemClass, String displayName, int quantity) {
    this.item = displayName;
    this.itemClass = itemClass;
    this.quantity = quantity;
    updateDisplay();
  }

  /**
   * Gets the item currently in this slot.
   *
   * @return Item object or null if empty
   */
  public Object getItem() {
    return item;
  }

  /**
   * Sets whether this is a hotbar slot (first 4 inventory slots).
   *
   * @param isHotbarSlot true if this is a hotbar slot
   */
  public void setHotbarSlot(boolean isHotbarSlot) {
    this.isHotbarSlot = isHotbarSlot;
    updateBackground();
  }

  /**
   * Sets whether this is the currently selected slot.
   *
   * @param isSelected true if this is the selected slot
   */
  public void setSelected(boolean isSelected) {
    this.isSelected = isSelected;
    updateBackground();
  }

  /**
   * Checks if this slot is empty.
   *
   * @return true if no item in slot
   */
  public boolean isEmpty() {
    return item == null;
  }

  /** Clears the item from this slot. */
  public void clear() {
    setItem(null, 0);
  }

  /**
   * Sets a placeholder texture displayed blurrily when the slot is empty.
   *
   * @param texture Texture region to show, or null to show nothing when empty
   */
  public void setEmptySlotTexture(TextureRegion texture) {
    this.emptySlotTexture = texture;
    if (item == null) updateDisplay();
  }

  /**
   * Sets the hover state for visual feedback.
   *
   * @param hovered true if mouse/touch is hovering over slot
   */
  public void setHovered(boolean hovered) {
    this.isHovered = hovered;
    updateBackground();
  }

  /**
   * Sets the dragging state for visual feedback.
   *
   * @param dragging true if item is being dragged
   */
  public void setDragging(boolean dragging) {
    this.isDragging = dragging;
    updateBackground();
  }

  private void updateDisplay() {
    if (item == null) {
      if (emptySlotTexture != null) {
        iconImage.setDrawable(new TextureRegionDrawable(emptySlotTexture));
        iconImage.setColor(1, 1, 1, 0.35f);
        iconImage.setVisible(true);
      } else {
        iconImage.setVisible(false);
      }
      iconLabel.setText("");
      quantityLabel.setText("");
    } else {
      ItemIconRenderer.ItemIcon icon = ItemIconRenderer.renderIcon(item);
      if (icon != null) {
        TextureRegion region =
            itemClass != null
                ? itemTextureManager.getTextureByClass(itemClass)
                : itemTextureManager.getTexture(item);
        if (region != null) {
          iconImage.setDrawable(new TextureRegionDrawable(region));
          iconImage.setColor(1, 1, 1, 1f);
          iconImage.setVisible(true);
          iconLabel.setVisible(false);
        } else if (showItemName) {
          iconImage.setVisible(false);
          iconLabel.setVisible(true);
          iconLabel.setText(icon.displayName);
          iconLabel.setFontScale(0.5f);
          iconLabel.setColor(icon.color);
        } else {
          iconImage.setVisible(false);
          iconLabel.setVisible(true);
          iconLabel.setText(String.valueOf(icon.character));
          iconLabel.setFontScale(1.5f);
          iconLabel.setColor(icon.color);
        }
      } else {
        iconImage.setVisible(false);
        iconLabel.setVisible(true);
        iconLabel.setText(showItemName ? item.toString() : "?");
        iconLabel.setFontScale(showItemName ? 0.5f : 1.5f);
        iconLabel.setColor(Color.WHITE);
      }

      quantityLabel.setText(quantity > 1 ? String.valueOf(quantity) : "");
    }

    updateBackground();
  }

  private void updateBackground() {
    Drawable background;

    if (isDragging) {
      background = style.draggingBackground;
    } else if (isSelected) {
      // Selected slot gets bright highlight
      background = style.selectedBackground;
    } else if (isHovered) {
      background = style.hoverBackground;
    } else if (isHotbarSlot) {
      // Hotbar slots use special background
      background = item != null ? style.hotbarFilledBackground : style.hotbarEmptyBackground;
    } else if (item != null) {
      background = style.filledBackground;
    } else {
      background = style.emptyBackground;
    }

    this.setBackground(background);
  }

  /**
   * Creates a drag actor for the given item: an Image if a texture is loaded, otherwise a Label.
   */
  public static Actor createDragActor(
      Object item, BitmapFont font, ItemTextureManager itemTextureManager) {
    ItemIconRenderer.ItemIcon icon = ItemIconRenderer.renderIcon(item);
    if (icon != null) {
      TextureRegion region = itemTextureManager.getTexture(item);
      if (region != null) {
        Image dragImage = new Image(new TextureRegionDrawable(region));
        dragImage.setScaling(Scaling.fit);
        dragImage.setSize(52, 52);
        return dragImage;
      }
    }
    Label.LabelStyle dragStyle = new Label.LabelStyle();
    dragStyle.font = font;
    dragStyle.fontColor = Color.WHITE;
    Label dragLabel = new Label(String.valueOf(item), dragStyle);
    dragLabel.setFontScale(1.5f);
    return dragLabel;
  }
}
