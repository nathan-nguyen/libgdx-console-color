package com.noiprocs.ui.libgdx.sprite;

public class LibgdxRenderContext {
  public final int height;
  public final int width;
  public final float virtualHeight;
  public final AlphaResolver alphaResolver;

  public LibgdxRenderContext(
      int height, int width, float virtualHeight, AlphaResolver alphaResolver) {
    this.height = height;
    this.width = width;
    this.virtualHeight = virtualHeight;
    this.alphaResolver = alphaResolver;
  }
}
