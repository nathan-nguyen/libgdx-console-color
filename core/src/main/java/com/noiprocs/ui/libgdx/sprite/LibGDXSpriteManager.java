package com.noiprocs.ui.libgdx.sprite;

import com.badlogic.gdx.utils.Disposable;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.item.ItemModel;
import com.noiprocs.resources.ModelTextureLoader;
import com.noiprocs.resources.ModelTextureLoader.TextureConfig;
import com.noiprocs.resources.SpriteConfigLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LibGDXSpriteManager implements Disposable {
  private final ModelTextureLoader textureLoader;
  private final SpriteConfigLoader spriteConfigLoader = new SpriteConfigLoader();
  private final Map<String, LibGDXSprite> spriteCache = new ConcurrentHashMap<>();

  public LibGDXSpriteManager(ModelTextureLoader textureLoader) {
    this.textureLoader = textureLoader;
  }

  public TextureConfig getConfig(Model model) {
    if (model == null) return null;
    String className =
        (model instanceof ItemModel)
            ? ((ItemModel) model).itemClass.getName()
            : model.getClass().getName();
    LibGDXSprite sprite =
        spriteCache.computeIfAbsent(
            className, cn -> LibGDXSpriteFactory.create(cn, textureLoader, spriteConfigLoader));
    return sprite != null ? sprite.getConfig(model) : null;
  }

  @Override
  public void dispose() {
    spriteConfigLoader.dispose();
  }
}
