package com.noiprocs.ui.libgdx.sprite;

import com.noiprocs.core.model.Model;
import com.noiprocs.resources.ModelTextureLoader.TextureConfig;
import com.noiprocs.resources.SpriteConfigLoader.SpriteEntry;
import java.util.Map;

public class LibGDXSprite {
  private final TextureConfig config;
  protected final Map<String, TextureConfig> namedConfigs;

  public LibGDXSprite(TextureConfig config) {
    this.config = config;
    this.namedConfigs = Map.of();
  }

  public LibGDXSprite(SpriteEntry entry) {
    this.config = entry.textureConfig;
    this.namedConfigs = entry.namedConfigs;
  }

  public TextureConfig getConfig(Model model) {
    return config;
  }
}
