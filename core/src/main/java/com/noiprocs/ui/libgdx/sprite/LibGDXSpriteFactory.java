package com.noiprocs.ui.libgdx.sprite;

import com.noiprocs.resources.ModelTextureLoader;
import com.noiprocs.resources.ModelTextureLoader.TextureConfig;
import com.noiprocs.resources.SpriteConfigLoader;
import com.noiprocs.resources.SpriteConfigLoader.SpriteEntry;
import java.lang.reflect.Constructor;

public class LibGDXSpriteFactory {

  public static LibGDXSprite create(
      String className, ModelTextureLoader textureLoader, SpriteConfigLoader spriteConfigLoader) {
    SpriteEntry entry = spriteConfigLoader.getEntry(className);
    if (entry != null) {
      return instantiateSprite(entry.spriteClass, entry.textureConfig);
    }

    TextureConfig baseConfig = textureLoader.getConfig(className);
    return baseConfig != null ? new LibGDXSprite(baseConfig) : null;
  }

  private static LibGDXSprite instantiateSprite(String spriteClass, TextureConfig config) {
    if (spriteClass == null) {
      return new LibGDXSprite(config);
    }
    try {
      Constructor<?> ctor = Class.forName(spriteClass).getConstructor(TextureConfig.class);
      return (LibGDXSprite) ctor.newInstance(config);
    } catch (Exception e) {
      return new LibGDXSprite(config);
    }
  }
}
