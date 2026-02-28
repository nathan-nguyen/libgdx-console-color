package com.noiprocs.ui.libgdx.sprite.mob.character;

import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.action.AnimatedAction;
import com.noiprocs.core.model.mob.MobModel;
import com.noiprocs.resources.ModelTextureLoader.TextureConfig;
import com.noiprocs.resources.SpriteConfigLoader.SpriteEntry;
import com.noiprocs.ui.libgdx.sprite.LibGDXSprite;

public class GolemSprite extends LibGDXSprite {
  private final TextureConfig upConfig;
  private final TextureConfig upFlippedConfig;
  private final TextureConfig downFlippedConfig;
  private final TextureConfig downActionConfig;
  private final TextureConfig downActionFlippedConfig;
  private final TextureConfig upActionConfig;
  private final TextureConfig upActionFlippedConfig;

  public GolemSprite(SpriteEntry entry) {
    super(entry);
    TextureConfig up = namedConfigs.get("up");
    this.upConfig = up != null ? up : (entry.textureConfig != null ? entry.textureConfig.flipped() : null);
    this.upFlippedConfig = this.upConfig != null ? this.upConfig.flipped() : null;
    this.downFlippedConfig = entry.textureConfig != null ? entry.textureConfig.flipped() : null;

    TextureConfig downAction = namedConfigs.get("down_action");
    this.downActionConfig = downAction != null ? downAction : entry.textureConfig;
    this.downActionFlippedConfig = downAction != null ? downAction.flipped() : this.downFlippedConfig;
    TextureConfig upAction = namedConfigs.get("up_action");
    this.upActionConfig = upAction != null ? upAction : this.upConfig;
    this.upActionFlippedConfig = upAction != null ? upAction.flipped() : this.upFlippedConfig;
  }

  @Override
  public TextureConfig getConfig(Model model) {
    MobModel mobModel = (MobModel) model;
    Vector3D facingDirection = mobModel.getFacingDirection();
    boolean isActing = mobModel.getAction() instanceof AnimatedAction;

    if (facingDirection.x + facingDirection.y < 0) {
      if (facingDirection.y < 0) return isActing ? upActionFlippedConfig : upFlippedConfig;
      return isActing ? upActionConfig : upConfig;
    }
    if (facingDirection.y > 0) return isActing ? downActionFlippedConfig : downFlippedConfig;
    return isActing ? downActionConfig : super.getConfig(model);
  }
}
