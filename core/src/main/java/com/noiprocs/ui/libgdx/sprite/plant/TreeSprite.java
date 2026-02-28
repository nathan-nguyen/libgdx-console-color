package com.noiprocs.ui.libgdx.sprite.plant;

import com.noiprocs.core.model.Model;
import com.noiprocs.gameplay.model.plant.TreeModel;
import com.noiprocs.resources.ModelTextureLoader.TextureConfig;
import com.noiprocs.resources.SpriteConfigLoader.SpriteEntry;
import com.noiprocs.ui.libgdx.sprite.LibGDXSprite;

public class TreeSprite extends LibGDXSprite {
  private final TextureConfig youngConfig;
  private final TextureConfig middleConfig;

  public TreeSprite(SpriteEntry entry) {
    super(entry);
    this.youngConfig = namedConfigs.get("young");
    this.middleConfig = namedConfigs.get("middle");
  }

  @Override
  public TextureConfig getConfig(Model model) {
    TreeModel treeModel = (TreeModel) model;
    if (treeModel.isYoungAge()) return youngConfig;
    if (treeModel.isMiddleAge()) return middleConfig;
    return super.getConfig(model);
  }
}
