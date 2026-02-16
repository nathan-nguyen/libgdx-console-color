package com.noiprocs.ui.libgdx.hud.widget;

import com.badlogic.gdx.graphics.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Renders item icons using character-based representation with colors. Converts item objects to
 * visual character + color pairs for display in graphical HUD slots.
 */
public class ItemIconRenderer {

  /** Represents an item's visual appearance as a character and color. */
  public static class ItemIcon {
    public final char character;
    public final Color color;
    public final String displayName;

    public ItemIcon(char character, Color color, String displayName) {
      this.character = character;
      this.color = color;
      this.displayName = displayName;
    }
  }

  // Map item types/categories to character representations
  private static final Map<String, Character> ITEM_TYPE_CHARS = new HashMap<>();
  private static final Map<String, Color> ITEM_TYPE_COLORS = new HashMap<>();

  static {
    // Weapons
    ITEM_TYPE_CHARS.put("SWORD", 'S');
    ITEM_TYPE_COLORS.put("SWORD", new Color(0.8f, 0.8f, 0.9f, 1f)); // Light blue-gray

    ITEM_TYPE_CHARS.put("BOW", 'B');
    ITEM_TYPE_COLORS.put("BOW", new Color(0.6f, 0.4f, 0.2f, 1f)); // Brown

    ITEM_TYPE_CHARS.put("AXE", 'A');
    ITEM_TYPE_COLORS.put("AXE", new Color(0.7f, 0.7f, 0.7f, 1f)); // Gray

    // Armor
    ITEM_TYPE_CHARS.put("HELMET", 'H');
    ITEM_TYPE_COLORS.put("HELMET", new Color(0.6f, 0.6f, 0.7f, 1f)); // Light gray

    ITEM_TYPE_CHARS.put("CHEST", 'C');
    ITEM_TYPE_COLORS.put("CHEST", new Color(0.5f, 0.5f, 0.6f, 1f)); // Medium gray

    ITEM_TYPE_CHARS.put("LEGS", 'L');
    ITEM_TYPE_COLORS.put("LEGS", new Color(0.4f, 0.4f, 0.5f, 1f)); // Dark gray

    ITEM_TYPE_CHARS.put("BOOTS", 'F');
    ITEM_TYPE_COLORS.put("BOOTS", new Color(0.3f, 0.2f, 0.1f, 1f)); // Dark brown

    // Materials
    ITEM_TYPE_CHARS.put("WOOD", 'W');
    ITEM_TYPE_COLORS.put("WOOD", new Color(0.5f, 0.3f, 0.1f, 1f)); // Brown

    ITEM_TYPE_CHARS.put("STONE", 'T');
    ITEM_TYPE_COLORS.put("STONE", new Color(0.5f, 0.5f, 0.5f, 1f)); // Gray

    ITEM_TYPE_CHARS.put("IRON", 'I');
    ITEM_TYPE_COLORS.put("IRON", new Color(0.6f, 0.6f, 0.7f, 1f)); // Light gray

    ITEM_TYPE_CHARS.put("GOLD", 'G');
    ITEM_TYPE_COLORS.put("GOLD", new Color(1.0f, 0.84f, 0.0f, 1f)); // Gold

    // Consumables
    ITEM_TYPE_CHARS.put("POTION", 'P');
    ITEM_TYPE_COLORS.put("POTION", new Color(1.0f, 0.2f, 0.2f, 1f)); // Red

    ITEM_TYPE_CHARS.put("FOOD", 'E');
    ITEM_TYPE_COLORS.put("FOOD", new Color(0.9f, 0.7f, 0.3f, 1f)); // Orange

    // Default
    ITEM_TYPE_CHARS.put("DEFAULT", '?');
    ITEM_TYPE_COLORS.put("DEFAULT", Color.WHITE);
  }

  /**
   * Renders an item as a character icon. Uses the item's type/name to determine the character and
   * color.
   *
   * @param item Item object (uses toString() to extract type info)
   * @return ItemIcon with character, color, and display name
   */
  public static ItemIcon renderIcon(Object item) {
    if (item == null) {
      return null;
    }

    // Extract item type from item object
    // For now, use toString() and try to match against known patterns
    String itemStr = item.toString().toUpperCase();
    String displayName = item.toString();

    // Try to match item type
    char iconChar = '?';
    Color iconColor = Color.WHITE;

    for (Map.Entry<String, Character> entry : ITEM_TYPE_CHARS.entrySet()) {
      if (itemStr.contains(entry.getKey())) {
        iconChar = entry.getValue();
        iconColor = ITEM_TYPE_COLORS.get(entry.getKey());
        break;
      }
    }

    // If no match, use first character of item name
    if (iconChar == '?') {
      iconChar = itemStr.isEmpty() ? '?' : itemStr.charAt(0);
      iconColor = ITEM_TYPE_COLORS.get("DEFAULT");
    }

    return new ItemIcon(iconChar, iconColor, displayName);
  }
}
