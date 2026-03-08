package com.noiprocs.ui.libgdx.sprite;

import com.badlogic.gdx.utils.Disposable;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.item.ItemModel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LibGDXSpriteManager implements Disposable {
  private final Map<String, LibgdxSprite> spriteCache = new ConcurrentHashMap<>();

  public LibgdxSprite getSprite(Model model) {
    if (model == null) return null;
    String className =
        (model instanceof ItemModel)
            ? ((ItemModel) model).itemClass.getName()
            : model.getClass().getName();
    return spriteCache.computeIfAbsent(className, LibGDXSpriteFactory::create);
  }

  public boolean hasTexture(Model model) {
    LibgdxSprite sprite = getSprite(model);
    return sprite != null && sprite.getTexture(model) != null;
  }

  @Override
  public void dispose() {
    LibgdxSpriteConfigLoader.get().dispose();
  }
}
