package com.noiprocs.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import java.util.Set;

/**
 * Renders virtual touch controls using ShapeRenderer and SpriteBatch. Provides visual feedback for
 * active/pressed controls.
 */
public class VirtualControlRenderer {
  private final ShapeRenderer shapeRenderer;

  // Colors for different control states (outline only, transparent backgrounds)
  private static final Color COLOR_DPAD = new Color(0.3f, 0.6f, 1f, 0.5f);
  private static final Color COLOR_DPAD_ACTIVE = new Color(0.3f, 0.6f, 1f, 1.0f);
  private static final Color COLOR_ACTION = new Color(1f, 0.3f, 0.3f, 0.5f);
  private static final Color COLOR_ACTION_ACTIVE = new Color(1f, 0.3f, 0.3f, 1.0f);

  public VirtualControlRenderer() {
    this.shapeRenderer = new ShapeRenderer();
  }

  /**
   * Render game controls (joystick, action buttons, quick actions).
   *
   * @param activeZones Set of currently active zones
   * @param batch SpriteBatch for rendering text labels
   * @param font Font for rendering labels
   * @param touchState Touch state for joystick position
   */
  public void renderGameControls(
      Set<ControlZone> activeZones, SpriteBatch batch, BitmapFont font, TouchState touchState) {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glLineWidth(3.0f); // Make borders bolder

    // Render joystick
    renderJoystick(touchState);

    // Render action buttons
    renderZone(
        ControlZone.ACTION_SPACE,
        activeZones.contains(ControlZone.ACTION_SPACE),
        COLOR_ACTION,
        COLOR_ACTION_ACTIVE);
    renderZone(
        ControlZone.ACTION_FIRE,
        activeZones.contains(ControlZone.ACTION_FIRE),
        COLOR_ACTION,
        COLOR_ACTION_ACTIVE);
    renderZone(
        ControlZone.ACTION_TOGGLE,
        activeZones.contains(ControlZone.ACTION_TOGGLE),
        COLOR_ACTION,
        COLOR_ACTION_ACTIVE);

    shapeRenderer.end();

    // Render labels
    batch.begin();
    renderLabel(batch, font, "Interact", ControlZone.ACTION_SPACE);
    renderLabel(batch, font, "Attack", ControlZone.ACTION_FIRE);
    renderLabel(batch, font, "Item", ControlZone.ACTION_TOGGLE);
    batch.end();
  }

  /** Render a single control zone. */
  private void renderZone(
      ControlZone zone, boolean active, Color inactiveColor, Color activeColor) {
    Color color = active ? activeColor : inactiveColor;
    shapeRenderer.setColor(color);

    Circle circle = (Circle) zone.getShape();
    shapeRenderer.circle(circle.x, circle.y, circle.radius, 32);
  }

  /** Render joystick (outer circle + inner knob). */
  private void renderJoystick(TouchState touchState) {
    Circle joystickCircle = (Circle) ControlZone.JOYSTICK.getShape();

    // Draw outer circle (boundary)
    shapeRenderer.setColor(COLOR_DPAD);
    shapeRenderer.circle(joystickCircle.x, joystickCircle.y, joystickCircle.radius, 32);

    // Draw inner knob based on touch offset
    Vector2 offset = touchState.getJoystickOffset();
    float knobX = joystickCircle.x + offset.x * joystickCircle.radius * 0.6f;
    float knobY = joystickCircle.y + offset.y * joystickCircle.radius * 0.6f;
    float knobRadius = joystickCircle.radius * 0.25f;

    Color knobColor = touchState.isJoystickActive() ? COLOR_DPAD_ACTIVE : COLOR_DPAD;
    shapeRenderer.setColor(knobColor);
    shapeRenderer.circle(knobX, knobY, knobRadius, 24);
  }

  /** Render a text label centered on a control zone. */
  private void renderLabel(SpriteBatch batch, BitmapFont font, String label, ControlZone zone) {
    Vector2 center = zone.getCenter();
    GlyphLayout layout = new GlyphLayout(font, label);
    float x = center.x - layout.width / 2;
    float y = center.y + layout.height / 2;

    // Draw shadow for better visibility
    font.setColor(0, 0, 0, 0.8f);
    font.draw(batch, label, x + 1, y - 1);

    // Draw label
    font.setColor(1, 1, 1, 1);
    font.draw(batch, label, x, y);
  }

  /** Set the projection matrix for the shape renderer. */
  public void setProjectionMatrix(Matrix4 matrix) {
    shapeRenderer.setProjectionMatrix(matrix);
  }

  /** Dispose of resources. */
  public void dispose() {
    shapeRenderer.dispose();
  }
}
