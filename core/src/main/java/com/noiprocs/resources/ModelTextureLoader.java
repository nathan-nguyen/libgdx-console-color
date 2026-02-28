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
 * Loads image textures for models from model-textures.json, keyed by full model class name. Each
 * entry configures the image path and rendering parameters.
 */
public class ModelTextureLoader implements Disposable {

  private static final String MODEL_TEXTURES_JSON = "model-textures.json";

  public static class TextureConfig {
    public final TextureRegion textureRegion;
    public final float offsetX;
    public final float offsetY;
    public final float scaleX;
    public final float scaleY;
    public final float flippedOffsetX;
    public final float flippedOffsetY;

    public TextureConfig(
        TextureRegion textureRegion, float offsetX, float offsetY, float scaleX, float scaleY) {
      this(textureRegion, offsetX, offsetY, scaleX, scaleY, offsetX, offsetY);
    }

    public TextureConfig(
        TextureRegion textureRegion,
        float offsetX,
        float offsetY,
        float scaleX,
        float scaleY,
        float flippedOffsetX,
        float flippedOffsetY) {
      this.textureRegion = textureRegion;
      this.offsetX = offsetX;
      this.offsetY = offsetY;
      this.scaleX = scaleX;
      this.scaleY = scaleY;
      this.flippedOffsetX = flippedOffsetX;
      this.flippedOffsetY = flippedOffsetY;
    }

    public TextureConfig flipped() {
      TextureRegion flippedRegion = new TextureRegion(textureRegion);
      flippedRegion.flip(true, false);
      return new TextureConfig(flippedRegion, flippedOffsetX, flippedOffsetY, scaleX, scaleY);
    }
  }

  private final Map<String, TextureConfig> configs = new HashMap<>();

  public ModelTextureLoader() {
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

  /** Returns the texture config for the given class name, or null if none is configured. */
  public TextureConfig getConfig(String className) {
    return configs.get(className);
  }

  @Override
  public void dispose() {
    for (TextureConfig config : configs.values()) {
      config.textureRegion.getTexture().dispose();
    }
    configs.clear();
  }
}
