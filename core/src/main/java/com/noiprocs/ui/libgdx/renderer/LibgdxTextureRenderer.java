package com.noiprocs.ui.libgdx.renderer;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Config;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.event.EventType;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.libgdx.sprite.LibGDXSpriteManager;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class LibgdxTextureRenderer {
  private final int height;
  private final int width;
  // Pixel height of the rendering area (viewport world height).
  private final float virtualHeight;
  private final OcclusionAlphaResolver occlusionAlphaResolver;
  private final SettingsManager settingsManager;
  private final LibGDXSpriteManager libgdxSpriteManager = new LibGDXSpriteManager();

  public LibgdxTextureRenderer(
      int height,
      int width,
      float virtualHeight,
      OcclusionAlphaResolver occlusionAlphaResolver,
      SettingsManager settingsManager) {
    this.height = height;
    this.width = width;
    this.virtualHeight = virtualHeight;
    this.occlusionAlphaResolver = occlusionAlphaResolver;
    this.settingsManager = settingsManager;
  }

  public boolean canRender(Model model) {
    return libgdxSpriteManager.hasTexture(model)
        && IsometricRenderPolicy.useImageTexture(model, settingsManager.isShowWalls());
  }

  public void render(
      SpriteBatch batch, Model model, Model playerModel, float offsetX, float offsetY) {
    LibgdxTexture texture = libgdxSpriteManager.getTexture(model);
    GameContext gameContext = GameContext.get();
    float modelX = (float) model.position.x / Config.WORLD_SCALE - offsetX;
    float modelY = (float) model.position.y / Config.WORLD_SCALE - offsetY;
    float screenX =
        (modelY - modelX) * UIConfig.CHAR_SIZE / 2f
            + UIConfig.CHAR_SIZE * (width + height) / 4f
            + texture.offsetX;
    float screenY =
        virtualHeight / 2f
            + UIConfig.CHAR_SIZE / 4f * ((height + width) / 2f - modelX - modelY)
            + texture.offsetY;
    float imgW = texture.textureRegion.getRegionWidth() * texture.scaleX;
    float imgH = texture.textureRegion.getRegionHeight() * texture.scaleY;
    float alpha =
        occlusionAlphaResolver.resolve(
            model,
            playerModel,
            width,
            virtualHeight,
            screenX,
            screenX + imgW,
            screenY,
            screenY + imgH);

    // Render special effect if model is hurt
    if (gameContext.modelManager.hasActiveEvent(model.id, EventType.HURT)) {
      float blurAlpha = alpha * 0.35f;
      batch.setColor(1f, 1f, 1f, blurAlpha);
      for (float dx : new float[] {-3f, 3f, 0f, 0f}) {
        for (float dy : new float[] {0f, 0f, -3f, 3f}) {
          batch.draw(texture.textureRegion, screenX + dx, screenY + dy, imgW, imgH);
        }
      }
    }
    batch.setColor(1f, 1f, 1f, alpha);
    batch.draw(texture.textureRegion, screenX, screenY, imgW, imgH);
  }
}
