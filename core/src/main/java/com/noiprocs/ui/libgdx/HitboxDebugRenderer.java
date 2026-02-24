package com.noiprocs.ui.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Config;
import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import java.util.List;

public class HitboxDebugRenderer {
  private final LibGDXGameScreen gameScreen;

  public HitboxDebugRenderer(LibGDXGameScreen gameScreen) {
    this.gameScreen = gameScreen;
  }

  public void render(
      ShapeRenderer shapeRenderer, float charWidth, float charHeight, float virtualHeight) {
    GameContext gameContext = GameContext.get();
    Model playerModel = gameContext.modelManager.getModel(gameContext.username);
    if (playerModel == null) return;

    float offsetX = gameScreen.getOffsetX(playerModel);
    float offsetY = gameScreen.getOffsetY(playerModel);
    List<Model> models = gameScreen.getRenderableModels(playerModel);

    float hw = (gameScreen.height + gameScreen.width) / 2f;
    float isoOffsetX = charWidth * (gameScreen.width + gameScreen.height) / 4f;

    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
    shapeRenderer.setColor(Color.RED);
    Gdx.gl.glLineWidth(2f);

    for (Model model : models) {
      Vector3D dimension = gameContext.hitboxManager.getHitboxDimension(model);
      if (dimension.x == 0 && dimension.y == 0) continue;

      float posX = (float) model.position.x / Config.WORLD_SCALE - offsetX;
      float posY = (float) model.position.y / Config.WORLD_SCALE - offsetY;
      float dimX = (float) dimension.x / Config.WORLD_SCALE;
      float dimY = (float) dimension.y / Config.WORLD_SCALE;

      // 4 corners of the hitbox in grid space: (gridX, gridY)
      // TL = (posX, posY), TR = (posX, posY+dimY)
      // BL = (posX+dimX, posY), BR = (posX+dimX, posY+dimY)
      float tlsx = (posY - posX) * charWidth / 2f + isoOffsetX;
      float tlsy = virtualHeight / 2f + charHeight / 4f * (hw - posX - posY);

      float trsx = (posY + dimY - posX) * charWidth / 2f + isoOffsetX;
      float trsy = virtualHeight / 2f + charHeight / 4f * (hw - posX - (posY + dimY));

      float blsx = (posY - (posX + dimX)) * charWidth / 2f + isoOffsetX;
      float blsy = virtualHeight / 2f + charHeight / 4f * (hw - (posX + dimX) - posY);

      float brsx = (posY + dimY - (posX + dimX)) * charWidth / 2f + isoOffsetX;
      float brsy = virtualHeight / 2f + charHeight / 4f * (hw - (posX + dimX) - (posY + dimY));

      shapeRenderer.line(tlsx, tlsy, trsx, trsy);
      shapeRenderer.line(trsx, trsy, brsx, brsy);
      shapeRenderer.line(brsx, brsy, blsx, blsy);
      shapeRenderer.line(blsx, blsy, tlsx, tlsy);
    }

    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }
}
