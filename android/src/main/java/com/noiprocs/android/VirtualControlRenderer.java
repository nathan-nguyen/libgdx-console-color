package com.noiprocs.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.noiprocs.resources.GameResource;
import com.noiprocs.resources.ResourceLoader;
import java.util.Set;

/**
 * Renders virtual touch controls using ShapeRenderer and SpriteBatch. Provides visual feedback for
 * active/pressed controls.
 */
public class VirtualControlRenderer {
  private final ShapeRenderer shapeRenderer;
  private final Texture attackIcon;
  private final Texture interactIcon;
  private final Texture useItemIcon;
  private Matrix4 projectionMatrix;

  // Colors for joystick states
  private static final Color COLOR_DPAD = new Color(0.3f, 0.6f, 1f, 0.5f);
  private static final Color COLOR_DPAD_ACTIVE = new Color(0.3f, 0.6f, 1f, 1.0f);

  public VirtualControlRenderer() {
    this.shapeRenderer = new ShapeRenderer();

    attackIcon = ResourceLoader.loadTexture(GameResource.ICON_ATTACK_BUTTON);
    interactIcon = ResourceLoader.loadTexture(GameResource.ICON_INTERACT_BUTTON);
    useItemIcon = ResourceLoader.loadTexture(GameResource.ICON_USE_ITEM_BUTTON);
  }

  /**
   * Render game controls (joystick, action buttons, quick actions).
   *
   * @param activeZones Set of currently active zones
   * @param batch SpriteBatch for rendering icons
   * @param touchState Touch state for joystick position
   */
  public void renderGameControls(
      Set<ControlZone> activeZones, SpriteBatch batch, TouchState touchState) {
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glLineWidth(3.0f); // Make borders bolder

    // Render joystick
    renderJoystick(touchState);

    shapeRenderer.end();

    // Render button icons
    if (projectionMatrix != null) {
      batch.setProjectionMatrix(projectionMatrix);
    }
    batch.begin();
    renderIcon(
        batch,
        interactIcon,
        ControlZone.ACTION_SPACE,
        activeZones.contains(ControlZone.ACTION_SPACE));
    renderIcon(
        batch, attackIcon, ControlZone.ACTION_FIRE, activeZones.contains(ControlZone.ACTION_FIRE));
    renderIcon(
        batch,
        useItemIcon,
        ControlZone.ACTION_TOGGLE,
        activeZones.contains(ControlZone.ACTION_TOGGLE));
    batch.end();
  }

  /** Render a texture icon centered on a control zone. */
  private void renderIcon(SpriteBatch batch, Texture icon, ControlZone zone, boolean active) {
    Circle circle = (Circle) zone.getShape();
    float size = circle.radius * 2.5f;
    float x = circle.x - size / 2f;
    float y = circle.y - size / 2f;
    batch.setColor(1f, 1f, 1f, active ? 0.5f : 1.0f);
    batch.draw(icon, x, y, size, size);
    batch.setColor(1f, 1f, 1f, 1f);
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

  /** Set the projection matrix for the shape renderer and icon rendering. */
  public void setProjectionMatrix(Matrix4 matrix) {
    this.projectionMatrix = matrix;
    shapeRenderer.setProjectionMatrix(matrix);
  }

  /** Dispose of resources. */
  public void dispose() {
    shapeRenderer.dispose();
    attackIcon.dispose();
    interactIcon.dispose();
    useItemIcon.dispose();
  }
}
