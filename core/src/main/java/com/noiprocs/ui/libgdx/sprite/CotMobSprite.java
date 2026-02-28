package com.noiprocs.ui.libgdx.sprite;

import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.mob.MobModel;
import com.noiprocs.resources.ModelTextureLoader.TextureConfig;
import com.noiprocs.resources.SpriteConfigLoader.SpriteEntry;

public class CotMobSprite extends LibGDXSprite {
  private final TextureConfig upConfig;
  private final TextureConfig upFlippedConfig;
  private final TextureConfig downFlippedConfig;

  public CotMobSprite(SpriteEntry entry) {
    super(entry);
    TextureConfig up = namedConfigs.get("up");
    this.upConfig = up != null ? up : (entry.textureConfig != null ? entry.textureConfig.flipped() : null);
    this.upFlippedConfig = this.upConfig != null ? this.upConfig.flipped() : null;
    this.downFlippedConfig = entry.textureConfig != null ? entry.textureConfig.flipped() : null;
  }

  @Override
  public TextureConfig getConfig(Model model) {
    MobModel mobModel = (MobModel) model;
    Vector3D facingDirection = mobModel.getFacingDirection();
    if (facingDirection.x + facingDirection.y < 0) {
      return facingDirection.y < 0 ? upFlippedConfig : upConfig;
    }
    return facingDirection.y > 0 ? downFlippedConfig : super.getConfig(model);
  }
}
