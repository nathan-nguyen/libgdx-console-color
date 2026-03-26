package com.noiprocs.ui.libgdx.sprite.mob;

import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.action.AnimatedAction;
import com.noiprocs.core.model.mob.MobModel;
import com.noiprocs.gameplay.model.mob.SpiderModel;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class SpiderSprite extends MobSprite {
  private static final String MODEL_CLASS = SpiderModel.class.getName();

  // STAND_SW_TEXTURE is this.texture (set by super())
  private final LibgdxTexture STAND_SE_TEXTURE = texture.flipped();
  private final LibgdxTexture STAND_NE_TEXTURE = loadTexture(MODEL_CLASS, "stand_ne");
  private final LibgdxTexture STAND_NW_TEXTURE = STAND_NE_TEXTURE.flipped();
  private final LibgdxTexture ATTACK_SW_TEXTURE = loadTexture(MODEL_CLASS, "attack_sw");
  private final LibgdxTexture ATTACK_SE_TEXTURE = ATTACK_SW_TEXTURE.flipped();
  private final LibgdxTexture ATTACK_NE_TEXTURE = loadTexture(MODEL_CLASS, "attack_ne");
  private final LibgdxTexture ATTACK_NW_TEXTURE = ATTACK_NE_TEXTURE.flipped();

  public SpiderSprite() {
    super(loadTexture(MODEL_CLASS, "stand_sw"));
  }

  @Override
  public LibgdxTexture getTexture(Model model) {
    MobModel mobModel = (MobModel) model;
    Vector3D facingDirection = mobModel.getFacingDirection();
    boolean isActing = mobModel.getAction() instanceof AnimatedAction;

    if (facingDirection.x + facingDirection.y < 0) {
      if (facingDirection.y < 0) return isActing ? ATTACK_NW_TEXTURE : STAND_NW_TEXTURE;
      return isActing ? ATTACK_NE_TEXTURE : STAND_NE_TEXTURE;
    }
    if (facingDirection.y > 0) return isActing ? ATTACK_SE_TEXTURE : STAND_SE_TEXTURE;
    return isActing ? ATTACK_SW_TEXTURE : super.getTexture(model);
  }
}
