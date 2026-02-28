package com.noiprocs.ui.libgdx.sprite.plant;

import com.noiprocs.core.model.Model;
import com.noiprocs.gameplay.model.plant.TreeModel;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class TreeSprite extends LibgdxSprite {
  private static final String MODEL_CLASS = TreeModel.class.getName();

  private static final LibgdxTexture DEFAULT_TEXTURE = loadTexture(MODEL_CLASS, "default");
  private static final LibgdxTexture YOUNG_TEXTURE = loadTexture(MODEL_CLASS, "young");
  private static final LibgdxTexture MIDDLE_TEXTURE = loadTexture(MODEL_CLASS, "middle");

  public TreeSprite() {
    super(DEFAULT_TEXTURE);
  }

  @Override
  public LibgdxTexture getTexture(Model model) {
    TreeModel treeModel = (TreeModel) model;
    if (treeModel.isYoungAge()) return YOUNG_TEXTURE;
    if (treeModel.isMiddleAge()) return MIDDLE_TEXTURE;
    return super.getTexture(model);
  }
}
