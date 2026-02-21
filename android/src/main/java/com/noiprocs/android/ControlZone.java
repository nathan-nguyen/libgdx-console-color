package com.noiprocs.android;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.noiprocs.resources.UIConfig;

/**
 * Defines all touch control zones and their screen positions. Positions are scaled based on actual
 * virtual screen dimensions. LibGDX uses bottom-left origin: (0,0) is bottom-left.
 */
public enum ControlZone {
  // Joystick (bottom left)
  JOYSTICK('\0'),

  // Action buttons (bottom right, circular)
  ACTION_SPACE(' '),
  ACTION_FIRE('f'),
  ACTION_TOGGLE('t');

  private Object shape; // Circle (calculated dynamically)
  private final char command;

  ControlZone(char command) {
    this.command = command;
  }

  /**
   * Initialize all control zone positions based on actual virtual screen dimensions. Must be called
   * before using any control zones.
   *
   * @param virtualWidth Actual virtual screen width
   * @param virtualHeight Actual virtual screen height
   */
  public static void initializePositions(float virtualWidth, float virtualHeight) {
    // Calculate scale factors based on base dimensions from UIConfig
    float scaleX = virtualWidth / UIConfig.BASE_VIRTUAL_WIDTH;
    float scaleY = virtualHeight / UIConfig.BASE_VIRTUAL_HEIGHT;

    // Joystick (bottom left)
    JOYSTICK.shape = new Circle(100, 100, 80f * Math.min(scaleX, scaleY));

    // Action buttons (bottom right, circular)
    float actionScale = Math.min(scaleX, scaleY);
    ACTION_SPACE.shape = new Circle(virtualWidth - 100, 75 * scaleY, 32 * actionScale);
    ACTION_FIRE.shape = new Circle(virtualWidth - 45, 115 * scaleY, 28 * actionScale);
    ACTION_TOGGLE.shape = new Circle(virtualWidth - 45, 35 * scaleY, 28 * actionScale);
  }

  /**
   * Check if a point in virtual screen coordinates is within this zone.
   *
   * @param x Virtual screen X coordinate
   * @param y Virtual screen Y coordinate
   * @return true if the point is within this zone
   */
  public boolean contains(float x, float y) {
    if (shape instanceof Circle) {
      Circle circle = (Circle) shape;
      float dx = x - circle.x;
      float dy = y - circle.y;
      return (dx * dx + dy * dy) <= (circle.radius * circle.radius);
    }
    return false;
  }

  /** Get the command character associated with this zone. */
  public char getCommand() {
    return command;
  }

  /** Get the shape for rendering purposes. */
  public Object getShape() {
    return shape;
  }

  /** Check if this is the joystick zone. */
  public boolean isJoystick() {
    return this == JOYSTICK;
  }

  /**
   * Get joystick direction from touch position. Returns the primary direction command based on
   * angle from center.
   *
   * @param x Touch X coordinate
   * @param y Touch Y coordinate
   * @return Direction character ('w', 'a', 's', 'd') or '\0' if not in dead zone
   */
  public char getJoystickDirection(float x, float y) {
    if (!isJoystick() || !(shape instanceof Circle)) {
      return '\0';
    }

    Circle circle = (Circle) shape;
    float dx = x - circle.x;
    float dy = y - circle.y;
    float distance = (float) Math.sqrt(dx * dx + dy * dy);

    // Dead zone in the center (20% of radius)
    if (distance < circle.radius * 0.2f) {
      return '\0';
    }

    // Calculate angle in degrees (0 = right, 90 = up, 180 = left, 270 = down)
    double angle = Math.toDegrees(Math.atan2(dy, dx));
    if (angle < 0) angle += 360;

    // Convert angle to direction (45-degree sectors)
    if (angle >= 45 && angle < 135) {
      return 'w'; // Up
    } else if (angle >= 135 && angle < 225) {
      return 'a'; // Left
    } else if (angle >= 225 && angle < 315) {
      return 's'; // Down
    } else {
      return 'd'; // Right
    }
  }

  /**
   * Get joystick offset vector (normalized) from touch position.
   *
   * @param x Touch X coordinate
   * @param y Touch Y coordinate
   * @return Vector2 with normalized offset, or zero vector if not applicable
   */
  public Vector2 getJoystickOffset(float x, float y) {
    if (!isJoystick() || !(shape instanceof Circle)) {
      return new Vector2();
    }

    Circle circle = (Circle) shape;
    float dx = x - circle.x;
    float dy = y - circle.y;
    float distance = (float) Math.sqrt(dx * dx + dy * dy);

    // Dead zone
    if (distance < circle.radius * 0.2f) {
      return new Vector2();
    }

    // Clamp to circle radius and normalize
    float clampedDistance = Math.min(distance, circle.radius);
    float normalizedX = (dx / distance) * (clampedDistance / circle.radius);
    float normalizedY = (dy / distance) * (clampedDistance / circle.radius);

    return new Vector2(normalizedX, normalizedY);
  }

  /** Check if this is a HUD navigation zone. */
  public boolean isHudControl() {
    return name().startsWith("HUD_");
  }

  /** Check if this is a game control zone (not HUD). */
  public boolean isGameControl() {
    return !isHudControl();
  }

  /**
   * Find the zone containing the given point, prioritizing HUD controls if requested.
   *
   * @param x Virtual screen X coordinate
   * @param y Virtual screen Y coordinate
   * @param hudMode If true, only check HUD controls; if false, only check game controls
   * @return The zone containing the point, or null if none found
   */
  public static ControlZone findZone(float x, float y, boolean hudMode) {
    for (ControlZone zone : values()) {
      if (hudMode && !zone.isHudControl()) {
        continue;
      }
      if (!hudMode && zone.isHudControl()) {
        continue;
      }
      if (zone.contains(x, y)) {
        return zone;
      }
    }
    return null;
  }

  /** Get the center position of this zone for rendering. */
  public Vector2 getCenter() {
    if (shape instanceof Circle) {
      Circle circle = (Circle) shape;
      return new Vector2(circle.x, circle.y);
    }
    return new Vector2();
  }
}
