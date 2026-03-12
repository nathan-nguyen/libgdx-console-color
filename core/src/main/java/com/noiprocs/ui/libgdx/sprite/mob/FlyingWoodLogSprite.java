package com.noiprocs.ui.libgdx.sprite.mob;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.mob.MobModel;
import com.noiprocs.gameplay.model.mob.projectile.FlyingWoodLogModel;
import com.noiprocs.ui.libgdx.sprite.LibgdxRenderContext;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class FlyingWoodLogSprite extends LibgdxSprite {
  private static final String MODEL_CLASS = FlyingWoodLogModel.class.getName();

  private static final LibgdxTexture DEFAULT_TEXTURE = loadTexture(MODEL_CLASS, "default");

  public FlyingWoodLogSprite() {
    super(DEFAULT_TEXTURE);
  }

  @Override
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

    MobModel mobModel = (MobModel) model;
    float rotation = computeScreenAngle(mobModel);

    batch.setColor(1f, 1f, 1f, alpha);
    batch.draw(
        tex.textureRegion, screenX, screenY, imgW / 2f, imgH / 2f, imgW, imgH, 1f, 1f, rotation);
  }

  private static final float ROTATION_SCALE = 10f;

  private float computeScreenAngle(MobModel model) {
    return (model.position.x + model.position.y) / ROTATION_SCALE;
  }
}
