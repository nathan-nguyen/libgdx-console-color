package com.noiprocs.ui.libgdx.sprite;

import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.mob.MobModel;
import com.noiprocs.resources.ModelTextureLoader.TextureConfig;
import com.noiprocs.resources.SpriteConfigLoader.SpriteEntry;

public class GolemSprite extends LibGDXSprite {
  private final TextureConfig upConfig;

  public GolemSprite(SpriteEntry entry) {
    super(entry);
    TextureConfig up = namedConfigs.get("up");
    this.upConfig = up != null ? up : (entry.textureConfig != null ? entry.textureConfig.flipped() : null);
  }

  @Override
  public TextureConfig getConfig(Model model) {
    MobModel mobModel = (MobModel) model;
    Vector3D facingDirection = mobModel.getFacingDirection();
    return facingDirection.x + facingDirection.y < 0 ? upConfig : super.getConfig(model);
  }
}
