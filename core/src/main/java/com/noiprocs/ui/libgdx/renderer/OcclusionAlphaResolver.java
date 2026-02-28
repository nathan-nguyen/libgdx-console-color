package com.noiprocs.ui.libgdx.renderer;

import com.noiprocs.core.model.Model;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.settings.SettingsManager;

/**
 * Determines the render alpha for a model that may occlude the player. When occlude is enabled, any
 * model that is isometrically deeper (x+y greater) than the player may have reduced alpha. Alpha
 * fades smoothly from FULL_ALPHA to OCCLUDED_ALPHA as the player's screen position approaches and
 * enters the model's screen bounding box, avoiding the abrupt flash of a hard cutoff.
 */
public class OcclusionAlphaResolver {

  public final float FULL_ALPHA = 1f;
  public final float OCCLUDED_ALPHA = 0.3f;
  private final SettingsManager settingsManager;

  /** Pixel radius around the bounding box over which the fade transition occurs. */
  private final float FADE_DISTANCE = 2f;

  public OcclusionAlphaResolver(SettingsManager settingsManager) {
    this.settingsManager = settingsManager;
  }

  private boolean isDeeper(Model model, Model playerModel) {
    return IsometricRenderPolicy.isoDepth(model) > IsometricRenderPolicy.isoDepth(playerModel);
  }

  private float playerScreenX(int width) {
    return UIConfig.CHAR_SIZE * width / 2f;
  }

  private float playerScreenY(float virtualHeight) {
    return virtualHeight / 2f;
  }

  public float resolve(
      Model model,
      Model playerModel,
      int width,
      float virtualHeight,
      float minX,
      float maxX,
      float minY,
      float maxY) {
    if (!settingsManager.isOcclude() || !isDeeper(model, playerModel)) return FULL_ALPHA;

    float playerScreenX = playerScreenX(width);
    float playerScreenY = playerScreenY(virtualHeight);

    // Distance from the player's screen point to the nearest point on the bounding box.
    // Zero when the player is inside the box.
    float dx = Math.max(0, Math.max(minX - playerScreenX, playerScreenX - maxX));
    float dy = Math.max(0, Math.max(minY - playerScreenY, playerScreenY - maxY));
    float dist = (float) Math.sqrt(dx * dx + dy * dy);

    // t=0 → fully inside (OCCLUDED_ALPHA), t=1 → at or beyond FADE_DISTANCE (FULL_ALPHA)
    float t = Math.min(1f, dist / FADE_DISTANCE);
    return OCCLUDED_ALPHA + (FULL_ALPHA - OCCLUDED_ALPHA) * t;
  }

  public float resolve(
      Model model,
      Model playerModel,
      char[][] texture,
      boolean isoTexture,
      float posX,
      float posY,
      float baseScreenX,
      float baseScreenY,
      int width,
      int height,
      float virtualHeight) {
    if (texture.length == 0 || texture[0].length == 0) return FULL_ALPHA;

    int rows = texture.length;
    int cols = texture[0].length;
    float minX, maxX, minY, maxY;
    if (isoTexture) {
      float isoOffset = UIConfig.CHAR_SIZE * (width + height) / 4f;
      float hw = (height + width) / 2f;
      minX = (posY - posX - (rows - 1)) * UIConfig.CHAR_SIZE / 2f + isoOffset;
      maxX = (posY + (cols - 1) - posX) * UIConfig.CHAR_SIZE / 2f + isoOffset;
      maxY = virtualHeight / 2f + UIConfig.CHAR_SIZE / 4f * (hw - posX - posY);
      minY =
          virtualHeight / 2f
              + UIConfig.CHAR_SIZE / 4f * (hw - posX - (rows - 1) - posY - (cols - 1));
    } else {
      minX = baseScreenX;
      maxX = baseScreenX + cols * UIConfig.CHAR_WIDTH;
      minY = baseScreenY - rows * UIConfig.CHAR_HEIGHT;
      maxY = baseScreenY;
    }
    return resolve(model, playerModel, width, virtualHeight, minX, maxX, minY, maxY);
  }
}
