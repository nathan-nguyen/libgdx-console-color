package com.noiprocs.ui.libgdx;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.noiprocs.core.GameContext;
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
  protected final char[][] map;
  protected final char[][] colorMap;

  protected GameContext gameContext;
  private HUDManager hudManager;

  public LibGDXGameScreen(int height, int width, int renderRange) {
    this.height = height;
    this.width = width;
    this.renderRange = renderRange;
    this.map = new char[height][width];
    this.colorMap = new char[height][width];
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
  public void setGameContext(GameContext gameContext) {
    this.gameContext = gameContext;
  }

  @Override
  public void render(int delta) {
    Model playerModel = gameContext.modelManager.getModel(gameContext.username);
    // Only render when playerModel is existing
    if (playerModel == null) return;

    // Construct the screen (populate map and colorMap arrays)
    this.constructScreen((PlayerModel) playerModel);
  }

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
      Model model = gameContext.modelManager.getModel(interactAction.targetId);
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

  /**
   * Renders the game screen using libGDX rendering API.
   *
   * @param batch SpriteBatch for rendering
   * @param font BitmapFont for rendering characters
   * @param charWidth Width of a character in pixels (fixed for monospace)
   * @param charHeight Height of a character in pixels
   * @param virtualHeight Virtual screen height (scaled for device)
   */
  public void renderWithBatch(
      SpriteBatch batch, BitmapFont font, float charWidth, float charHeight, float virtualHeight) {
    Model playerModel = gameContext.modelManager.getModel(gameContext.username);
    if (playerModel == null) return;

    // Sync graphical HUD (runs on GL thread)
    syncGraphicalHUD((PlayerModel) playerModel);

    float x = 0;
    float y = virtualHeight;

    // 1. Render map without borders (full height)
    for (int i = 0; i < height; i++) {
      renderMapLineWithColors(batch, font, i, x, y, charWidth);
      y -= charHeight;
    }
  }

  private void renderMapLineWithColors(
      SpriteBatch batch, BitmapFont font, int mapRow, float x, float y, float charWidth) {
    float currentX = x;
    Color originalColor = batch.getColor().cpy();

    // Map content with colors - render each character individually for monospace
    for (int j = 0; j < width; j++) {
      char ch = map[mapRow][j] == 0 ? ' ' : map[mapRow][j];
      char colorChar = colorMap[mapRow][j];

      Color color =
          (colorChar != 0) ? COLOR_CHAR_MAP.getOrDefault(colorChar, Color.WHITE) : Color.WHITE;

      batch.setColor(color);
      renderCharAtPosition(batch, font, ch, currentX, y);
      currentX += charWidth;
    }

    // Restore original color
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

  protected void constructScreen(PlayerModel playerModel) {
    // Get list of visible objects not far from player
    // Render order: Models with smaller posX render first.
    int offsetX = playerModel.position.x - height / 2;
    int offsetY = playerModel.position.y - width / 2;
    this.clearMap();

    List<Model> renderableModelList =
        ((ClientModelManager) gameContext.modelManager)
            .getLocalChunk()
            .flatMap(modelChunk -> modelChunk.map.values().stream())
            .filter(
                model ->
                    model.isVisible
                        && model.position.manhattanDistanceTo(playerModel.position) <= renderRange)
            .sorted(Comparator.comparingInt(u -> u.position.x))
            .collect(Collectors.toList());

    for (Model model : renderableModelList) {
      ConsoleSprite consoleSprite =
          (ConsoleSprite) gameContext.spriteManager.createRenderableObject(model);
      ConsoleTexture consoleTexture = consoleSprite.getTexture(model);
      char[][] texture = consoleTexture.texture;

      int posX = model.position.x - offsetX - consoleTexture.offsetX;
      int posY = model.position.y - offsetY - consoleTexture.offsetY;

      // Main player sprite position is always fixed and does not depend on current model position
      if (model.id.equals(playerModel.id)) {
        posX = height / 2 - consoleTexture.offsetX;
        posY = width / 2 - consoleTexture.offsetY;
      }

      // Check if model has active hurt event and use red color override
      char overrideColor = 0;
      if (gameContext.modelManager.hasActiveEvent(model.id, EventType.HURT)) {
        overrideColor = 'r'; // Red color for hurt models
      }

      logger.debug("Rendering model {}", model);
      this.updateMap(posX, posY, texture, consoleTexture.colorMap, overrideColor);
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

  private void clearMap() {
    for (int i = 0; i < height; ++i) {
      for (int j = 0; j < width; ++j) {
        map[i][j] = 0;
        colorMap[i][j] = 0;
      }
    }
  }

  private void updateMap(
      int posX, int posY, char[][] texture, char[][] spriteColorMap, char overrideColor) {
    for (int i = 0; i < texture.length; ++i) {
      for (int j = 0; j < texture[0].length; ++j) {
        if (texture[i][j] == 0) continue;
        int x = posX + i;
        int y = posY + j;
        if (x >= 0 && x < height && y >= 0 && y < width) {
          map[x][y] = texture[i][j];
          // Use override color if provided, otherwise use sprite's color map
          if (overrideColor != 0) {
            colorMap[x][y] = overrideColor;
          } else {
            colorMap[x][y] = (spriteColorMap != null) ? spriteColorMap[i][j] : 0;
          }
        }
      }
    }
  }
}
