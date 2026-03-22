package com.noiprocs.ui.libgdx.sprite.environment;

import com.noiprocs.core.model.Model;
import com.noiprocs.gameplay.model.environment.WallTrapModel;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class WallTrapSprite extends LibgdxSprite {
  private static final String MODEL_CLASS = WallTrapModel.class.getName();

  // WAITING_TEXTURE is this.texture (set by super())
  private final LibgdxTexture FIRE_TEXTURE = loadTexture(MODEL_CLASS, "fire");

  public WallTrapSprite() {
    super(loadTexture(MODEL_CLASS, "waiting"));
  }

  @Override
  public LibgdxTexture getTexture(Model model) {
    if (((WallTrapModel) model).isClosed()) return FIRE_TEXTURE;
    return super.getTexture(model);
  }
}
