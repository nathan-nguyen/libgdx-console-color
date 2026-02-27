package com.noiprocs.ui.libgdx.sprite;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.resources.ModelTextureLoader.TextureConfig;

public class PlayerSprite extends LibGDXSprite {
  private final TextureConfig flippedConfig;

  public PlayerSprite(TextureConfig baseConfig) {
    super(baseConfig);
    if (baseConfig != null) {
      TextureRegion flippedRegion = new TextureRegion(baseConfig.textureRegion);
      flippedRegion.flip(true, false);
      this.flippedConfig =
          new TextureConfig(
              flippedRegion,
              baseConfig.offsetX,
              baseConfig.offsetY,
              baseConfig.scaleX,
              baseConfig.scaleY);
    } else {
      this.flippedConfig = null;
    }
  }

  @Override
  public TextureConfig getConfig(Model model) {
    PlayerModel playerModel = (PlayerModel) model;
    return playerModel.getFacingDirection().y > 0 ? flippedConfig : super.getConfig(model);
  }
}
