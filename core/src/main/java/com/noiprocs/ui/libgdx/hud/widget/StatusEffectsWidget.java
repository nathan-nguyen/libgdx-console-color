package com.noiprocs.ui.libgdx.hud.widget;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.noiprocs.core.model.effect.EffectInterface;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.gameplay.model.effect.DamageBoostEffect;
import com.noiprocs.gameplay.model.effect.MaxHealthBoostEffect;
import com.noiprocs.gameplay.model.effect.SpeedBoostEffect;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class StatusEffectsWidget extends Table {

  private static final int ICON_SIZE = 28;

  private final LinkedHashMap<Class<? extends EffectInterface>, Texture> effectTextures;
  private Set<Class<? extends EffectInterface>> activeEffects = Collections.emptySet();

  public StatusEffectsWidget() {
    effectTextures = new LinkedHashMap<>();
    effectTextures.put(
        DamageBoostEffect.class,
        new Texture(Gdx.files.internal("icons/damage_enhance_effect.png")));
    effectTextures.put(
        MaxHealthBoostEffect.class,
        new Texture(Gdx.files.internal("icons/health_ehance_effect.png")));
    effectTextures.put(
        SpeedBoostEffect.class, new Texture(Gdx.files.internal("icons/speed_enhance_effect.png")));
  }

  public void update(PlayerModel playerModel) {
    Set<Class<? extends EffectInterface>> newActive = new HashSet<>();
    for (Class<? extends EffectInterface> effectClass : effectTextures.keySet()) {
      if (playerModel.hasEffect(effectClass)) {
        newActive.add(effectClass);
      }
    }
    if (newActive.equals(activeEffects)) return;

    activeEffects = newActive;
    clearChildren();
    for (Map.Entry<Class<? extends EffectInterface>, Texture> entry : effectTextures.entrySet()) {
      if (activeEffects.contains(entry.getKey())) {
        add(new Image(entry.getValue())).size(ICON_SIZE, ICON_SIZE).pad(2);
      }
    }
  }

  public void dispose() {
    for (Texture texture : effectTextures.values()) {
      texture.dispose();
    }
  }
}
