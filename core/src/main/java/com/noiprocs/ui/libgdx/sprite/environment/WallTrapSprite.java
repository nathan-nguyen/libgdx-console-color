package com.noiprocs.ui.libgdx.sprite.environment;

import com.noiprocs.core.model.Model;
import com.noiprocs.gameplay.model.environment.WallTrapModel;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class WallTrapSprite extends LibgdxSprite {
  private static final String MODEL_CLASS = WallTrapModel.class.getName();

  private static final LibgdxTexture WAITING_TEXTURE = loadTexture(MODEL_CLASS, "waiting");
  private static final LibgdxTexture FIRE_TEXTURE = loadTexture(MODEL_CLASS, "fire");

  public WallTrapSprite() {
    super(WAITING_TEXTURE);
  }

  @Override
  public LibgdxTexture getTexture(Model model) {
    if (((WallTrapModel) model).isClosed()) return FIRE_TEXTURE;
    return WAITING_TEXTURE;
  }
}
