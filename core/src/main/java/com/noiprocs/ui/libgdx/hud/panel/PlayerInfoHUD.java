package com.noiprocs.ui.libgdx.hud.panel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Disposable;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.MetricCollector;
import com.noiprocs.core.control.command.InputCommand;
import com.noiprocs.core.model.item.Inventory;
import com.noiprocs.core.model.item.Item;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.resources.RenderResources;
import com.noiprocs.settings.HotbarLocation;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotStyle;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotWidget;
import com.noiprocs.ui.libgdx.hud.widget.StatusEffectsWidget;
import java.util.LinkedList;
import java.util.List;

public class PlayerInfoHUD extends Table {
  private static final int HOTBAR_SIZE = 4;

  private final HealthBarActor healthBar;
  private final StatusEffectsWidget statusEffects;
  private final Label debugLabel;
  private final ItemSlotWidget[] hotbarSlots;
  private final List<Disposable> disposableList = new LinkedList<>();
  private final Table hotbarTable;

  private HotbarLocation hotbarLocation = HotbarLocation.TOP;

  public PlayerInfoHUD(SettingsManager settingsManager) {
    RenderResources renderResources = RenderResources.get();
    BitmapFont font = renderResources.getHudFont();

    this.healthBar = new HealthBarActor();
    this.statusEffects =
        new StatusEffectsWidget(renderResources.getItemTextureManager().getStatusEffectTextures());
    this.hotbarSlots = new ItemSlotWidget[HOTBAR_SIZE];

    Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
    this.hotbarTable = createHotBarPanel();
    this.debugLabel = new Label("", labelStyle);
    this.debugLabel.setVisible(false);

    settingsManager.subscribe(
        () -> {
          HotbarLocation newLocation = settingsManager.getHotbarLocation();
          if (newLocation != hotbarLocation) {
            hotbarLocation = newLocation;
            buildLayout(hotbarLocation);
          }
          debugLabel.setVisible(settingsManager.isDebugMode());
        });

    setFillParent(true);
    buildLayout(HotbarLocation.TOP);
  }

  private Table createHotBarPanel() {
    ItemSlotStyle slotStyle = ItemSlotStyle.createDefault();
    disposableList.add(slotStyle);

    Table panel = new Table();

    for (int i = 0; i < HOTBAR_SIZE; i++) {
      final int slotIndex = i;
      hotbarSlots[i] = new ItemSlotWidget(slotStyle, false);
      hotbarSlots[i].setHotbarSlot(true);
      hotbarSlots[i].addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              GameContext ctx = GameContext.get();
              ctx.controlManager.processInput(
                  new InputCommand(ctx.username, String.valueOf(slotIndex + 1)));
            }
          });
      panel.add(hotbarSlots[i]).size(ItemSlotWidget.DIMENSION, ItemSlotWidget.DIMENSION).pad(1);
    }
    return panel;
  }

  private void buildLayout(HotbarLocation location) {
    clearChildren();
    top().left().pad(10);
    add(healthBar).size(HealthBarActor.BAR_WIDTH, HealthBarActor.BAR_HEIGHT).left();
    row();
    add(statusEffects).left();
    row();
    if (location == HotbarLocation.TOP) {
      add(hotbarTable).left();
      row();
      add(debugLabel).left();
    } else {
      add(debugLabel).left().expandY().top();
      row();
      add(hotbarTable).left().padBottom(10);
    }
  }

  public void update(PlayerModel playerModel) {
    healthBar.setHealth(playerModel.getHealth(), playerModel.getMaxHealth());
    statusEffects.update(playerModel);

    int currentSlot = playerModel.getCurrentInventorySlot();
    Inventory inventory = playerModel.getInventory();
    for (int i = 0; i < HOTBAR_SIZE; i++) {
      Item item = inventory.getItem(i);
      if (item != null) {
        hotbarSlots[i].setItem(item, item.amount);
      } else {
        hotbarSlots[i].setItem(null, 0);
      }
      hotbarSlots[i].setSelected(i == currentSlot);
    }

    if (debugLabel.isVisible()) {
      debugLabel.setText(
          String.format(
              "Position: %s\nFPS: %d\nPing: %d ms\nPayload: %d Bytes",
              playerModel.position,
              Gdx.graphics.getFramesPerSecond(),
              MetricCollector.pingMs.getLast(),
              MetricCollector.packageSizePerClientBytes.getAvg()));
    }
  }

  public void dispose() {
    healthBar.dispose();
    for (Disposable object : disposableList) {
      object.dispose();
    }
  }

  private static class HealthBarActor extends Widget {
    static final float BAR_WIDTH = 188f;
    static final float BAR_HEIGHT = 14f;
    private static final int CORNER_RADIUS = 5;
    private static final Color BG_COLOR = new Color(0.2f, 0.2f, 0.2f, 1f);

    private final Texture roundedTexture;
    private final GlyphLayout glyphLayout = new GlyphLayout();
    private float ratio = 1f;
    private Color barColor = Color.GREEN;
    private int health = 0;
    private int maxHealth = 0;

    HealthBarActor() {
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
      BitmapFont font = RenderResources.get().getHudFont();
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
