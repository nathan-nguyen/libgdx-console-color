package com.noiprocs.ui.libgdx;

/**
 * Determines the render alpha for a model that may occlude the player. Alpha fades smoothly from
 * FULL_ALPHA to OCCLUDED_ALPHA as the player's screen position approaches and enters the model's
 * screen bounding box, avoiding the abrupt flash of a hard cutoff.
 */
public class OcclusionAlphaResolver {

  public static final float FULL_ALPHA = 1f;
  public static final float OCCLUDED_ALPHA = 0.3f;

  /** Pixel radius around the bounding box over which the fade transition occurs. */
  private static final float FADE_DISTANCE = 80f;

  public static float resolve(
      boolean isDeeper,
      float playerScreenX,
      float playerScreenY,
      float minX,
      float maxX,
      float minY,
      float maxY) {
    if (!isDeeper) return FULL_ALPHA;

    // Distance from the player's screen point to the nearest point on the bounding box.
    // Zero when the player is inside the box.
    float dx = Math.max(0, Math.max(minX - playerScreenX, playerScreenX - maxX));
    float dy = Math.max(0, Math.max(minY - playerScreenY, playerScreenY - maxY));
    float dist = (float) Math.sqrt(dx * dx + dy * dy);

    // t=0 → fully inside (OCCLUDED_ALPHA), t=1 → at or beyond FADE_DISTANCE (FULL_ALPHA)
    float t = Math.min(1f, dist / FADE_DISTANCE);
    return OCCLUDED_ALPHA + (FULL_ALPHA - OCCLUDED_ALPHA) * t;
  }
}
