package com.noiprocs.android;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import java.util.Set;

/**
 * Renders virtual touch controls using ShapeRenderer and SpriteBatch.
 * Provides visual feedback for active/pressed controls.
 */
public class VirtualControlRenderer {
  private final ShapeRenderer shapeRenderer;

  // Colors for different control states
  private static final Color COLOR_INACTIVE = new Color(1f, 1f, 1f, 0.2f);
  private static final Color COLOR_ACTIVE = new Color(1f, 1f, 1f, 0.5f);
  private static final Color COLOR_DPAD = new Color(0.3f, 0.6f, 1f, 0.3f);
  private static final Color COLOR_DPAD_ACTIVE = new Color(0.3f, 0.6f, 1f, 0.6f);
  private static final Color COLOR_ACTION = new Color(1f, 0.3f, 0.3f, 0.3f);
  private static final Color COLOR_ACTION_ACTIVE = new Color(1f, 0.3f, 0.3f, 0.6f);
  private static final Color COLOR_QUICK = new Color(0.3f, 1f, 0.3f, 0.3f);
  private static final Color COLOR_QUICK_ACTIVE = new Color(0.3f, 1f, 0.3f, 0.6f);
  private static final Color COLOR_HUD = new Color(1f, 1f, 0.3f, 0.3f);
  private static final Color COLOR_HUD_ACTIVE = new Color(1f, 1f, 0.3f, 0.6f);

  public VirtualControlRenderer() {
    this.shapeRenderer = new ShapeRenderer();
  }

  /**
   * Render game controls (D-pad, action buttons, quick actions).
   *
   * @param activeZones Set of currently active zones
   * @param batch SpriteBatch for rendering text labels
   * @param font Font for rendering labels
   */
  public void renderGameControls(Set<ControlZone> activeZones, SpriteBatch batch, BitmapFont font) {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    // Render D-pad
    renderZone(ControlZone.DPAD_UP, activeZones.contains(ControlZone.DPAD_UP), COLOR_DPAD, COLOR_DPAD_ACTIVE);
    renderZone(ControlZone.DPAD_DOWN, activeZones.contains(ControlZone.DPAD_DOWN), COLOR_DPAD, COLOR_DPAD_ACTIVE);
    renderZone(ControlZone.DPAD_LEFT, activeZones.contains(ControlZone.DPAD_LEFT), COLOR_DPAD, COLOR_DPAD_ACTIVE);
    renderZone(ControlZone.DPAD_RIGHT, activeZones.contains(ControlZone.DPAD_RIGHT), COLOR_DPAD, COLOR_DPAD_ACTIVE);

    // Render action buttons
    renderZone(ControlZone.ACTION_SPACE, activeZones.contains(ControlZone.ACTION_SPACE), COLOR_ACTION, COLOR_ACTION_ACTIVE);
    renderZone(ControlZone.ACTION_FIRE, activeZones.contains(ControlZone.ACTION_FIRE), COLOR_ACTION, COLOR_ACTION_ACTIVE);
    renderZone(ControlZone.ACTION_TOGGLE, activeZones.contains(ControlZone.ACTION_TOGGLE), COLOR_ACTION, COLOR_ACTION_ACTIVE);

    // Render quick actions
    renderZone(ControlZone.QUICK_EQUIPMENT, activeZones.contains(ControlZone.QUICK_EQUIPMENT), COLOR_QUICK, COLOR_QUICK_ACTIVE);
    renderZone(ControlZone.QUICK_SLOT_1, activeZones.contains(ControlZone.QUICK_SLOT_1), COLOR_QUICK, COLOR_QUICK_ACTIVE);
    renderZone(ControlZone.QUICK_SLOT_2, activeZones.contains(ControlZone.QUICK_SLOT_2), COLOR_QUICK, COLOR_QUICK_ACTIVE);
    renderZone(ControlZone.QUICK_SLOT_3, activeZones.contains(ControlZone.QUICK_SLOT_3), COLOR_QUICK, COLOR_QUICK_ACTIVE);
    renderZone(ControlZone.QUICK_SLOT_4, activeZones.contains(ControlZone.QUICK_SLOT_4), COLOR_QUICK, COLOR_QUICK_ACTIVE);

    shapeRenderer.end();

    // Render labels
    batch.begin();
    renderLabel(batch, font, "W", ControlZone.DPAD_UP);
    renderLabel(batch, font, "S", ControlZone.DPAD_DOWN);
    renderLabel(batch, font, "A", ControlZone.DPAD_LEFT);
    renderLabel(batch, font, "D", ControlZone.DPAD_RIGHT);
    renderLabel(batch, font, "SPC", ControlZone.ACTION_SPACE);
    renderLabel(batch, font, "F", ControlZone.ACTION_FIRE);
    renderLabel(batch, font, "T", ControlZone.ACTION_TOGGLE);
    renderLabel(batch, font, "E", ControlZone.QUICK_EQUIPMENT);
    renderLabel(batch, font, "1", ControlZone.QUICK_SLOT_1);
    renderLabel(batch, font, "2", ControlZone.QUICK_SLOT_2);
    renderLabel(batch, font, "3", ControlZone.QUICK_SLOT_3);
    renderLabel(batch, font, "4", ControlZone.QUICK_SLOT_4);
    batch.end();
  }

