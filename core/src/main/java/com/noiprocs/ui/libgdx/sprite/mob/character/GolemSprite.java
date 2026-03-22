package com.noiprocs.ui.libgdx.sprite.mob.character;

import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.action.AnimatedAction;
import com.noiprocs.core.model.mob.MobModel;
import com.noiprocs.gameplay.model.mob.character.GolemModel;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;
import com.noiprocs.ui.libgdx.sprite.mob.MobSprite;

public class GolemSprite extends MobSprite {
  private static final String MODEL_CLASS = GolemModel.class.getName();

  // STAND_SW_TEXTURE is this.texture (set by super())
  private final LibgdxTexture STAND_SE_TEXTURE = texture.flipped();
  private final LibgdxTexture STAND_NE_TEXTURE = loadTexture(MODEL_CLASS, "stand_ne");
  private final LibgdxTexture STAND_NW_TEXTURE = STAND_NE_TEXTURE.flipped();
  private final LibgdxTexture ACTION_SW_TEXTURE = loadTexture(MODEL_CLASS, "action_sw");
  private final LibgdxTexture ACTION_SE_TEXTURE = ACTION_SW_TEXTURE.flipped();
  private final LibgdxTexture ACTION_NE_TEXTURE = loadTexture(MODEL_CLASS, "action_ne");
  private final LibgdxTexture ACTION_NW_TEXTURE = ACTION_NE_TEXTURE.flipped();

  public GolemSprite() {
    super(loadTexture(MODEL_CLASS, "stand_sw"));
  }

  @Override
  public LibgdxTexture getTexture(Model model) {
    MobModel mobModel = (MobModel) model;
    Vector3D facingDirection = mobModel.getFacingDirection();
    boolean isActing = mobModel.getAction() instanceof AnimatedAction;

    if (facingDirection.x + facingDirection.y < 0) {
      if (facingDirection.y < 0) return isActing ? ACTION_NW_TEXTURE : STAND_NW_TEXTURE;
      return isActing ? ACTION_NE_TEXTURE : STAND_NE_TEXTURE;
    }
    if (facingDirection.y > 0) return isActing ? ACTION_SE_TEXTURE : STAND_SE_TEXTURE;
    return isActing ? ACTION_SW_TEXTURE : super.getTexture(model);
  }
}
