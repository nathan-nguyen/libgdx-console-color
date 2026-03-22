package com.noiprocs.ui.libgdx.sprite.mob.projectile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Config;
import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.gameplay.model.mob.projectile.BombModel;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.ui.libgdx.sprite.LibgdxRenderContext;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;

public class BombSprite extends LibgdxSprite {
  private static final String MODEL_CLASS = BombModel.class.getName();

  private static final Color FILL_COLOR = new Color(1f, 0.3f, 0f, 0.15f);
  private static final Color OUTLINE_COLOR = new Color(1f, 0.3f, 0f, 0.8f);
  private static final Color FLASH_FILL_COLOR = new Color(1f, 0.6f, 0f, 0.5f);
  private static final Color FLASH_OUTLINE_COLOR = new Color(1f, 0.6f, 0f, 1f);
  private static final int FLASH_THRESHOLD = 15;

  public BombSprite() {
    super(loadTexture(MODEL_CLASS, "default"));
  }

  @Override
  public void render(
      SpriteBatch batch,
      Model model,
      Model playerModel,
      float offsetX,
      float offsetY,
      LibgdxRenderContext ctx) {
    batch.end();
    drawExplosionArea((BombModel) model, batch, ctx, offsetX, offsetY);
    batch.begin();
    super.render(batch, model, playerModel, offsetX, offsetY, ctx);
  }

  // A Chebyshev-distance area in world space is a square, which projects isometrically to a
  // diamond.
  // The 4 world corners (±R, ±R) project to: top=(cx, cy+extY), right=(cx+extX, cy),
  // bottom=(cx, cy-extY), left=(cx-extX, cy), where extX = r*CHAR_SIZE, extY = r*CHAR_SIZE/2.
  private void drawExplosionArea(
      BombModel bomb, SpriteBatch batch, LibgdxRenderContext ctx, float offsetX, float offsetY) {
    float hw = (ctx.height + ctx.width) / 2f;
    float isoOffsetX = UIConfig.CHAR_SIZE * hw / 2f;

    Vector3D center = GameContext.get().hitboxManager.getHitboxCenter(bomb);
    float gx = (float) center.x / Config.WORLD_SCALE - offsetX;
    float gy = (float) center.y / Config.WORLD_SCALE - offsetY;
    float cx = (gy - gx) * UIConfig.CHAR_SIZE / 2f + isoOffsetX;
    float cy = ctx.virtualHeight / 2f + UIConfig.CHAR_SIZE / 4f * (hw - gx - gy);

    float r = (float) bomb.getExplosionRadius() / Config.WORLD_SCALE;
    float extX = r * UIConfig.CHAR_SIZE;
    float extY = r * UIConfig.CHAR_SIZE / 2f;

    float[] diamond = {cx, cy + extY, cx + extX, cy, cx, cy - extY, cx - extX, cy};

    int countdown = bomb.getExplosionCountdown();
    boolean flashBright = false;
    if (countdown <= FLASH_THRESHOLD) {
      long flashPeriodMs = Math.max(60L, (long) countdown * 40);
      flashBright = (System.currentTimeMillis() / (flashPeriodMs / 2)) % 2 == 0;
    }
    Color fillColor = flashBright ? FLASH_FILL_COLOR : FILL_COLOR;
    Color outlineColor = flashBright ? FLASH_OUTLINE_COLOR : OUTLINE_COLOR;

    ShapeRenderer sr = ctx.shapeRenderer;
    sr.setProjectionMatrix(batch.getProjectionMatrix());

    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

    sr.begin(ShapeRenderer.ShapeType.Filled);
    sr.setColor(fillColor);
    sr.triangle(diamond[0], diamond[1], diamond[2], diamond[3], diamond[4], diamond[5]);
    sr.triangle(diamond[0], diamond[1], diamond[4], diamond[5], diamond[6], diamond[7]);
    sr.end();

    sr.begin(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glLineWidth(2f);
    sr.setColor(outlineColor);
    sr.polygon(diamond);
    sr.end();

    Gdx.gl.glDisable(GL20.GL_BLEND);
  }
}
