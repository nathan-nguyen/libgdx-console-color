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
  // D-pad zones (bottom left - S clear of bottom border)
  DPAD_UP(new Rectangle(50, 100, 55, 55), 'w'),
  DPAD_DOWN(new Rectangle(50, 10, 55, 55), 's'),
  DPAD_LEFT(new Rectangle(5, 55, 55, 55), 'a'),
  DPAD_RIGHT(new Rectangle(95, 55, 55, 55), 'd'),

  // Action buttons (bottom right, circular)
  ACTION_SPACE(new Circle(340, 75, 32), ' '),     // Primary action - center
  ACTION_FIRE(new Circle(395, 115, 28), 'f'),     // Fire - upper right
  ACTION_TOGGLE(new Circle(395, 35, 28), 't'),    // Toggle - lower right

  // Quick actions (top right corner - E button, below player info)
  QUICK_EQUIPMENT(new Rectangle(390, 580, 50, 50), 'e'),  // Equipment button

  // Inventory slots (top left, horizontal row, below player info)
  QUICK_SLOT_1(new Rectangle(5, 585, 45, 45), '1'),
  QUICK_SLOT_2(new Rectangle(55, 585, 45, 45), '2'),
  QUICK_SLOT_3(new Rectangle(105, 585, 45, 45), '3'),
  QUICK_SLOT_4(new Rectangle(155, 585, 45, 45), '4'),

  // HUD navigation (context-aware, shown when HUD is active)
  HUD_UP(new Rectangle(195, 600, 50, 50), 'w'),        // Top center
  HUD_DOWN(new Rectangle(195, 30, 50, 50), 's'),       // Bottom center
  HUD_LEFT(new Rectangle(5, 315, 50, 50), 'a'),        // Left middle
  HUD_RIGHT(new Rectangle(385, 315, 50, 50), 'd'),     // Right middle
  HUD_CONFIRM(new Circle(220, 90, 38), '\n'),          // Enter - bottom center
  HUD_CLOSE(new Circle(390, 640, 28), (char) 27),      // Escape - top right corner
  HUD_TAB(new Rectangle(40, 640, 70, 40), '\t'),       // Tab - top left
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
   * Check if this is a D-pad zone.
   */
  public boolean isDpad() {
    return this == DPAD_UP || this == DPAD_DOWN || this == DPAD_LEFT || this == DPAD_RIGHT;
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
