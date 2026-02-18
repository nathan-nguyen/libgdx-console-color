package com.noiprocs.resources;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class RenderResources {
  private final SpriteBatch batch;
  private final BitmapFont font;
  private final BitmapFont hudFont;
  private final BitmapFont panelFont;

  public RenderResources() {
    batch = new SpriteBatch();
    FontGenerator fontGenerator = new FontGenerator();
    font = fontGenerator.generateMonospaceFont();
    hudFont = fontGenerator.generateHUDFont();
    panelFont = fontGenerator.generatePanelFont();
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

  public BitmapFont getPanelFont() {
    return panelFont;
  }

  public void dispose() {
    batch.dispose();
    font.dispose();
    hudFont.dispose();
    panelFont.dispose();
  }
}
