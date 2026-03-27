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
import com.noiprocs.resources.RenderResources;

public class ItemSlotWidget extends Table {
  public static final int DIMENSION = 36;
  public static final int ITEM_NAME_HEIGHT = 21;
  public static final float ITEM_NAME_FONT_SCALE = 0.45f;
  private final ItemSlotStyle style;

  private Object item;
  private Class<?> itemClass;
  private int quantity;
  private final boolean showItemName;
  private boolean isHotbarSlot;
  private boolean isSelected;

  private TextureRegion emptySlotTexture;
  private Image iconImage;
  private Label iconLabel;
  private Label quantityLabel;
  private boolean isHovered;
  private boolean isDragging;

  public ItemSlotWidget(ItemSlotStyle style, boolean showItemName) {
    this.style = style;
    this.showItemName = showItemName;
    this.item = null;
    this.quantity = 0;
    this.isHovered = false;
    this.isDragging = false;

    setupUI();
  }

  private void setupUI() {
    BitmapFont font = RenderResources.get().getPanelFont();
    this.setSize(DIMENSION, DIMENSION);
    this.pad(2);
    this.setTouchable(Touchable.enabled);

    iconImage = new Image();
    iconImage.setScaling(Scaling.fit);
    iconImage.setVisible(false);

    Label.LabelStyle iconStyle = new Label.LabelStyle(font, Color.WHITE);
    iconLabel = new Label("", iconStyle);
    iconLabel.setAlignment(Align.center);
    iconLabel.setFontScale(showItemName ? 0.5f : 1.5f);
    if (showItemName) {
      iconLabel.setWrap(true);
    }

    Label.LabelStyle qtyStyle = new Label.LabelStyle(font, Color.WHITE);
    quantityLabel = new Label("", qtyStyle);
    quantityLabel.setFontScale(font.getData().scaleX * 0.45f);

    Table quantityOverlay = new Table();
    quantityOverlay.add(quantityLabel).expand().right().bottom().pad(1);

    Stack contentStack = new Stack();
    contentStack.setTouchable(Touchable.enabled);
    contentStack.add(iconImage);
    contentStack.add(iconLabel);
    contentStack.add(quantityOverlay);

    this.add(contentStack).expand().fill();

    updateBackground();
  }

  public void setItem(Object item, int quantity) {
    this.item = item;
    this.itemClass = null;
    this.quantity = quantity;
    updateDisplay();
  }

  public void setItem(Class<?> itemClass, String displayName, int quantity) {
    this.item = displayName;
    this.itemClass = itemClass;
    this.quantity = quantity;
    updateDisplay();
  }

  public Object getItem() {
    return item;
  }

  public void setHotbarSlot(boolean isHotbarSlot) {
    this.isHotbarSlot = isHotbarSlot;
    updateBackground();
  }

  public void setSelected(boolean isSelected) {
    this.isSelected = isSelected;
    updateBackground();
  }

  public boolean isEmpty() {
    return item == null;
  }

  public void clear() {
    setItem(null, 0);
  }

  public void setEmptySlotTexture(TextureRegion texture) {
    this.emptySlotTexture = texture;
    if (item == null) updateDisplay();
  }

  public void setHovered(boolean hovered) {
    this.isHovered = hovered;
    updateBackground();
  }

  public void setDragging(boolean dragging) {
    this.isDragging = dragging;
    updateBackground();
  }

  private void updateDisplay() {
    ItemTextureManager itemTextureManager = RenderResources.get().getItemTextureManager();
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
      background = style.selectedBackground;
    } else if (isHovered) {
      background = style.hoverBackground;
    } else if (isHotbarSlot) {
      background = item != null ? style.hotbarFilledBackground : style.hotbarEmptyBackground;
    } else if (item != null) {
      background = style.filledBackground;
    } else {
      background = style.emptyBackground;
    }

    this.setBackground(background);
  }

  public static Actor createDragActor(
      Object item, BitmapFont font, ItemTextureManager itemTextureManager) {
    ItemIconRenderer.ItemIcon icon = ItemIconRenderer.renderIcon(item);
    if (icon != null) {
      TextureRegion region = itemTextureManager.getTexture(item);
      if (region != null) {
        Image dragImage = new Image(new TextureRegionDrawable(region));
        dragImage.setScaling(Scaling.fit);
        dragImage.setSize(DIMENSION, DIMENSION);
        return dragImage;
      }
    }
    Label.LabelStyle dragStyle = new Label.LabelStyle(font, Color.WHITE);
    Label dragLabel = new Label(String.valueOf(item), dragStyle);
    dragLabel.setFontScale(1.5f);
    return dragLabel;
  }

  public static Label generateNameLabel(Label.LabelStyle nameLabelStyle) {
    Label nameLabel = new Label("", nameLabelStyle);
    nameLabel.setFontScale(ITEM_NAME_FONT_SCALE);
    nameLabel.setAlignment(Align.center);
    nameLabel.setWrap(true);
    return nameLabel;
  }
}
