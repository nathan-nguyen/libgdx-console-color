package com.noiprocs.ui.libgdx.sprite.mob;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.noiprocs.core.model.DurableModel;
import com.noiprocs.core.model.Model;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;
import com.noiprocs.ui.libgdx.sprite.SpriteRenderContext;

public class MobSprite extends LibgdxSprite {
  private static final float BAR_HEIGHT = 3f;
  private static final float BAR_GAP = 2f;

  private static TextureRegion whitePixel;

  public MobSprite(LibgdxTexture texture) {
    super(texture);
  }

  private static TextureRegion getWhitePixel() {
    if (whitePixel == null) {
      Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
      pixmap.setColor(1f, 1f, 1f, 1f);
      pixmap.fill();
      whitePixel = new TextureRegion(new Texture(pixmap));
      pixmap.dispose();
    }
    return whitePixel;
  }

  @Override
  public void render(
      SpriteBatch batch,
      Model model,
      Model playerModel,
      float offsetX,
      float offsetY,
      SpriteRenderContext ctx) {
    super.render(batch, model, playerModel, offsetX, offsetY, ctx);
    renderHealthBar(batch, model, offsetX, offsetY, ctx);
  }

  private void renderHealthBar(
      SpriteBatch batch, Model model, float offsetX, float offsetY, SpriteRenderContext ctx) {
    DurableModel durableModel = (DurableModel) model;
    int health = durableModel.getHealth();
    int maxHealth = durableModel.getMaxHealth();
    if (health >= maxHealth) return;

    float[] b = screenBounds(getTexture(model), model, offsetX, offsetY, ctx);
    float screenX = b[0], screenY = b[1], imgW = b[2], imgH = b[3];

    float barWidth = imgW * 0.7f;
    float barX = screenX + (imgW - barWidth) / 2f;
    float barY = screenY + imgH + BAR_GAP;
    float ratio = maxHealth > 0 ? (float) health / maxHealth : 0f;
    float fillWidth = barWidth * ratio;
    Color barColor;
    if (ratio <= 0.3f) {
      barColor = Color.RED;
    } else if (ratio <= 0.7f) {
      barColor = Color.YELLOW;
    } else {
      barColor = Color.GREEN;
    }

    TextureRegion pixel = getWhitePixel();
    batch.setColor(0.3f, 0f, 0f, 1f);
    batch.draw(pixel, barX, barY, barWidth, BAR_HEIGHT);
    batch.setColor(barColor);
    batch.draw(pixel, barX, barY, fillWidth, BAR_HEIGHT);
    batch.setColor(1f, 1f, 1f, 1f);
  }
}
