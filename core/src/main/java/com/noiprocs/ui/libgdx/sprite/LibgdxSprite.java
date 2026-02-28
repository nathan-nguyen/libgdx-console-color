package com.noiprocs.ui.libgdx.sprite;

import com.noiprocs.core.model.Model;

public class LibgdxSprite {
  protected final LibgdxTexture texture;

  public LibgdxSprite(LibgdxTexture texture) {
    this.texture = texture;
  }

  protected static LibgdxTexture loadTexture(String modelClass, String name) {
    return LibgdxSpriteConfigLoader.get().getTexture(modelClass, name);
  }

  public LibgdxTexture getTexture(Model model) {
    return texture;
  }
}
