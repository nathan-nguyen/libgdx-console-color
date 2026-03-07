package com.noiprocs.ui.libgdx.renderer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.noiprocs.core.model.Model;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.ui.console.hud.AbstractHitboxDebugRenderer;
import java.util.List;

public class HitboxDebugRenderer extends AbstractHitboxDebugRenderer {
  // Pixel height of the rendering area (viewport world height).
  private final float virtualHeight;
  private final float hw;
  private final float isoOffsetX;

  private ShapeRenderer shapeRenderer;

  public HitboxDebugRenderer(int height, int width, float virtualHeight) {
    super(height, width);
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

    this.shapeRenderer = shapeRenderer;
    renderAll(models, offsetX, offsetY, null);

    shapeRenderer.end();
    Gdx.gl.glDisable(GL20.GL_BLEND);
  }

  private void drawIsoRect(float posX, float posY, float h, float w, Color color) {
    shapeRenderer.setColor(color);
    // 4 corners in grid space: TL=(posX,posY), TR=(posX,posY+w),
    //                          BL=(posX+h,posY), BR=(posX+h,posY+w)
    float tlsx = isoScreenX(posX, posY), tlsy = isoScreenY(posX, posY);
    float trsx = isoScreenX(posX, posY + w), trsy = isoScreenY(posX, posY + w);
    float blsx = isoScreenX(posX + h, posY), blsy = isoScreenY(posX + h, posY);
    float brsx = isoScreenX(posX + h, posY + w), brsy = isoScreenY(posX + h, posY + w);
    shapeRenderer.line(tlsx, tlsy, trsx, trsy);
    shapeRenderer.line(trsx, trsy, brsx, brsy);
    shapeRenderer.line(brsx, brsy, blsx, blsy);
    shapeRenderer.line(blsx, blsy, tlsx, tlsy);
  }

  @Override
  protected void drawBaseHitbox(float posX, float posY, float h, float w) {
    drawIsoRect(posX, posY, h, w, Color.RED);
  }

  @Override
  protected void drawInteractRange(
      float posX, float posY, float dimH, float dimW, float rangeH, float rangeW) {
    drawIsoRect(posX - rangeH, posY - rangeW, dimH + 2 * rangeH, dimW + 2 * rangeW, Color.YELLOW);
  }

  @Override
  protected void drawAttackHitbox(float aX, float aY, float h, float w) {
    drawIsoRect(aX, aY, h, w, Color.ORANGE);
  }
}
