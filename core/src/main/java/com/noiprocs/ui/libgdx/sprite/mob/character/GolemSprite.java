package com.noiprocs.ui.libgdx.sprite.mob.character;

import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.action.AnimatedAction;
import com.noiprocs.core.model.mob.MobModel;
import com.noiprocs.gameplay.model.mob.character.GolemModel;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class GolemSprite extends LibgdxSprite {
  private static final String MODEL_CLASS = GolemModel.class.getName();

  private static final LibgdxTexture DEFAULT_TEXTURE = loadTexture(MODEL_CLASS, "default");
  private static final LibgdxTexture DOWN_FLIPPED_TEXTURE = DEFAULT_TEXTURE.flipped();
  private static final LibgdxTexture UP_TEXTURE = loadTexture(MODEL_CLASS, "up");
  private static final LibgdxTexture UP_FLIPPED_TEXTURE = UP_TEXTURE.flipped();
  private static final LibgdxTexture DOWN_ACTION_TEXTURE = loadTexture(MODEL_CLASS, "down_action");
  private static final LibgdxTexture DOWN_ACTION_FLIPPED_TEXTURE = DOWN_ACTION_TEXTURE.flipped();
  private static final LibgdxTexture UP_ACTION_TEXTURE = loadTexture(MODEL_CLASS, "up_action");
  private static final LibgdxTexture UP_ACTION_FLIPPED_TEXTURE = UP_ACTION_TEXTURE.flipped();

  public GolemSprite() {
    super(DEFAULT_TEXTURE);
  }

  @Override
  public LibgdxTexture getTexture(Model model) {
    MobModel mobModel = (MobModel) model;
    Vector3D facingDirection = mobModel.getFacingDirection();
    boolean isActing = mobModel.getAction() instanceof AnimatedAction;

    if (facingDirection.x + facingDirection.y < 0) {
      if (facingDirection.y < 0) return isActing ? UP_ACTION_FLIPPED_TEXTURE : UP_FLIPPED_TEXTURE;
      return isActing ? UP_ACTION_TEXTURE : UP_TEXTURE;
    }
    if (facingDirection.y > 0) return isActing ? DOWN_ACTION_FLIPPED_TEXTURE : DOWN_FLIPPED_TEXTURE;
    return isActing ? DOWN_ACTION_TEXTURE : super.getTexture(model);
  }
}
