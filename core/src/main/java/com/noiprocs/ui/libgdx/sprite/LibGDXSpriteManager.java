package com.noiprocs.ui.libgdx.sprite;

import com.badlogic.gdx.utils.Disposable;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.item.ItemModel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LibGDXSpriteManager implements Disposable {
  private final Map<String, LibgdxSprite> spriteCache = new ConcurrentHashMap<>();

  public boolean hasTexture(Model model) {
    return getTexture(model) != null;
  }

  public LibgdxTexture getTexture(Model model) {
    if (model == null) return null;
    String className =
        (model instanceof ItemModel)
            ? ((ItemModel) model).itemClass.getName()
            : model.getClass().getName();
    LibgdxSprite sprite = spriteCache.computeIfAbsent(className, LibGDXSpriteFactory::create);
    return sprite != null ? sprite.getTexture(model) : null;
  }

  @Override
  public void dispose() {
    LibgdxSpriteConfigLoader.get().dispose();
  }
}
