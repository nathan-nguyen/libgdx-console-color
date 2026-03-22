package com.noiprocs.ui.libgdx.sprite.plant;

import com.noiprocs.core.model.Model;
import com.noiprocs.gameplay.model.plant.TreeModel;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class TreeSprite extends LibgdxSprite {
  private static final String MODEL_CLASS = TreeModel.class.getName();

  // DEFAULT_TEXTURE is this.texture (set by super())
  private final LibgdxTexture YOUNG_TEXTURE = loadTexture(MODEL_CLASS, "young");
  private final LibgdxTexture MIDDLE_TEXTURE = loadTexture(MODEL_CLASS, "middle");

  public TreeSprite() {
    super(loadTexture(MODEL_CLASS, "default"));
  }

  @Override
  public LibgdxTexture getTexture(Model model) {
    TreeModel treeModel = (TreeModel) model;
    if (treeModel.isYoungAge()) return YOUNG_TEXTURE;
    if (treeModel.isMiddleAge()) return MIDDLE_TEXTURE;
    return super.getTexture(model);
  }
}
