package com.noiprocs.ui.libgdx.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Config;
import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.action.AttackAction;
import com.noiprocs.core.model.action.InteractAction;
import com.noiprocs.core.model.action.NearestAction;
import com.noiprocs.core.model.mob.MobModel;
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
      shapeRenderer.setColor(Color.RED);
      float tlsx = isoScreenX(posX, posY), tlsy = isoScreenY(posX, posY);
      float trsx = isoScreenX(posX, posY + dimY), trsy = isoScreenY(posX, posY + dimY);
      float blsx = isoScreenX(posX + dimX, posY), blsy = isoScreenY(posX + dimX, posY);
      float brsx = isoScreenX(posX + dimX, posY + dimY),
          brsy = isoScreenY(posX + dimX, posY + dimY);

      shapeRenderer.line(tlsx, tlsy, trsx, trsy);
      shapeRenderer.line(trsx, trsy, brsx, brsy);
      shapeRenderer.line(brsx, brsy, blsx, blsy);
      shapeRenderer.line(blsx, blsy, tlsx, tlsy);

      if (model instanceof MobModel) {
        MobModel mob = (MobModel) model;
        if (mob.getAction() instanceof NearestAction || mob.getAction() instanceof InteractAction) {
          float rangeX = (float) NearestAction.INTERACT_RANGE.x / Config.WORLD_SCALE;
          float rangeY = (float) NearestAction.INTERACT_RANGE.y / Config.WORLD_SCALE;
          float iaX = posX - rangeX;
          float iaY = posY - rangeY;
          float iaDimX = dimX + 2 * rangeX;
          float iaDimY = dimY + 2 * rangeY;

          shapeRenderer.setColor(Color.YELLOW);
          float itlsx = isoScreenX(iaX, iaY), itlsy = isoScreenY(iaX, iaY);
          float itrsx = isoScreenX(iaX, iaY + iaDimY), itrsy = isoScreenY(iaX, iaY + iaDimY);
          float iblsx = isoScreenX(iaX + iaDimX, iaY), iblsy = isoScreenY(iaX + iaDimX, iaY);
          float ibrsx = isoScreenX(iaX + iaDimX, iaY + iaDimY),
              ibrsy = isoScreenY(iaX + iaDimX, iaY + iaDimY);

          shapeRenderer.line(itlsx, itlsy, itrsx, itrsy);
          shapeRenderer.line(itrsx, itrsy, ibrsx, ibrsy);
          shapeRenderer.line(ibrsx, ibrsy, iblsx, iblsy);
          shapeRenderer.line(iblsx, iblsy, itlsx, itlsy);
        } else if (mob.getAction() instanceof AttackAction) {
          AttackAction attack = (AttackAction) mob.getAction();
          float aDimX = (float) attack.hitboxDimension.x / Config.WORLD_SCALE;
          float aDimY = (float) attack.hitboxDimension.y / Config.WORLD_SCALE;
          Vector3D facing = mob.getFacingDirection();
          if (facing.x != 0) {
            float tmp = aDimX;
            aDimX = aDimY;
            aDimY = tmp;
          }
          float xOffset = facing.x > 0 ? dimX : (facing.x < 0 ? -aDimX : 0);
          float yOffset = facing.y > 0 ? dimY : (facing.y < 0 ? -aDimY : 0);
          float aX = posX + (float) attack.distance.x / Config.WORLD_SCALE + xOffset;
          float aY = posY + (float) attack.distance.y / Config.WORLD_SCALE + yOffset;

          shapeRenderer.setColor(Color.ORANGE);
          float atlsx = isoScreenX(aX, aY), atlsy = isoScreenY(aX, aY);
          float atrsx = isoScreenX(aX, aY + aDimY), atrsy = isoScreenY(aX, aY + aDimY);
          float ablsx = isoScreenX(aX + aDimX, aY), ablsy = isoScreenY(aX + aDimX, aY);
          float abrsx = isoScreenX(aX + aDimX, aY + aDimY),
              abrsy = isoScreenY(aX + aDimX, aY + aDimY);

          shapeRenderer.line(atlsx, atlsy, atrsx, atrsy);
          shapeRenderer.line(atrsx, atrsy, abrsx, abrsy);
          shapeRenderer.line(abrsx, abrsy, ablsx, ablsy);
          shapeRenderer.line(ablsx, ablsy, atlsx, atlsy);
        }
      }
    }

    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }
}
