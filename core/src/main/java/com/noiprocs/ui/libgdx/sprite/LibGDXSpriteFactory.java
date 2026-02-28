package com.noiprocs.ui.libgdx.sprite;

import com.noiprocs.ui.libgdx.sprite.LibgdxSpriteConfigLoader.SpriteEntry;
import java.lang.reflect.Constructor;

public class LibGDXSpriteFactory {

  public static LibgdxSprite create(String className) {
    SpriteEntry entry = LibgdxSpriteConfigLoader.get().getEntry(className);
    if (entry == null) return null;
    return instantiateSprite(entry.spriteClass, className);
  }

  private static LibgdxSprite instantiateSprite(String spriteClass, String modelClass) {
    if (spriteClass == null) {
      return new LibgdxSprite(LibgdxSprite.loadTexture(modelClass, "default"));
    }
    try {
      Constructor<?> ctor = Class.forName(spriteClass).getConstructor();
      return (LibgdxSprite) ctor.newInstance();
    } catch (Exception e) {
      return new LibgdxSprite(LibgdxSprite.loadTexture(modelClass, "default"));
    }
  }
}
