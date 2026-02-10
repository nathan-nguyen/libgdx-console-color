package com.noiprocs.android;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Defines all touch control zones and their screen positions.
 * Coordinates are in virtual screen space (440x690 pixels).
 * LibGDX uses bottom-left origin: (0,0) is bottom-left, (440,690) is top-right.
 */
public enum ControlZone {
  // Joystick (bottom left - replaces D-pad)
  JOYSTICK(new Circle(82.5f, 87.5f, 80f), '\0'),  // Centered in D-pad area

  // D-pad zones (kept for compatibility, but not used in game mode)
  DPAD_UP(new Rectangle(55, 115, 55, 55), 'w'),       // y=115, bottom at 115, top at 170
  DPAD_DOWN(new Rectangle(55, 5, 55, 55), 's'),       // y=5, bottom at 5 (TOUCHING border)
  DPAD_LEFT(new Rectangle(5, 60, 55, 55), 'a'),       // y=60, bottom at 60, top at 115
  DPAD_RIGHT(new Rectangle(105, 60, 55, 55), 'd'),     // y=60, bottom at 60, top at 115

  // Action buttons (bottom right, circular)
  ACTION_SPACE(new Circle(340, 75, 32), ' '),     // Primary action - center
  ACTION_FIRE(new Circle(395, 115, 28), 'f'),     // Fire - upper right
  ACTION_TOGGLE(new Circle(395, 35, 28), 't'),    // Toggle - lower right

  // Quick actions (top right corner - E button, touching top map border)
  QUICK_EQUIPMENT(new Rectangle(390, 600, 45, 45), 'e'),  // y=600, top at 645 (below border at ~650)

  // Inventory slots (top left, horizontal row, touching top map border)
  QUICK_SLOT_1(new Rectangle(5, 600, 45, 45), '1'),       // y=600, top at 645 (below border at ~650)
  QUICK_SLOT_2(new Rectangle(55, 600, 45, 45), '2'),      // y=600, top at 645 (below border at ~650)
  QUICK_SLOT_3(new Rectangle(105, 600, 45, 45), '3'),     // y=600, top at 645 (below border at ~650)
  QUICK_SLOT_4(new Rectangle(155, 600, 45, 45), '4'),     // y=600, top at 645 (below border at ~650)

  // HUD navigation (context-aware, shown when HUD is active)
  // Positioned at bottom, touching bottom border from above (same as DPAD)
  HUD_UP(new Rectangle(55, 115, 55, 55), 'w'),         // y=115, bottom at 115, top at 170
  HUD_DOWN(new Rectangle(55, 5, 55, 55), 's'),         // y=5, bottom at 5 (TOUCHING border)
  HUD_LEFT(new Rectangle(5, 60, 55, 55), 'a'),         // y=60, bottom at 60, top at 115
  HUD_RIGHT(new Rectangle(105, 60, 55, 55), 'd'),       // y=60, bottom at 60, top at 115

  // Action buttons positioned to match game controls
  HUD_TAB(new Circle(340, 75, 32), '\t'),              // Same position as ACTION_SPACE
  HUD_CONFIRM(new Circle(395, 115, 28), '\n'),         // Same position as ACTION_FIRE (F)
  HUD_CLOSE(new Rectangle(390, 600, 45, 45), (char) 27), // Same position as QUICK_EQUIPMENT (E), y=600, top at 645
  HUD_EQUIPMENT_ACTION(new Rectangle(385, 350, 50, 50), 'e'); // For chest interaction

  private final Object shape; // Either Rectangle or Circle
  private final char command;

  ControlZone(Rectangle rect, char command) {
    this.shape = rect;
    this.command = command;
  }

  ControlZone(Circle circle, char command) {
    this.shape = circle;
    this.command = command;
  }

  /**
   * Check if a point in virtual screen coordinates is within this zone.
   *
   * @param x Virtual screen X coordinate
   * @param y Virtual screen Y coordinate
   * @return true if the point is within this zone
   */
  public boolean contains(float x, float y) {
    if (shape instanceof Rectangle) {
      return ((Rectangle) shape).contains(x, y);
    } else if (shape instanceof Circle) {
      Circle circle = (Circle) shape;
      float dx = x - circle.x;
      float dy = y - circle.y;
      return (dx * dx + dy * dy) <= (circle.radius * circle.radius);
    }
    return false;
  }

  /**
   * Get the command character associated with this zone.
   */
  public char getCommand() {
    return command;
  }

  /**
   * Get the shape for rendering purposes.
   */
  public Object getShape() {
    return shape;
  }

  /**
   * Check if this is the joystick zone.
   */
  public boolean isJoystick() {
    return this == JOYSTICK;
  }

  /**
   * Check if this is a D-pad zone.
   */
  public boolean isDpad() {
    return this == DPAD_UP || this == DPAD_DOWN || this == DPAD_LEFT || this == DPAD_RIGHT;
  }

  /**
   * Get joystick direction from touch position.
   * Returns the primary direction command based on angle from center.
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

  /**
   * Check if this is a HUD navigation zone.
   */
  public boolean isHudControl() {
    return name().startsWith("HUD_");
  }

  /**
   * Check if this is a game control zone (not HUD).
   */
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

  /**
   * Get the center position of this zone for rendering.
   */
  public Vector2 getCenter() {
    if (shape instanceof Rectangle) {
      Rectangle rect = (Rectangle) shape;
      return new Vector2(rect.x + rect.width / 2, rect.y + rect.height / 2);
    } else if (shape instanceof Circle) {
      Circle circle = (Circle) shape;
      return new Vector2(circle.x, circle.y);
    }
    return new Vector2();
  }
}
