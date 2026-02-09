package com.noiprocs.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/** Utility class for generating fonts. */
public class FontGenerator {
  private static final int FONT_SIZE = 12;
  // Total lines: 44 (2 player info + 40 map + 2 border), window height: 690
  public static final float CHAR_HEIGHT = 690f / 44;
  // Total columns: 62 (2 border + 60 map), window width: 440
  public static final float CHAR_WIDTH = 440f / 62;

  private BitmapFont font;

  /**
   * Generates a monospace bitmap font using system fonts.
   *
   * <p>Prioritizes fonts that match Java's "monospaced" logical font across different platforms.
   *
   * @return A configured BitmapFont suitable for monospace text rendering
   */
  public BitmapFont generateMonospaceFont() {
    String[] monospaceFonts = ResourceLoader.loadMonospaceFonts();

    FreeTypeFontGenerator generator = null;
    for (String fontPath : monospaceFonts) {
      try {
        if (Gdx.files.absolute(fontPath).exists()) {
          generator = new FreeTypeFontGenerator(Gdx.files.absolute(fontPath));
          break;
        }
      } catch (Exception e) {
        // Try next
      }
    }

    if (generator == null) {
      throw new RuntimeException("No monospace font found on this system");
    }

    FreeTypeFontParameter parameter = new FreeTypeFontParameter();
    parameter.size = FONT_SIZE;
    parameter.mono = true;
    parameter.characters = ResourceLoader.loadFontCharacters();
    parameter.color = Color.WHITE;

    font = generator.generateFont(parameter);
    generator.dispose();
    font.setUseIntegerPositions(true);
    // Disable markup to prevent any text formatting interference
    font.getData().markupEnabled = false;

    forceMonospacing();
    return font;
  }

  /**
   * Forces all glyphs to use the same advance width for true monospacing. This ensures consistent
   * character spacing across all glyphs.
   */
  private void forceMonospacing() {
    BitmapFont.BitmapFontData fontData = font.getData();
    for (int i = 0; i < fontData.glyphs.length; i++) {
      BitmapFont.Glyph[] page = fontData.glyphs[i];
      if (page == null) {
        continue;
      }
      for (BitmapFont.Glyph glyph : page) {
        if (glyph != null) {
          glyph.xadvance = (int) CHAR_WIDTH;
        }
      }
    }
    fontData.spaceXadvance = CHAR_HEIGHT;
  }
}
