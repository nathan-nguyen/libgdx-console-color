package com.noiprocs.ui.libgdx.sprite.environment;

import com.noiprocs.core.model.Model;
import com.noiprocs.gameplay.model.building.ShrineModel;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class ShrineSprite extends LibgdxSprite {
  private static final String MODEL_CLASS = ShrineModel.class.getName();

  private final LibgdxTexture ACTIVE_TEXTURE = loadTexture(MODEL_CLASS, "active");

  public ShrineSprite() {
    super(loadTexture(MODEL_CLASS, "inactive"));
  }

  @Override
  public LibgdxTexture getTexture(Model model) {
    if (((ShrineModel) model).isActivated()) return ACTIVE_TEXTURE;
    return super.getTexture(model);
  }
}
