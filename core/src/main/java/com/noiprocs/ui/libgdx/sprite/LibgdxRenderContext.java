package com.noiprocs.ui.libgdx.sprite;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class LibgdxRenderContext {
  public final int height;
  public final int width;
  public final float virtualHeight;
  public final AlphaResolver alphaResolver;
  public final ShapeRenderer shapeRenderer;

  public LibgdxRenderContext(
      int height,
      int width,
      float virtualHeight,
      AlphaResolver alphaResolver,
      ShapeRenderer shapeRenderer) {
    this.height = height;
    this.width = width;
    this.virtualHeight = virtualHeight;
    this.alphaResolver = alphaResolver;
    this.shapeRenderer = shapeRenderer;
  }
}
