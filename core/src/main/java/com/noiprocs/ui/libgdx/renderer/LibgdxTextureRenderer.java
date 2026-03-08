package com.noiprocs.ui.libgdx.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.noiprocs.core.model.Model;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.libgdx.sprite.LibGDXSpriteManager;
import com.noiprocs.ui.libgdx.sprite.LibgdxRenderContext;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;

public class LibgdxTextureRenderer {
  private final LibgdxRenderContext renderContext;
  private final SettingsManager settingsManager;
  private final LibGDXSpriteManager libgdxSpriteManager = new LibGDXSpriteManager();

  public LibgdxTextureRenderer(LibgdxRenderContext renderContext, SettingsManager settingsManager) {
    this.settingsManager = settingsManager;
    this.renderContext = renderContext;
  }

  public boolean canRender(Model model) {
    return libgdxSpriteManager.hasTexture(model)
        && IsometricRenderPolicy.useImageTexture(model, settingsManager.isShowWalls());
  }

  public void render(
      SpriteBatch batch, Model model, Model playerModel, float offsetX, float offsetY) {
    LibgdxSprite sprite = libgdxSpriteManager.getSprite(model);
    sprite.render(batch, model, playerModel, offsetX, offsetY, renderContext);
  }
}
