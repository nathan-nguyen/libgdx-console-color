package com.noiprocs.ui.libgdx.hud.widget;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

/**
 * Reusable draggable slot widget for inventory items, equipment slots, and crafting materials.
 * Displays item using character-based icon with color coding and quantity.
 */
public class ItemSlotWidget extends Table {

  private final ItemSlotStyle style;
  private final BitmapFont font;

  private Object item; // Item object (from console-color-core dependency)
  private int quantity;
  private final boolean showItemName; // Whether to show full item name instead of icon character
  private boolean isHotbarSlot; // Whether this is a hotbar slot (first 4 inventory slots)
  private boolean isSelected; // Whether this is the currently selected slot

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
  public ItemSlotWidget(ItemSlotStyle style, BitmapFont font, boolean showItemName) {
    this.style = style;
    this.font = font;
    // Equipment slot type (e.g., "HELMET", "CHEST") or null for inventory
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

    // Create icon/name label (center)
    Label.LabelStyle iconStyle = new Label.LabelStyle();
    iconStyle.font = font;
    iconStyle.fontColor = Color.WHITE;
    iconLabel = new Label("", iconStyle);
    iconLabel.setFontScale(showItemName ? 0.5f : 1.5f); // Smaller for names, larger for icons

    // Create quantity label (bottom-right corner)
    Label.LabelStyle qtyStyle = new Label.LabelStyle();
    qtyStyle.font = font;
    qtyStyle.fontColor = Color.WHITE;
    quantityLabel = new Label("", qtyStyle);
    quantityLabel.setFontScale(0.7f); // Smaller quantity text

    // Layout: Stack icon/name in center, quantity in bottom-right
    Table contentTable = new Table();
    contentTable.setFillParent(true); // Fill entire slot area
    contentTable.setTouchable(Touchable.enabled); // Make content table touchable
    contentTable.add(iconLabel).expand().center();
    contentTable.row();
    contentTable.add(quantityLabel).right().bottom();

    this.add(contentTable).expand().fill();

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
      iconLabel.setText("");
      quantityLabel.setText("");
    } else {
      // Render item using full name or icon based on showItemName setting
      ItemIconRenderer.ItemIcon icon = ItemIconRenderer.renderIcon(item);
      if (icon != null) {
        if (showItemName) {
          // Show full item name instead of single character
          iconLabel.setText(icon.displayName);
          iconLabel.setFontScale(0.5f); // Smaller font for full name
        } else {
          // Show single character icon
          iconLabel.setText(String.valueOf(icon.character));
          iconLabel.setFontScale(1.5f); // Larger icon
        }
        iconLabel.setColor(icon.color);
      } else {
        iconLabel.setText(showItemName ? item.toString() : "?");
        iconLabel.setFontScale(showItemName ? 0.5f : 1.5f);
        iconLabel.setColor(Color.WHITE);
      }

      // Show quantity if > 1
      if (quantity > 1) {
        quantityLabel.setText(String.valueOf(quantity));
      } else {
        quantityLabel.setText("");
      }
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
}
