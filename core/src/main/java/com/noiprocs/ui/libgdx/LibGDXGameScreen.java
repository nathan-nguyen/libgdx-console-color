package com.noiprocs.ui.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Config;
import com.noiprocs.core.graphic.GameScreenInterface;
import com.noiprocs.core.model.InventoryContainerInterface;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.action.Action;
import com.noiprocs.core.model.action.InteractAction;
import com.noiprocs.core.model.event.EventType;
import com.noiprocs.core.model.manager.ClientModelManager;
import com.noiprocs.core.model.mob.character.HumanoidModel;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.ui.console.sprite.ConsoleSprite;
import com.noiprocs.ui.console.sprite.ConsoleTexture;
import com.noiprocs.ui.console.util.ColorMapper;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.ui.libgdx.hud.HUDManager;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibGDXGameScreen implements GameScreenInterface {
  private static final Logger logger = LoggerFactory.getLogger(LibGDXGameScreen.class);
  protected final int height;
  protected final int width;
  protected final int renderRange;
  private HUDManager hudManager;

  public LibGDXGameScreen(int height, int width, int renderRange) {
    this.height = height;
    this.width = width;
    this.renderRange = renderRange;
  }

  // Color character to libGDX Color mapping
  private static final Map<Character, Color> COLOR_CHAR_MAP = new HashMap<>();

  static {
    for (Map.Entry<Character, ColorMapper.RGB> entry : ColorMapper.COLOR_MAP.entrySet()) {
      ColorMapper.RGB rgb = entry.getValue();
      COLOR_CHAR_MAP.put(entry.getKey(), new Color(rgb.r / 255f, rgb.g / 255f, rgb.b / 255f, 1f));
    }
  }

  @Override
  public void setGameContext(GameContext gameContext) {}

  @Override
  public void render(int delta) {}

  /**
   * Synchronizes the graphical HUD with the text-based HUD state. When a chest is opened in the
   * text-based system, this automatically opens the graphical inventory HUD.
   */
  private void syncGraphicalHUD(PlayerModel playerModel) {
    if (hudManager == null) {
      return;
    }

    Action playerAction = playerModel.getAction();
    if (playerAction instanceof InteractAction) {
      InteractAction interactAction = (InteractAction) playerAction;
      Model model = GameContext.get().modelManager.getModel(interactAction.targetId);
      if (model instanceof InventoryContainerInterface || isHumanoidButNotPlayer(model)) {
        if (!hudManager.isOpen()) {
          hudManager.openInventoryHUD(interactAction.targetId);
        }
      }
    }
    // Note: Inventory HUD stays open until manually closed (clicking outside or ESC)
    // This matches the Equipment HUD behavior
  }

  private boolean isHumanoidButNotPlayer(Model model) {
    return model instanceof HumanoidModel && !(model instanceof PlayerModel);
  }

  public void renderWithBatch(
      SpriteBatch batch, BitmapFont font, float charWidth, float charHeight, float virtualHeight) {
    GameContext gameContext = GameContext.get();
    Model playerModel = gameContext.modelManager.getModel(gameContext.username);
    if (playerModel == null) return;

    syncGraphicalHUD((PlayerModel) playerModel);

    float offsetX = (float) playerModel.position.x / Config.WORLD_SCALE - height / 2;
    float offsetY = (float) playerModel.position.y / Config.WORLD_SCALE - width / 2;

    List<Model> renderableModelList =
        ((ClientModelManager) gameContext.modelManager)
            .getLocalChunk()
            .flatMap(modelChunk -> modelChunk.map.values().stream())
            .filter(
                model ->
                    model.isVisible
                        && model.position.manhattanDistanceTo(playerModel.position) <= renderRange)
            .sorted(Comparator.comparingInt(u -> u.position.x + u.position.y))
            .collect(Collectors.toList());

    Color originalColor = batch.getColor().cpy();

    for (Model model : renderableModelList) {
      ConsoleSprite consoleSprite =
          (ConsoleSprite) gameContext.spriteManager.createRenderableObject(model);
      ConsoleTexture consoleTexture = consoleSprite.getTexture(model);
      char[][] texture = consoleTexture.texture;

      float posX = (float) model.position.x / Config.WORLD_SCALE - offsetX - consoleTexture.offsetX;
      float posY = (float) model.position.y / Config.WORLD_SCALE - offsetY - consoleTexture.offsetY;

      if (model.id.equals(playerModel.id)) {
        posX = height / 2 - consoleTexture.offsetX;
        posY = width / 2 - consoleTexture.offsetY;
      }

      char overrideColor = 0;
      if (gameContext.modelManager.hasActiveEvent(model.id, EventType.HURT)) {
        overrideColor = 'r';
      }

      logger.debug("Rendering model {}", model);

      boolean isoTexture = IsometricRenderPolicy.useIsometricTexture(model);
      float baseScreenX = (posY - posX) * charWidth / 2f + charWidth * (width + height) / 4f;
      float baseScreenY =
          virtualHeight / 2f + charHeight / 4f * ((height + width) / 2f - posX - posY);

      for (int i = 0; i < texture.length; i++) {
        for (int j = 0; j < texture[0].length; j++) {
          if (texture[i][j] == 0) continue;
          float screenX, screenY;
          if (isoTexture) {
            float x = posX + i;
            float y = posY + j;
            screenX = (y - x) * charWidth / 2f + charWidth * (width + height) / 4f;
            screenY = virtualHeight / 2f + charHeight / 4f * ((height + width) / 2f - x - y);
          } else {
            screenX = baseScreenX + j * UIConfig.CHAR_WIDTH;
            screenY = baseScreenY - i * UIConfig.CHAR_HEIGHT;
          }
          if (screenX >= 0 && screenX < width * charWidth && screenY >= 0 && screenY < virtualHeight) {
            char colorChar =
                overrideColor != 0
                    ? overrideColor
                    : (consoleTexture.colorMap != null ? consoleTexture.colorMap[i][j] : 0);
            Color color =
                (colorChar != 0)
                    ? COLOR_CHAR_MAP.getOrDefault(colorChar, Color.WHITE)
                    : Color.WHITE;
            batch.setColor(color);
            renderCharAtPosition(batch, font, texture[i][j], screenX, screenY);
          }
        }
      }
    }

    batch.setColor(originalColor);
  }

  private void renderCharAtPosition(SpriteBatch batch, BitmapFont font, char ch, float x, float y) {
    if (ch == ' ' || ch == 0) {
      return; // Don't render spaces
    }

    BitmapFont.Glyph glyph = font.getData().getGlyph(ch);
    if (glyph != null && glyph.page < font.getRegions().size) {
      float scale = font.getData().scaleX;
      float glyphX = x + glyph.xoffset * scale;
      float glyphY = y + glyph.yoffset * scale;

      batch.draw(
          font.getRegions().get(glyph.page).getTexture(),
          Math.round(glyphX),
          Math.round(glyphY),
          glyph.width * scale,
          glyph.height * scale,
          glyph.u,
          glyph.v,
          glyph.u2,
          glyph.v2);
    }
  }

  /**
   * Sets the HUD manager for this game screen.
   *
   * @param hudManager HUDManager instance
   */
  public void setHudManager(HUDManager hudManager) {
    this.hudManager = hudManager;
  }

  /**
   * Gets the HUD manager.
   *
   * @return HUDManager instance or null
   */
  public HUDManager getHudManager() {
    return hudManager;
  }
}
