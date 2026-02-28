package com.noiprocs.ui.libgdx.sprite;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class LibgdxTexture {
  public final TextureRegion textureRegion;
  public final float offsetX;
  public final float offsetY;
  public final float scaleX;
  public final float scaleY;
  public final float flippedOffsetX;
  public final float flippedOffsetY;

  public LibgdxTexture(
      TextureRegion textureRegion, float offsetX, float offsetY, float scaleX, float scaleY) {
    this(textureRegion, offsetX, offsetY, scaleX, scaleY, offsetX, offsetY);
  }

  public LibgdxTexture(
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

  public LibgdxTexture flipped() {
    TextureRegion flippedRegion = new TextureRegion(textureRegion);
    flippedRegion.flip(true, false);
    return new LibgdxTexture(flippedRegion, flippedOffsetX, flippedOffsetY, scaleX, scaleY);
  }
}
