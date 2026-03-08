package com.noiprocs.ui.libgdx.sprite;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Config;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.event.EventType;
import com.noiprocs.resources.UIConfig;

public class LibgdxSprite {
  protected final LibgdxTexture texture;

  public LibgdxSprite(LibgdxTexture texture) {
    this.texture = texture;
  }

  protected static LibgdxTexture loadTexture(String modelClass, String name) {
    return LibgdxSpriteConfigLoader.get().getTexture(modelClass, name);
  }

  public LibgdxTexture getTexture(Model model) {
    return texture;
  }

  /** Returns {screenX, screenY, imgW, imgH} for the given texture at the model's world position. */
  protected float[] screenBounds(
      LibgdxTexture tex, Model model, float offsetX, float offsetY, LibgdxRenderContext ctx) {
    float modelX = (float) model.position.x / Config.WORLD_SCALE - offsetX;
    float modelY = (float) model.position.y / Config.WORLD_SCALE - offsetY;
    float screenX =
        (modelY - modelX) * UIConfig.CHAR_SIZE / 2f
            + UIConfig.CHAR_SIZE * (ctx.width + ctx.height) / 4f
            + tex.offsetX;
    float screenY =
        ctx.virtualHeight / 2f
            + UIConfig.CHAR_SIZE / 4f * ((ctx.height + ctx.width) / 2f - modelX - modelY)
            + tex.offsetY;
    float imgW = tex.textureRegion.getRegionWidth() * tex.scaleX;
    float imgH = tex.textureRegion.getRegionHeight() * tex.scaleY;
    return new float[] {screenX, screenY, imgW, imgH};
  }

  public void render(
      SpriteBatch batch,
      Model model,
      Model playerModel,
      float offsetX,
      float offsetY,
      LibgdxRenderContext ctx) {
    LibgdxTexture tex = getTexture(model);
    float[] b = screenBounds(tex, model, offsetX, offsetY, ctx);
    float screenX = b[0], screenY = b[1], imgW = b[2], imgH = b[3];
    float alpha =
        ctx.alphaResolver.resolve(
            model, playerModel, screenX, screenX + imgW, screenY, screenY + imgH);

    boolean isHurt = GameContext.get().modelManager.hasActiveEvent(model.id, EventType.HURT);
    boolean isInteract =
        GameContext.get().modelManager.hasActiveEvent(model.id, EventType.INTERACT);
    if (isHurt || isInteract) {
      float blurAlpha = alpha * 0.35f;
      batch.setColor(1f, 1f, 1f, blurAlpha);
      for (float dx : new float[] {-3f, 3f, 0f, 0f}) {
        for (float dy : new float[] {0f, 0f, -3f, 3f}) {
          batch.draw(tex.textureRegion, screenX + dx, screenY + dy, imgW, imgH);
        }
      }
    }
    if (isHurt) {
      batch.setColor(1f, 0.6f, 0.6f, alpha);
    } else {
      batch.setColor(1f, 1f, 1f, alpha);
    }
    batch.draw(tex.textureRegion, screenX, screenY, imgW, imgH);
  }
}
