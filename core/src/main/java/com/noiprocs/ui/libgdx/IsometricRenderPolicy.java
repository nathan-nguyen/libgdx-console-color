package com.noiprocs.ui.libgdx;

import com.noiprocs.core.model.Model;
import com.noiprocs.gameplay.model.environment.MazePartModel;
import com.noiprocs.gameplay.model.environment.WallTrapModel;
import com.noiprocs.gameplay.model.environment.WorldBoundaryHorizontalModel;
import com.noiprocs.gameplay.model.environment.WorldBoundaryVerticalModel;

public class IsometricRenderPolicy {

  public static boolean useIsometricTexture(Model model) {
    return model instanceof WorldBoundaryVerticalModel
        || model instanceof WorldBoundaryHorizontalModel
        || model instanceof WallTrapModel
        || model instanceof MazePartModel;
  }
}
