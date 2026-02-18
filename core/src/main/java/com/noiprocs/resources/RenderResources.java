package com.noiprocs.resources;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RenderResources {
  private final SpriteBatch batch;
  private final BitmapFont font;
  private final BitmapFont hudFont;

  public RenderResources() {
    batch = new SpriteBatch();
    FontGenerator fontGenerator = new FontGenerator();
    font = fontGenerator.generateMonospaceFont();
    hudFont = fontGenerator.generateHUDFont();
  }

  public SpriteBatch getBatch() {
    return batch;
  }

  public BitmapFont getFont() {
    return font;
  }

  public BitmapFont getHudFont() {
    return hudFont;
  }

  public void dispose() {
    batch.dispose();
    font.dispose();
    hudFont.dispose();
  }
}
