package com.noiprocs.ui.libgdx.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.noiprocs.core.model.item.Item;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.settings.SettingsManager;

public class PlayerInfoHUD extends Table {

  private final Label nameLabel;
  private final HealthBarActor healthBar;
  private final Label inventoryLabel;

  public PlayerInfoHUD(BitmapFont font) {
    Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
    this.nameLabel = new Label("", style);
    this.healthBar = new HealthBarActor(font);
    this.inventoryLabel = new Label("", style);

    setFillParent(true);
    top().left().pad(5);
    add(nameLabel).left();
    add(healthBar).size(HealthBarActor.BAR_WIDTH, HealthBarActor.BAR_HEIGHT).padLeft(6).left();
    row();
    add(inventoryLabel).left().colspan(2);
  }

  public void update(PlayerModel playerModel, SettingsManager settingsManager) {
    boolean debug = settingsManager != null && settingsManager.isDebugMode();

    StringBuilder name = new StringBuilder(playerModel.id);
    if (debug) {
      name.append(" - [").append(playerModel.position).append("]");
    }
    nameLabel.setText(name.toString());

    healthBar.setHealth(playerModel.getHealth(), playerModel.getMaxHealth());

    StringBuilder inv = new StringBuilder("Inventory: [");
    Item item = playerModel.getHoldingItem();
    if (item != null) {
      inv.append(item.name).append(": ").append(item.amount);
    }
    inv.append("]");
    if (debug) {
      inv.append("  FPS: ").append(Gdx.graphics.getFramesPerSecond());
    }
    inventoryLabel.setText(inv.toString());
  }

  public void dispose() {
    healthBar.dispose();
  }

  private static class HealthBarActor extends Widget {
    static final float BAR_WIDTH = 188f;
    static final float BAR_HEIGHT = 14f;
    private static final int CORNER_RADIUS = 5;
    private static final Color BG_COLOR = new Color(0.2f, 0.2f, 0.2f, 1f);

    private final Texture roundedTexture;
    private final BitmapFont font;
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private float ratio = 1f;
    private Color barColor = Color.GREEN;
    private int health = 0;
    private int maxHealth = 0;

    HealthBarActor(BitmapFont font) {
      this.font = font;
      this.roundedTexture =
          createRoundedRectTexture((int) BAR_WIDTH, (int) BAR_HEIGHT, CORNER_RADIUS);
    }

    private static Texture createRoundedRectTexture(int width, int height, int radius) {
      Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
      pixmap.setColor(0, 0, 0, 0);
      pixmap.fill();
      pixmap.setColor(Color.WHITE);
      pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
      pixmap.fillRectangle(0, radius, width, height - 2 * radius);
      pixmap.fillCircle(radius, radius, radius);
      pixmap.fillCircle(width - radius, radius, radius);
      pixmap.fillCircle(radius, height - radius, radius);
      pixmap.fillCircle(width - radius, height - radius, radius);
      Texture texture = new Texture(pixmap);
      pixmap.dispose();
      return texture;
    }

    void setHealth(int health, int maxHealth) {
      this.health = health;
      this.maxHealth = maxHealth;
      ratio = maxHealth > 0 ? (float) health / maxHealth : 0f;
      if (ratio <= 0.3f) {
        barColor = Color.RED;
      } else if (ratio <= 0.7f) {
        barColor = Color.YELLOW;
      } else {
        barColor = Color.GREEN;
      }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
      float x = getX(), y = getY();

      batch.setColor(BG_COLOR);
      batch.draw(roundedTexture, x, y, BAR_WIDTH, BAR_HEIGHT);

      float fillWidth = BAR_WIDTH * ratio;
      if (fillWidth > 0) {
        batch.setColor(barColor);
        batch.draw(roundedTexture, x, y, fillWidth, BAR_HEIGHT, 0, 0, fillWidth / BAR_WIDTH, 1);
      }

      String label = health + " / " + maxHealth;
      glyphLayout.setText(font, label);
      float textX = x + (BAR_WIDTH - glyphLayout.width) / 2f;
      float textY = y + (BAR_HEIGHT + glyphLayout.height) / 2f;
      font.setColor(Color.WHITE);
      font.draw(batch, label, textX, textY);

      batch.setColor(Color.WHITE);
    }

    public void dispose() {
      roundedTexture.dispose();
    }

    @Override
    public float getPrefWidth() {
      return BAR_WIDTH;
    }

    @Override
    public float getPrefHeight() {
      return BAR_HEIGHT;
    }
  }
}
