package com.noiprocs.ui.libgdx.hud.widget;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Visual styling for item slot widgets. Creates programmatic textures for different slot states.
 */
public class ItemSlotStyle {
  public Drawable emptyBackground;
  public Drawable filledBackground;
  public Drawable hoverBackground;
  public Drawable draggingBackground;
  public Drawable borderTexture;
  public Drawable hotbarEmptyBackground;
  public Drawable hotbarFilledBackground;
  public Drawable selectedBackground;

  /**
   * Creates default item slot style with programmatic textures.
   *
   * @return Default ItemSlotStyle instance
   */
  public static ItemSlotStyle createDefault() {
    ItemSlotStyle style = new ItemSlotStyle();

    Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);

    // Empty slot - dark gray
    pixmap.setColor(0.2f, 0.2f, 0.2f, 1f);
    pixmap.fill();
    style.emptyBackground = new TextureRegionDrawable(new Texture(pixmap));

    // Filled slot - slightly lighter gray
    pixmap.setColor(0.3f, 0.3f, 0.3f, 1f);
    pixmap.fill();
    style.filledBackground = new TextureRegionDrawable(new Texture(pixmap));

    // Hover - blue tint
    pixmap.setColor(0.3f, 0.4f, 0.6f, 1f);
    pixmap.fill();
    style.hoverBackground = new TextureRegionDrawable(new Texture(pixmap));

    // Dragging - yellowish tint
    pixmap.setColor(0.5f, 0.5f, 0.3f, 1f);
    pixmap.fill();
    style.draggingBackground = new TextureRegionDrawable(new Texture(pixmap));

    // Border - light gray
    pixmap.setColor(0.5f, 0.5f, 0.5f, 1f);
    pixmap.fill();
    style.borderTexture = new TextureRegionDrawable(new Texture(pixmap));

    // Hotbar empty - brown/tan tint
    pixmap.setColor(0.3f, 0.25f, 0.2f, 1f);
    pixmap.fill();
    style.hotbarEmptyBackground = new TextureRegionDrawable(new Texture(pixmap));

    // Hotbar filled - lighter brown
    pixmap.setColor(0.4f, 0.35f, 0.25f, 1f);
    pixmap.fill();
    style.hotbarFilledBackground = new TextureRegionDrawable(new Texture(pixmap));

    // Selected slot - bright yellow/gold highlight
    pixmap.setColor(0.8f, 0.7f, 0.2f, 1f);
    pixmap.fill();
    style.selectedBackground = new TextureRegionDrawable(new Texture(pixmap));

    pixmap.dispose();

    return style;
  }

  /** Disposes of all textures used by this style. */
  public void dispose() {
    disposeDrawable(emptyBackground);
    disposeDrawable(filledBackground);
    disposeDrawable(hoverBackground);
    disposeDrawable(draggingBackground);
    disposeDrawable(borderTexture);
    disposeDrawable(hotbarEmptyBackground);
    disposeDrawable(hotbarFilledBackground);
    disposeDrawable(selectedBackground);
  }

  private void disposeDrawable(Drawable drawable) {
    if (drawable instanceof TextureRegionDrawable) {
      ((TextureRegionDrawable) drawable).getRegion().getTexture().dispose();
    }
  }
}
