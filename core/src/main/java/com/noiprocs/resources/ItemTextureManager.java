package com.noiprocs.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages item icon textures. Loads mappings from item-icons.json, keyed by full item class name.
 * Only loads textures for icon files that actually exist in assets.
 */
public class ItemTextureManager implements Disposable {

  private static final String ITEM_ICONS_JSON = "item-icons.json";

  private final Map<String, TextureRegion> textures = new HashMap<>();

  public ItemTextureManager() {
    FileHandle jsonFile = Gdx.files.internal(ITEM_ICONS_JSON);
    if (!jsonFile.exists()) return;

    JsonValue root = new JsonReader().parse(jsonFile);
    for (JsonValue entry = root.child; entry != null; entry = entry.next) {
      String className = entry.name;
      String iconPath = entry.asString();
      FileHandle iconFile = Gdx.files.internal(iconPath);
      if (iconFile.exists()) {
        Texture tex = new Texture(iconFile);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        textures.put(className, new TextureRegion(tex));
      }
    }
  }

  /** Returns the icon texture for the given item, or null if no icon is mapped. */
  public TextureRegion getTexture(Object item) {
    if (item == null) return null;
    return textures.get(item.getClass().getName());
  }

  @Override
  public void dispose() {
    for (TextureRegion region : textures.values()) {
      region.getTexture().dispose();
    }
    textures.clear();
  }
}
