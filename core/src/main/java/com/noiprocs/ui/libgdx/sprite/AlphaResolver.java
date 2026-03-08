package com.noiprocs.ui.libgdx.sprite;

import com.noiprocs.core.model.Model;

@FunctionalInterface
public interface AlphaResolver {
  float resolve(Model model, Model playerModel, float minX, float maxX, float minY, float maxY);
}
