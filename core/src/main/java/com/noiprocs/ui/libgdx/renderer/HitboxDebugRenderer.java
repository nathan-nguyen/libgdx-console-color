package com.noiprocs.ui.libgdx.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Config;
import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.resources.UIConfig;
import java.util.List;

public class HitboxDebugRenderer {
  // Pixel height of the rendering area (viewport world height).
  private final float virtualHeight;
  private final float hw;
  private final float isoOffsetX;

  public HitboxDebugRenderer(int height, int width, float virtualHeight) {
    this.virtualHeight = virtualHeight;
    this.hw = (height + width) / 2f;
    this.isoOffsetX = UIConfig.CHAR_SIZE * hw / 2f;
  }

  private float isoScreenX(float gridX, float gridY) {
    return (gridY - gridX) * UIConfig.CHAR_SIZE / 2f + isoOffsetX;
  }

  private float isoScreenY(float gridX, float gridY) {
    return virtualHeight / 2f + UIConfig.CHAR_SIZE / 4f * (hw - gridX - gridY);
  }

  public void render(
      ShapeRenderer shapeRenderer, List<Model> models, float offsetX, float offsetY) {
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(Color.RED);
    Gdx.gl.glLineWidth(2f);

    GameContext gameContext = GameContext.get();
    for (Model model : models) {
      Vector3D dimension = gameContext.hitboxManager.getHitboxDimension(model);
      if (dimension.x == 0 && dimension.y == 0) continue;

      float posX = (float) model.position.x / Config.WORLD_SCALE - offsetX;
      float posY = (float) model.position.y / Config.WORLD_SCALE - offsetY;
      float dimX = (float) dimension.x / Config.WORLD_SCALE;
      float dimY = (float) dimension.y / Config.WORLD_SCALE;

      // 4 corners in grid space: TL=(posX,posY), TR=(posX,posY+dimY),
      //                          BL=(posX+dimX,posY), BR=(posX+dimX,posY+dimY)
      float tlsx = isoScreenX(posX, posY), tlsy = isoScreenY(posX, posY);
      float trsx = isoScreenX(posX, posY + dimY), trsy = isoScreenY(posX, posY + dimY);
      float blsx = isoScreenX(posX + dimX, posY), blsy = isoScreenY(posX + dimX, posY);
      float brsx = isoScreenX(posX + dimX, posY + dimY),
          brsy = isoScreenY(posX + dimX, posY + dimY);

      shapeRenderer.line(tlsx, tlsy, trsx, trsy);
      shapeRenderer.line(trsx, trsy, brsx, brsy);
      shapeRenderer.line(brsx, brsy, blsx, blsy);
      shapeRenderer.line(blsx, blsy, tlsx, tlsy);
    }

    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }
}
