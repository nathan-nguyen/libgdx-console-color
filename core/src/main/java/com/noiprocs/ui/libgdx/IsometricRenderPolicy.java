package com.noiprocs.ui.libgdx;

import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.gameplay.model.environment.MazePartModel;
import com.noiprocs.gameplay.model.environment.WallTrapModel;
import com.noiprocs.gameplay.model.environment.WorldBoundaryHorizontalModel;
import com.noiprocs.gameplay.model.environment.WorldBoundaryVerticalModel;

public class IsometricRenderPolicy {

  public static int isoDepth(Model model) {
    Vector3D dim = GameContext.get().hitboxManager.getHitboxDimension(model);
    return 2 * (model.position.x + model.position.y) + dim.x + dim.y;
  }

  public static boolean useIsometricTexture(Model model) {
    return model instanceof WorldBoundaryVerticalModel
        || model instanceof WorldBoundaryHorizontalModel
        || model instanceof WallTrapModel
        || model instanceof MazePartModel;
  }
}
