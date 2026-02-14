package com.noiprocs.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/** Utility class for generating fonts. */
public class FontGenerator {
  private static final int FONT_SIZE = 12;

  private BitmapFont font;

  /**
   * Generates a monospace bitmap font using the bundled DejaVu Sans Mono font.
   *
   * @return A configured BitmapFont suitable for monospace text rendering with full Unicode support
   */
  public BitmapFont generateMonospaceFont() {
    // Load bundled DejaVu Sans Mono font from assets
    FreeTypeFontGenerator generator =
        new FreeTypeFontGenerator(Gdx.files.internal("DejaVuSansMono.ttf"));

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
          glyph.xadvance = (int) UIConfig.CHAR_WIDTH;
        }
      }
    }
    fontData.spaceXadvance = UIConfig.CHAR_HEIGHT;
  }
}
