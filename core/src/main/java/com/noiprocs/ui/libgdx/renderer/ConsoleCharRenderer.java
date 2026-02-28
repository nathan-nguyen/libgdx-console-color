package com.noiprocs.ui.libgdx.renderer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Config;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.event.EventType;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.ui.console.sprite.ConsoleSprite;
import com.noiprocs.ui.console.sprite.ConsoleTexture;
import com.noiprocs.ui.console.util.ColorMapper;
import java.util.HashMap;
import java.util.Map;

/** Renders models that use console-style character textures (ConsoleSprite/ConsoleTexture). */
public class ConsoleCharRenderer {
  private static final Map<Character, Color> COLOR_CHAR_MAP = new HashMap<>();

  static {
    for (Map.Entry<Character, ColorMapper.RGB> entry : ColorMapper.COLOR_MAP.entrySet()) {
      ColorMapper.RGB rgb = entry.getValue();
      COLOR_CHAR_MAP.put(entry.getKey(), new Color(rgb.r / 255f, rgb.g / 255f, rgb.b / 255f, 1f));
    }
  }

  private final int height;
  private final int width;
  // Pixel height of the rendering area (viewport world height).
  private final float virtualHeight;
  private final OcclusionAlphaResolver occlusionAlphaResolver;

  public ConsoleCharRenderer(
      int height, int width, float virtualHeight, OcclusionAlphaResolver occlusionAlphaResolver) {
    this.height = height;
    this.width = width;
    this.virtualHeight = virtualHeight;
    this.occlusionAlphaResolver = occlusionAlphaResolver;
  }

  /**
   * Renders a single model's console character texture into the sprite batch, applying isometric
   * positioning, occlusion alpha, and hurt color override.
   */
  public void render(
      SpriteBatch batch,
      BitmapFont font,
      Model model,
      Model playerModel,
      float offsetX,
      float offsetY) {
    GameContext gameContext = GameContext.get();
    ConsoleSprite consoleSprite =
        (ConsoleSprite) gameContext.spriteManager.createRenderableObject(model);
    ConsoleTexture consoleTexture = consoleSprite.getTexture(model);
    char[][] consoleTextureData = consoleTexture.texture;

    float posX = (float) model.position.x / Config.WORLD_SCALE - offsetX - consoleTexture.offsetX;
    float posY = (float) model.position.y / Config.WORLD_SCALE - offsetY - consoleTexture.offsetY;

    if (model.id.equals(playerModel.id)) {
      posX = height / 2f - consoleTexture.offsetX;
      posY = width / 2f - consoleTexture.offsetY;
    }

    char overrideColor = 0;
    if (gameContext.modelManager.hasActiveEvent(model.id, EventType.HURT)) {
      overrideColor = 'r';
    }

    boolean isoTexture = IsometricRenderPolicy.useIsometricTexture(model);
    float baseScreenX, baseScreenY;
    if (isoTexture) {
      baseScreenX =
          (posY - posX) * UIConfig.CHAR_SIZE / 2f + UIConfig.CHAR_SIZE * (width + height) / 4f;
      baseScreenY =
          virtualHeight / 2f + UIConfig.CHAR_SIZE / 4f * ((height + width) / 2f - posX - posY);
    } else {
      float anchorX = posX + consoleTexture.offsetX;
      float anchorY = posY + consoleTexture.offsetY;
      float anchorScreenX =
          (anchorY - anchorX) * UIConfig.CHAR_SIZE / 2f
              + UIConfig.CHAR_SIZE * (width + height) / 4f;
      float anchorScreenY =
          virtualHeight / 2f
              + UIConfig.CHAR_SIZE / 4f * ((height + width) / 2f - anchorX - anchorY);
      baseScreenX = anchorScreenX - consoleTexture.offsetY * UIConfig.CHAR_WIDTH;
      baseScreenY = anchorScreenY + consoleTexture.offsetX * UIConfig.CHAR_HEIGHT;
    }

    float alpha =
        occlusionAlphaResolver.resolve(
            model,
            playerModel,
            consoleTextureData,
            isoTexture,
            posX,
            posY,
            baseScreenX,
            baseScreenY,
            width,
            height,
            virtualHeight);

    for (int i = 0; i < consoleTextureData.length; i++) {
      for (int j = 0; j < consoleTextureData[0].length; j++) {
        if (consoleTextureData[i][j] == 0) continue;
        float screenX, screenY;
        if (isoTexture) {
          float x = posX + i;
          float y = posY + j;
          screenX = (y - x) * UIConfig.CHAR_SIZE / 2f + UIConfig.CHAR_SIZE * (width + height) / 4f;
          screenY = virtualHeight / 2f + UIConfig.CHAR_SIZE / 4f * ((height + width) / 2f - x - y);
        } else {
          screenX = baseScreenX + j * UIConfig.CHAR_WIDTH;
          screenY = baseScreenY - i * UIConfig.CHAR_HEIGHT;
        }
        if (screenX >= 0
            && screenX < width * UIConfig.CHAR_SIZE
            && screenY >= 0
            && screenY < virtualHeight) {
          char colorChar =
              overrideColor != 0
                  ? overrideColor
                  : (consoleTexture.colorMap != null ? consoleTexture.colorMap[i][j] : 0);
          Color color =
              (colorChar != 0) ? COLOR_CHAR_MAP.getOrDefault(colorChar, Color.WHITE) : Color.WHITE;
          batch.setColor(color.r, color.g, color.b, alpha);
          renderCharAtPosition(batch, font, consoleTextureData[i][j], screenX, screenY);
        }
      }
    }
  }

  /** Draws a single character glyph from the given font at the specified screen coordinates. */
  private void renderCharAtPosition(SpriteBatch batch, BitmapFont font, char ch, float x, float y) {
    if (ch == ' ' || ch == 0) {
      return;
    }

    BitmapFont.Glyph glyph = font.getData().getGlyph(ch);
    if (glyph != null && glyph.page < font.getRegions().size) {
      float scale = font.getData().scaleX;
      float glyphX = x + glyph.xoffset * scale;
      float glyphY = y + glyph.yoffset * scale;

      batch.draw(
          font.getRegions().get(glyph.page).getTexture(),
          Math.round(glyphX),
          Math.round(glyphY),
          glyph.width * scale,
          glyph.height * scale,
          glyph.u,
          glyph.v,
          glyph.u2,
          glyph.v2);
    }
  }
}