  /**
   * Render HUD navigation controls.
   *
   * @param activeZones Set of currently active zones
   * @param batch SpriteBatch for rendering text labels
   * @param font Font for rendering labels
   * @param showEquipmentAction Whether to show the equipment action button (chest interaction)
   */
  public void renderHudControls(
      Set<ControlZone> activeZones, SpriteBatch batch, BitmapFont font, boolean showEquipmentAction) {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

    // Render navigation arrows
    renderZone(ControlZone.HUD_UP, activeZones.contains(ControlZone.HUD_UP), COLOR_HUD, COLOR_HUD_ACTIVE);
    renderZone(ControlZone.HUD_DOWN, activeZones.contains(ControlZone.HUD_DOWN), COLOR_HUD, COLOR_HUD_ACTIVE);
    renderZone(ControlZone.HUD_LEFT, activeZones.contains(ControlZone.HUD_LEFT), COLOR_HUD, COLOR_HUD_ACTIVE);
    renderZone(ControlZone.HUD_RIGHT, activeZones.contains(ControlZone.HUD_RIGHT), COLOR_HUD, COLOR_HUD_ACTIVE);

    // Render action buttons
    renderZone(ControlZone.HUD_CONFIRM, activeZones.contains(ControlZone.HUD_CONFIRM), COLOR_HUD, COLOR_HUD_ACTIVE);
    renderZone(ControlZone.HUD_CLOSE, activeZones.contains(ControlZone.HUD_CLOSE), COLOR_HUD, COLOR_HUD_ACTIVE);
    renderZone(ControlZone.HUD_TAB, activeZones.contains(ControlZone.HUD_TAB), COLOR_HUD, COLOR_HUD_ACTIVE);

    // Render equipment action button if in chest mode
    if (showEquipmentAction) {
      renderZone(ControlZone.HUD_EQUIPMENT_ACTION, activeZones.contains(ControlZone.HUD_EQUIPMENT_ACTION), COLOR_HUD, COLOR_HUD_ACTIVE);
    }

    shapeRenderer.end();

    // Render labels
    batch.begin();
    renderLabel(batch, font, "^", ControlZone.HUD_UP);
    renderLabel(batch, font, "v", ControlZone.HUD_DOWN);
    renderLabel(batch, font, "<", ControlZone.HUD_LEFT);
    renderLabel(batch, font, ">", ControlZone.HUD_RIGHT);
    renderLabel(batch, font, "OK", ControlZone.HUD_CONFIRM);
    renderLabel(batch, font, "X", ControlZone.HUD_CLOSE);
    renderLabel(batch, font, "TAB", ControlZone.HUD_TAB);
    if (showEquipmentAction) {
      renderLabel(batch, font, "E", ControlZone.HUD_EQUIPMENT_ACTION);
    }
    batch.end();
  }

  /**
   * Render a single control zone.
   */
  private void renderZone(ControlZone zone, boolean active, Color inactiveColor, Color activeColor) {
    Color color = active ? activeColor : inactiveColor;
    shapeRenderer.setColor(color);

    Object shape = zone.getShape();
    if (shape instanceof Rectangle) {
      Rectangle rect = (Rectangle) shape;
      shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
    } else if (shape instanceof Circle) {
      Circle circle = (Circle) shape;
      shapeRenderer.circle(circle.x, circle.y, circle.radius, 32);
    }
  }

  /**
   * Render a text label centered on a control zone.
   */
  private void renderLabel(SpriteBatch batch, BitmapFont font, String label, ControlZone zone) {
    com.badlogic.gdx.math.Vector2 center = zone.getCenter();
    com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(font, label);
    float x = center.x - layout.width / 2;
    float y = center.y + layout.height / 2;

    // Draw shadow for better visibility
    font.setColor(0, 0, 0, 0.8f);
    font.draw(batch, label, x + 1, y - 1);

    // Draw label
    font.setColor(1, 1, 1, 1);
    font.draw(batch, label, x, y);
  }

  /**
   * Set the projection matrix for the shape renderer.
   */
  public void setProjectionMatrix(com.badlogic.gdx.math.Matrix4 matrix) {
    shapeRenderer.setProjectionMatrix(matrix);
  }

  /**
   * Dispose of resources.
   */
  public void dispose() {
    shapeRenderer.dispose();
  }
}
