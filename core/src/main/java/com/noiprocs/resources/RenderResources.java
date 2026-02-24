package com.noiprocs.resources;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class RenderResources {
  private final SpriteBatch batch;
  private final BitmapFont font;
  private final BitmapFont hudFont;
  private final BitmapFont panelFont;
  private final ItemTextureManager itemTextureManager;
  private final ModelTextureManager modelTextureManager;
  private final ShapeRenderer shapeRenderer;

  public RenderResources() {
    batch = new SpriteBatch();
    FontGenerator fontGenerator = new FontGenerator();
    font = fontGenerator.generateMonospaceFont();
    hudFont = fontGenerator.generateHUDFont();
    panelFont = fontGenerator.generatePanelFont();
    itemTextureManager = new ItemTextureManager();
    modelTextureManager = new ModelTextureManager();
    shapeRenderer = new ShapeRenderer();
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

  public ItemTextureManager getItemTextureManager() {
    return itemTextureManager;
  }

  public ModelTextureManager getModelTextureManager() {
    return modelTextureManager;
  }

  public ShapeRenderer getShapeRenderer() {
    return shapeRenderer;
  }

  public void dispose() {
    batch.dispose();
    font.dispose();
    hudFont.dispose();
    panelFont.dispose();
    itemTextureManager.dispose();
    modelTextureManager.dispose();
    shapeRenderer.dispose();
  }
}
