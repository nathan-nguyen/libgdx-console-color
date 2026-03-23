package com.noiprocs.ui.libgdx.hud.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.noiprocs.core.model.effect.AbstractTtlEffect;
import com.noiprocs.core.model.effect.EffectInterface;
import com.noiprocs.core.model.mob.character.PlayerModel;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class StatusEffectsWidget extends Table {

  private static final int ICON_SIZE = 28;
  private static final int FLASH_TTL_THRESHOLD = 360;
  private static final float FLASH_SPEED = (float) (Math.PI * 2); // 1 Hz

  private final Map<Class<? extends EffectInterface>, Texture> effectTextures;
  private final LinkedHashMap<Class<? extends EffectInterface>, Image> activeImages =
      new LinkedHashMap<>();
  private final Map<Class<? extends EffectInterface>, Integer> activeTtls = new HashMap<>();

  private float flashTime = 0f;

  public StatusEffectsWidget(Map<Class<? extends EffectInterface>, Texture> effectTextures) {
    this.effectTextures = effectTextures;
  }

  @SuppressWarnings("unchecked")
  public void update(PlayerModel playerModel) {
    flashTime += Gdx.graphics.getDeltaTime();
    float flashAlpha = 0.55f + 0.45f * (float) Math.sin(flashTime * FLASH_SPEED);

    activeTtls.clear();
    for (EffectInterface effect : playerModel.getEffects()) {
      Class<? extends EffectInterface> effectClass =
          (Class<? extends EffectInterface>) effect.getClass();
      if (!effectTextures.containsKey(effectClass) || !(effect instanceof AbstractTtlEffect))
        continue;
      int ttl = ((AbstractTtlEffect) effect).getTtl();
      activeTtls.put(effectClass, ttl);
      Image img = activeImages.get(effectClass);
      if (img != null) {
        img.getColor().a = ttl <= FLASH_TTL_THRESHOLD ? flashAlpha : 1f;
      }
    }

    if (!activeTtls.keySet().equals(activeImages.keySet())) {
      clearChildren();
      activeImages.clear();
      for (Map.Entry<Class<? extends EffectInterface>, Texture> entry : effectTextures.entrySet()) {
        if (activeTtls.containsKey(entry.getKey())) {
          Image img = new Image(entry.getValue());
          int ttl = activeTtls.get(entry.getKey());
          img.getColor().a = ttl <= FLASH_TTL_THRESHOLD ? flashAlpha : 1f;
          add(img).size(ICON_SIZE, ICON_SIZE).pad(2);
          activeImages.put(entry.getKey(), img);
        }
      }
    }
  }
}
