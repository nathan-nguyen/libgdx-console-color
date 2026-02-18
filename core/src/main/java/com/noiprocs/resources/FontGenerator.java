package com.noiprocs.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

/** Utility class for generating fonts. */
public class FontGenerator {
  private static final int FONT_SIZE = 12;
  private static final int HUD_FONT_SIZE = 12;

  private BitmapFont font;

  /**
   * Generates a high-resolution font for HUD labels. The font is rasterized at the physical screen
   * pixel size and scaled down to virtual coordinates, so it stays sharp at any display resolution.
   *
   * @return A BitmapFont sized for HUD use
   */
  public BitmapFont generateHUDFont() {
    FreeTypeFontGenerator generator =
        new FreeTypeFontGenerator(Gdx.files.internal("DejaVuSansMono.ttf"));

    float screenScale = Gdx.graphics.getHeight() / UIConfig.BASE_VIRTUAL_HEIGHT;

    FreeTypeFontParameter parameter = new FreeTypeFontParameter();
    parameter.size = Math.round(HUD_FONT_SIZE * screenScale);
    parameter.characters = ResourceLoader.loadFontCharacters();
    parameter.color = Color.WHITE;

    BitmapFont hudFont = generator.generateFont(parameter);
    generator.dispose();
    hudFont.setUseIntegerPositions(false);
    hudFont.getData().setScale(1f / screenScale);
    return hudFont;
  }

  /**
   * Generates a monospace bitmap font using the bundled DejaVu Sans Mono font.
   *
   * @return A configured BitmapFont suitable for monospace text rendering with full Unicode support
   */
  public BitmapFont generateMonospaceFont() {
    FreeTypeFontGenerator generator =
        new FreeTypeFontGenerator(Gdx.files.internal("DejaVuSansMono.ttf"));

    float screenScale = Gdx.graphics.getHeight() / UIConfig.BASE_VIRTUAL_HEIGHT;

    FreeTypeFontParameter parameter = new FreeTypeFontParameter();
    parameter.size = Math.round(FONT_SIZE * screenScale);
    parameter.mono = true;
    parameter.characters = ResourceLoader.loadFontCharacters();
    parameter.color = Color.WHITE;

    font = generator.generateFont(parameter);
    generator.dispose();
    font.setUseIntegerPositions(false);
    font.getData().markupEnabled = false;
    font.getData().setScale(1f / screenScale);

    forceMonospacing(screenScale);
    return font;
  }

  /**
   * Generates a font for HUD panel labels. Rasterized at virtual size so that Label.setFontScale()
   * values work as expected in virtual coordinate units.
   *
   * @return A BitmapFont sized for HUD panel use
   */
  public BitmapFont generatePanelFont() {
    FreeTypeFontGenerator generator =
        new FreeTypeFontGenerator(Gdx.files.internal("DejaVuSansMono.ttf"));

    FreeTypeFontParameter parameter = new FreeTypeFontParameter();
    parameter.size = HUD_FONT_SIZE;
    parameter.characters = ResourceLoader.loadFontCharacters();
    parameter.color = Color.WHITE;

    BitmapFont panelFont = generator.generateFont(parameter);
    generator.dispose();
    panelFont.setUseIntegerPositions(false);
    return panelFont;
  }

  private void forceMonospacing(float screenScale) {
    BitmapFont.BitmapFontData fontData = font.getData();
    for (int i = 0; i < fontData.glyphs.length; i++) {
      BitmapFont.Glyph[] page = fontData.glyphs[i];
      if (page == null) {
        continue;
      }
      for (BitmapFont.Glyph glyph : page) {
        if (glyph != null) {
          glyph.xadvance = Math.round(UIConfig.CHAR_WIDTH * screenScale);
        }
      }
    }
    fontData.spaceXadvance = Math.round(UIConfig.CHAR_HEIGHT * screenScale);
  }
}
