package com.noiprocs.ui.libgdx.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.noiprocs.resources.UIConfig;

/** Renders a seamlessly tiling background texture that scrolls with the world. */
public class BackgroundRenderer {
  private static final String BACKGROUND_CONFIG_JSON = "background-config.json";

  private final Texture backgroundTexture;
  private final TextureRegion backgroundRegion;
  private final float virtualWidth;
  private final float virtualHeight;
  private final float tileScale;
  private final float brightness;
  private final float uSpan;
  private final float vSpan;

  public BackgroundRenderer(float virtualWidth, float virtualHeight) {
    this.virtualWidth = virtualWidth;
    this.virtualHeight = virtualHeight;

    JsonValue config = new JsonReader().parse(Gdx.files.internal(BACKGROUND_CONFIG_JSON));
    String imagePath = config.getString("imagePath");
    tileScale = config.getFloat("tileScale");
    brightness = config.getFloat("brightness", 1.0f);

    backgroundTexture = new Texture(Gdx.files.internal(imagePath));
    backgroundTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
    uSpan = virtualWidth / backgroundTexture.getWidth() / tileScale;
    vSpan = virtualHeight / backgroundTexture.getHeight() / tileScale;
    backgroundRegion = new TextureRegion(backgroundTexture);
  }

  /**
   * Draws the tiling background. Must be called inside an active SpriteBatch begin/end block.
   * offsetX/offsetY are the tile-space camera offsets used by the isometric renderer.
   */
  public void render(SpriteBatch batch, float offsetX, float offsetY) {
    float texW = backgroundTexture.getWidth();
    float texH = backgroundTexture.getHeight();
    float u = -(offsetX - offsetY) * UIConfig.CHAR_SIZE / 2f / texW / tileScale;
    float v = (offsetX + offsetY) * UIConfig.CHAR_SIZE / 4f / texH / tileScale;
    backgroundRegion.setRegion(u, v, u + uSpan, v + vSpan);
    Color prevColor = batch.getColor().cpy();
    batch.setColor(brightness, brightness, brightness, prevColor.a);
    batch.draw(backgroundRegion, 0f, 0f, virtualWidth, virtualHeight);
    batch.setColor(prevColor);
  }

  public void dispose() {
    backgroundTexture.dispose();
  }
}
