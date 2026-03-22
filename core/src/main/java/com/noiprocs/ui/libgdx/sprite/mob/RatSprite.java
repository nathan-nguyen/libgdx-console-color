package com.noiprocs.ui.libgdx.sprite.mob;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.mob.MobModel;
import com.noiprocs.gameplay.model.action.ChargeRetreatAction;
import com.noiprocs.gameplay.model.mob.RatModel;
import com.noiprocs.ui.libgdx.sprite.LibgdxRenderContext;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class RatSprite extends MobSprite {
  private static final String MODEL_CLASS = RatModel.class.getName();

  // SW_TEXTURE is this.texture (set by super())
  private final LibgdxTexture SE_TEXTURE = texture.flipped();
  private final LibgdxTexture NW_TEXTURE = loadTexture(MODEL_CLASS, "nw");
  private final LibgdxTexture NE_TEXTURE = NW_TEXTURE.flipped();

  public RatSprite() {
    super(loadTexture(MODEL_CLASS, "sw"));
  }

  @Override
  public LibgdxTexture getTexture(Model model) {
    MobModel mobModel = (MobModel) model;
    Vector3D facingDirection = mobModel.getFacingDirection();

    if (facingDirection.x + facingDirection.y < 0) {
      if (facingDirection.y < 0) return NW_TEXTURE;
      return NE_TEXTURE;
    }
    if (facingDirection.y > 0) return SE_TEXTURE;
    return super.getTexture(model);
  }

  @Override
  public void render(
      SpriteBatch batch,
      Model model,
      Model playerModel,
      float offsetX,
      float offsetY,
      LibgdxRenderContext ctx) {
    super.render(batch, model, playerModel, offsetX, offsetY, ctx);

    MobModel mobModel = (MobModel) model;
    if (!(mobModel.getAction() instanceof ChargeRetreatAction)) return;

    ChargeRetreatAction chargeAction = (ChargeRetreatAction) mobModel.getAction();
    LibgdxTexture tex = getTexture(model);
    float[] b = screenBounds(tex, model, offsetX, offsetY, ctx);
    float alpha =
        ctx.alphaResolver.resolve(model, playerModel, b[0], b[0] + b[2], b[1], b[1] + b[3]);

    if (chargeAction.isPreparing()) {
      batch.setColor(1f, 0.85f, 0.1f, 0.45f * alpha);
    } else {
      batch.setColor(0.7f, 0.1f, 1f, 0.55f * alpha);
    }
    batch.draw(tex.textureRegion, b[0], b[1], b[2], b[3]);
    batch.setColor(1f, 1f, 1f, 1f);
  }
}
