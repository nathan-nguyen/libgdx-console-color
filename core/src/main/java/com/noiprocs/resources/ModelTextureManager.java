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
 * Manages image textures for model rendering. Loads mappings from model-textures.json, keyed by
 * full model class name. Each entry configures the image path and rendering parameters.
 */
public class ModelTextureManager implements Disposable {

  private static final String MODEL_TEXTURES_JSON = "model-textures.json";

  public static class TextureConfig {
    public final TextureRegion textureRegion;
    public final float offsetX;
    public final float offsetY;
    public final float scaleX;
    public final float scaleY;

    public TextureConfig(
        TextureRegion textureRegion, float offsetX, float offsetY, float scaleX, float scaleY) {
      this.textureRegion = textureRegion;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.scaleX = scaleX;
      this.scaleY = scaleY;
    }
  }

  private final Map<String, TextureConfig> configs = new HashMap<>();

  public ModelTextureManager() {
    FileHandle jsonFile = Gdx.files.internal(MODEL_TEXTURES_JSON);
    if (!jsonFile.exists()) return;

    JsonValue root = new JsonReader().parse(jsonFile);
    for (JsonValue entry = root.child; entry != null; entry = entry.next) {
      String className = entry.name;
      String imagePath = entry.getString("imagePath");
      float offsetX = entry.getFloat("offsetX", 0f);
      float offsetY = entry.getFloat("offsetY", 0f);
      float scaleX = entry.getFloat("scaleX", 1f);
      float scaleY = entry.getFloat("scaleY", 1f);

      FileHandle imgFile = Gdx.files.internal(imagePath);
      if (imgFile.exists()) {
        Texture tex = new Texture(imgFile);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        configs.put(
            className, new TextureConfig(new TextureRegion(tex), offsetX, offsetY, scaleX, scaleY));
      }
    }
  }

  /** Returns the texture config for the given model instance, or null if none is configured. */
  public TextureConfig getConfig(Object model) {
    if (model == null) return null;
    return configs.get(model.getClass().getName());
  }

  @Override
  public void dispose() {
    for (TextureConfig config : configs.values()) {
      config.textureRegion.getTexture().dispose();
    }
    configs.clear();
  }
}
