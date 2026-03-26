package com.noiprocs.android;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.noiprocs.resources.GameResource;
import com.noiprocs.resources.ResourceLoader;
import com.noiprocs.settings.HotbarLocation;
import com.noiprocs.settings.SettingsManager;
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

  // Slot height (48) + pad(2) top/bottom (4) + cell padBottom (10)
  private static final float HOTBAR_HEIGHT = 62f;

  // Colors for joystick states
  private static final Color COLOR_DPAD = new Color(0.3f, 0.6f, 1f, 0.5f);
  private static final Color COLOR_DPAD_ACTIVE = new Color(0.3f, 0.6f, 1f, 1.0f);
  private static final Color COLOR_THROW_AIM = new Color(1f, 0.8f, 0.2f, 0.8f);

  private final SettingsManager settingsManager;

  public VirtualControlRenderer(SettingsManager settingsManager) {
    this.settingsManager = settingsManager;
    this.shapeRenderer = new ShapeRenderer();

    attackIcon = ResourceLoader.loadTexture(GameResource.ICON_ATTACK_BUTTON);
    interactIcon = ResourceLoader.loadTexture(GameResource.ICON_INTERACT_BUTTON);
    useItemIcon = ResourceLoader.loadTexture(GameResource.ICON_USE_ITEM_BUTTON);
  }

  /** Render joystick and shape-based controls via ShapeRenderer. */
  public void renderShapes(TouchState touchState) {
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glLineWidth(3.0f);
    renderJoystick(touchState);
    if (touchState.isThrowAimPointerActive()) {
      renderThrowJoystick(touchState);
    }
    shapeRenderer.end();

    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  /** Render button icons into an already-open Batch. */
  public void renderIcons(Batch batch, Set<ControlZone> activeZones, boolean throwAimActive) {
    renderIcon(
        batch,
        interactIcon,
        ControlZone.ACTION_INTERACT,
        activeZones.contains(ControlZone.ACTION_INTERACT));
    renderIcon(
        batch,
        attackIcon,
        ControlZone.ACTION_ATTACK,
        activeZones.contains(ControlZone.ACTION_ATTACK));
    if (!throwAimActive) {
      renderIcon(
          batch,
          useItemIcon,
          ControlZone.ACTION_USE_ITEM,
          activeZones.contains(ControlZone.ACTION_USE_ITEM));
    }
  }

  /** Render a texture icon centered on a control zone. */
  private void renderIcon(Batch batch, Texture icon, ControlZone zone, boolean active) {
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
    float yOffset =
        settingsManager.getHotbarLocation() == HotbarLocation.BOTTOM ? HOTBAR_HEIGHT : 0f;
    float centerX = joystickCircle.x;
    float centerY = joystickCircle.y + yOffset;

    // Draw outer circle (boundary)
    shapeRenderer.setColor(COLOR_DPAD);
    shapeRenderer.circle(centerX, centerY, joystickCircle.radius, 32);

    // Draw inner knob based on touch offset
    Vector2 offset = touchState.getJoystickOffset();
    float knobX = centerX + offset.x * joystickCircle.radius * 0.6f;
    float knobY = centerY + offset.y * joystickCircle.radius * 0.6f;
    float knobRadius = joystickCircle.radius * 0.25f;

    Color knobColor = touchState.isJoystickActive() ? COLOR_DPAD_ACTIVE : COLOR_DPAD;
    shapeRenderer.setColor(knobColor);
    shapeRenderer.circle(knobX, knobY, knobRadius, 24);
  }

  /** Render throw aim joystick centered on the ACTION_TOGGLE button. */
  private void renderThrowJoystick(TouchState touchState) {
    float radius = ControlZone.getJoystickRadius();
    Vector2 center = ControlZone.ACTION_USE_ITEM.getCenter();

    shapeRenderer.setColor(COLOR_THROW_AIM);
    shapeRenderer.circle(center.x, center.y, radius, 32);

    Vector2 offset = touchState.getThrowAimOffset();
    float knobX = center.x + offset.x * radius * 0.6f;
    float knobY = center.y + offset.y * radius * 0.6f;
    shapeRenderer.circle(knobX, knobY, radius * 0.25f, 24);
  }

  /** Set the projection matrix for the shape renderer. */
  public void setProjectionMatrix(Matrix4 matrix) {
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
