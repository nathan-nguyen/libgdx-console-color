package com.noiprocs.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.noiprocs.core.model.effect.EffectInterface;
import com.noiprocs.gameplay.model.effect.DamageBoostEffect;
import com.noiprocs.gameplay.model.effect.MaxHealthBoostEffect;
import com.noiprocs.gameplay.model.effect.SpeedBoostEffect;
import com.noiprocs.gameplay.model.effect.SpeedSlowEffect;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages item icon textures. Loads mappings from item-icons.json, keyed by full item class name.
 * Only loads textures for icon files that actually exist in assets.
 */
public class ItemTextureManager implements Disposable {

  private static final String ITEM_ICONS_JSON = "item-icons.json";

  private static final Map<String, String> EQUIPMENT_SLOT_ICON_PATHS = new HashMap<>();

  static {
    EQUIPMENT_SLOT_ICON_PATHS.put("HELMET", "icons/helmet_slot.png");
    EQUIPMENT_SLOT_ICON_PATHS.put("CHEST PLATE", "icons/chest_plate_slot.png");
    EQUIPMENT_SLOT_ICON_PATHS.put("LEGGING", "icons/legging_slot.png");
    EQUIPMENT_SLOT_ICON_PATHS.put("BOOT", "icons/boots_slot.png");
  }

  private final Map<String, TextureRegion> textures = new HashMap<>();
  private final Map<String, TextureRegion> slotTextures = new HashMap<>();
  private final LinkedHashMap<Class<? extends EffectInterface>, Texture> effectTextures =
      new LinkedHashMap<>();

  public ItemTextureManager() {
    FileHandle jsonFile = Gdx.files.internal(ITEM_ICONS_JSON);
    if (jsonFile.exists()) {
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

    for (Map.Entry<String, String> entry : EQUIPMENT_SLOT_ICON_PATHS.entrySet()) {
      FileHandle file = Gdx.files.internal(entry.getValue());
      if (file.exists()) {
        Texture tex = new Texture(file);
        tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        slotTextures.put(entry.getKey(), new TextureRegion(tex));
      }
    }

    effectTextures.put(
        DamageBoostEffect.class, ResourceLoader.loadTexture(GameResource.ICON_EFFECT_DAMAGE));
    effectTextures.put(
        MaxHealthBoostEffect.class, ResourceLoader.loadTexture(GameResource.ICON_EFFECT_HEALTH));
    effectTextures.put(
        SpeedBoostEffect.class, ResourceLoader.loadTexture(GameResource.ICON_EFFECT_SPEED));
    effectTextures.put(
        SpeedSlowEffect.class, ResourceLoader.loadTexture(GameResource.ICON_EFFECT_WEB_SLOW));
  }

  public Map<Class<? extends EffectInterface>, Texture> getStatusEffectTextures() {
    return Collections.unmodifiableMap(effectTextures);
  }

  /** Returns the icon texture for the given item, or null if no icon is mapped. */
  public TextureRegion getTexture(Object item) {
    if (item == null) return null;
    return textures.get(item.getClass().getName());
  }

  /** Returns the icon texture for the given item class, or null if no icon is mapped. */
  public TextureRegion getTextureByClass(Class<?> itemClass) {
    if (itemClass == null) return null;
    return textures.get(itemClass.getName());
  }

  /** Returns the placeholder texture for an equipment slot type (e.g. "HELMET"), or null. */
  public TextureRegion getEquipmentSlotTexture(String slotType) {
    return slotTextures.get(slotType);
  }

  @Override
  public void dispose() {
    for (TextureRegion region : textures.values()) {
      region.getTexture().dispose();
    }
    textures.clear();
    for (TextureRegion region : slotTextures.values()) {
      region.getTexture().dispose();
    }
    slotTextures.clear();
    for (Texture texture : effectTextures.values()) {
      texture.dispose();
    }
    effectTextures.clear();
  }
}
