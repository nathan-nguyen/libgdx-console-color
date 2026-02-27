package com.noiprocs.ui.libgdx.sprite;

import com.noiprocs.core.model.Model;
import com.noiprocs.resources.ModelTextureLoader.TextureConfig;

public class LibGDXSprite {
  private final TextureConfig config;

  public LibGDXSprite(TextureConfig config) {
    this.config = config;
  }

  public TextureConfig getConfig(Model model) {
    return config;
  }
}
