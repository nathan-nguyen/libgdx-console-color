package com.noiprocs.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.noiprocs.resources.ModelTextureLoader.TextureConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads sprite configurations from sprite-config.json, mapping model class names to their texture
 * config and sprite class.
 */
public class SpriteConfigLoader implements Disposable {

  private static final String SPRITE_CONFIG_JSON = "sprite-config.json";

  public static class SpriteEntry {
    public final TextureConfig textureConfig;
    public final Map<String, TextureConfig> namedConfigs;
    public final String spriteClass;

    public SpriteEntry(
        TextureConfig textureConfig, Map<String, TextureConfig> namedConfigs, String spriteClass) {
      this.textureConfig = textureConfig;
      this.namedConfigs = namedConfigs;
      this.spriteClass = spriteClass;
    }
  }

  private final Map<String, SpriteEntry> entries = new HashMap<>();
  private final List<Texture> textures = new ArrayList<>();

  public SpriteConfigLoader() {
    FileHandle jsonFile = Gdx.files.internal(SPRITE_CONFIG_JSON);
    if (!jsonFile.exists()) return;

    JsonValue root = new JsonReader().parse(jsonFile);
    JsonValue sprites = root.get("sprites");
    if (sprites == null) return;

    for (JsonValue entry = sprites.child; entry != null; entry = entry.next) {
      String spriteClass = entry.getString("spriteClass", null);
      Map<String, TextureConfig> namedConfigs = new LinkedHashMap<>();

      JsonValue imagesNode = entry.get("images");
      if (imagesNode != null) {
        for (JsonValue imgEntry = imagesNode.child; imgEntry != null; imgEntry = imgEntry.next) {
          TextureConfig config = loadTextureConfig(imgEntry);
          if (config != null) namedConfigs.put(imgEntry.name, config);
        }
      } else {
        TextureConfig config = loadTextureConfig(entry);
        if (config == null) continue;
        namedConfigs.put("default", config);
      }

      TextureConfig baseConfig =
          namedConfigs.getOrDefault(
              "default", namedConfigs.isEmpty() ? null : namedConfigs.values().iterator().next());
      SpriteEntry spriteEntry = new SpriteEntry(baseConfig, namedConfigs, spriteClass);

      JsonValue modelClasses = entry.get("modelClasses");
      for (JsonValue cn = modelClasses.child; cn != null; cn = cn.next) {
        entries.put(cn.asString(), spriteEntry);
      }
    }
  }

  private TextureConfig loadTextureConfig(JsonValue node) {
    String imagePath = node.getString("imagePath", null);
    if (imagePath == null) return null;
    FileHandle imgFile = Gdx.files.internal(imagePath);
    if (!imgFile.exists()) return null;

    float offsetX = node.getFloat("offsetX", 0f);
    float offsetY = node.getFloat("offsetY", 0f);
    float scaleX = node.getFloat("scaleX", 1f);
    float scaleY = node.getFloat("scaleY", 1f);
    float flippedOffsetX = node.getFloat("flippedOffsetX", offsetX);
    float flippedOffsetY = node.getFloat("flippedOffsetY", offsetY);

    Texture tex = new Texture(imgFile);
    tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    textures.add(tex);
    return new TextureConfig(
        new TextureRegion(tex), offsetX, offsetY, scaleX, scaleY, flippedOffsetX, flippedOffsetY);
  }

  public SpriteEntry getEntry(String className) {
    return entries.get(className);
  }

  @Override
  public void dispose() {
    for (Texture tex : textures) {
      tex.dispose();
    }
    textures.clear();
    entries.clear();
  }
}
