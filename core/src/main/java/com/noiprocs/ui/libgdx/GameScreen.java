package com.noiprocs.ui.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.GameMode;
import com.noiprocs.core.common.Config;
import com.noiprocs.core.graphic.GameScreenInterface;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.manager.ServerModelManager;
import com.noiprocs.input.InputController;
import com.noiprocs.resources.RenderResources;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.console.hitbox.ConsoleHitboxManager;
import com.noiprocs.ui.console.sprite.ConsoleSpriteManager;
import com.noiprocs.ui.libgdx.hud.HUDManager;
import com.noiprocs.ui.libgdx.renderer.BackgroundRenderer;
import com.noiprocs.ui.libgdx.renderer.ConsoleCharRenderer;
import com.noiprocs.ui.libgdx.renderer.HitboxDebugRenderer;
import com.noiprocs.ui.libgdx.renderer.IsometricRenderPolicy;
import com.noiprocs.ui.libgdx.renderer.LibgdxTextureRenderer;
import com.noiprocs.ui.libgdx.renderer.OcclusionAlphaResolver;
import com.noiprocs.ui.libgdx.sprite.LibgdxRenderContext;
import com.noiprocs.ui.libgdx.widget.MenuOverlay;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Game screen wrapper that integrates the existing character-grid rendering into the Screen
 * interface. Handles game initialization, input, and rendering.
 */
public class GameScreen implements Screen {
  private static final int RENDER_RANGE = 12000;
  // Maintains virtual resolution (BASE_VIRTUAL_WIDTH × BASE_VIRTUAL_HEIGHT) regardless of window
  // size, letterboxing/pillarboxing to preserve aspect ratio.
  private final Viewport viewport;
  // Orthographic projection matrix applied to the SpriteBatch and ShapeRenderer each frame.
  private final OrthographicCamera camera;
  // Shared GPU resources (SpriteBatch, fonts, ShapeRenderer, item textures) owned by LibGDXApp.
  private final RenderResources renderResources;
  private final InputController inputController;
  private final SettingsManager settingsManager;
  private final Stage virtualControlsStage;
  private final Runnable showMainMenu;

  private final String username;
  private final String hostname;
  private final int port;
  private final String type;
  private final String platform;

  private int screenHeight;
  private int screenWidth;
  private BackgroundRenderer backgroundRenderer;
  private LibgdxTextureRenderer libgdxTextureRenderer;
  private ConsoleCharRenderer consoleCharRenderer;
  private HitboxDebugRenderer hitboxDebugRenderer;
  private GameContext gameContext;
  private Thread gameThread;
  private MenuOverlay menuOverlay;
  private HUDManager hudManager;

  public GameScreen(
      Viewport viewport,
      OrthographicCamera camera,
      RenderResources renderResources,
      InputController inputController,
      SettingsManager settingsManager,
      Stage virtualControlsStage,
      Runnable showMainMenu,
      String username,
      String hostname,
      int port,
      String type,
      String platform) {
    this.viewport = viewport;
    this.camera = camera;
    this.renderResources = renderResources;
    this.inputController = inputController;
    this.settingsManager = settingsManager;
    this.virtualControlsStage = virtualControlsStage;
    this.showMainMenu = showMainMenu;
    this.username = username;
    this.hostname = hostname;
    this.port = port;
    this.type = type;
    this.platform = platform;
  }

  @Override
  public void show() {
    float virtualHeight = viewport.getWorldHeight();
    float virtualWidth = viewport.getWorldWidth();

    // screenHeight/screenWidth are in tile units (virtual pixels / CHAR_SIZE).
    screenHeight = Math.round(virtualHeight / UIConfig.CHAR_SIZE);
    screenWidth = Math.round(virtualWidth / UIConfig.CHAR_SIZE);
    backgroundRenderer = new BackgroundRenderer(virtualWidth, virtualHeight);

    OcclusionAlphaResolver occlusionAlphaResolver = new OcclusionAlphaResolver(settingsManager);
    LibgdxRenderContext renderContext =
        new LibgdxRenderContext(
            screenHeight,
            screenWidth,
            virtualHeight,
            (model, playerModel, minX, maxX, minY, maxY) ->
                occlusionAlphaResolver.resolve(
                    model, playerModel, screenWidth, virtualHeight, minX, maxX, minY, maxY),
            renderResources.getShapeRenderer());
    libgdxTextureRenderer = new LibgdxTextureRenderer(renderContext, settingsManager);
    consoleCharRenderer =
        new ConsoleCharRenderer(screenHeight, screenWidth, virtualHeight, occlusionAlphaResolver);
    hitboxDebugRenderer = new HitboxDebugRenderer(screenHeight, screenWidth, virtualHeight);

    gameContext =
        GameContext.build(
            platform,
            username,
            type,
            hostname,
            port,
            Gdx.files.getLocalStoragePath() + "world",
            new ConsoleHitboxManager(),
            new ConsoleSpriteManager(),
            // Rendering is driven by libgdx's render loop directly; these callbacks are unused.
            new GameScreenInterface() {
              @Override
              public void setGameContext(GameContext gameContext) {}

              @Override
              public void render(int i) {}
            });

    gameThread = new Thread(gameContext::run);
    gameThread.start();

    setupUI();

    hudManager =
        new HUDManager(
            viewport,
            renderResources.getPanelFont(),
            renderResources.getHudFont(),
            renderResources.getItemTextureManager(),
            settingsManager,
            this::toggleMenu);
    menuOverlay.setOnClose(() -> hudManager.setButtonsVisible(true));

    setupInputMultiplexer();
  }

  private void setupUI() {
    menuOverlay =
        new MenuOverlay(
            settingsManager,
            showMainMenu,
            renderResources.getHudFont(),
            viewport,
            renderResources.getBatch());
  }

  // HUD stage is first so it consumes touch events before the menu overlay sees them.
  private void setupInputMultiplexer() {
    InputMultiplexer multiplexer = new InputMultiplexer();
    if (hudManager != null) {
      multiplexer.addProcessor(hudManager.getHudStage());
    }
    multiplexer.addProcessor(menuOverlay.getStage());
    Gdx.input.setInputProcessor(multiplexer);
  }

  // Keeps menu visibility and HUD action buttons mutually exclusive.
  private void toggleMenu() {
    boolean show = !menuOverlay.isVisible();
    menuOverlay.setVisible(show);
    hudManager.setButtonsVisible(!show);
  }

  @Override
  public void render(float delta) {
    if (inputController != null) {
      inputController.handleInput(hudManager);
    }

    ScreenUtils.clear(0f, 0f, 0f, 1f);
    camera.update();
    SpriteBatch batch = renderResources.getBatch();
    batch.setProjectionMatrix(camera.combined);
    renderWorld(batch);

    if (virtualControlsStage != null) {
      virtualControlsStage.act(delta);
      virtualControlsStage.draw();
    }
    hudManager.render(delta);
    menuOverlay.render(delta);
  }

  private void renderWorld(SpriteBatch batch) {
    Model playerModel = GameContext.get().modelManager.getModel(username);
    if (playerModel == null) return;

    // Convert player world position to tile offset so the player is centered on screen.
    float offsetX = (float) playerModel.position.x / Config.WORLD_SCALE - screenHeight / 2f;
    float offsetY = (float) playerModel.position.y / Config.WORLD_SCALE - screenWidth / 2f;
    List<Model> renderableModels = getRenderableModels(playerModel);

    batch.begin();
    backgroundRenderer.render(batch, offsetX, offsetY);
    // Renderers may tint the batch color; restore it after the loop.
    Color originalColor = batch.getColor().cpy();
    for (Model model : renderableModels) {
      if (libgdxTextureRenderer.canRender(model)) {
        libgdxTextureRenderer.render(batch, model, playerModel, offsetX, offsetY);
      } else {
        consoleCharRenderer.render(
            batch, renderResources.getFont(), model, playerModel, offsetX, offsetY);
      }
    }
    batch.setColor(originalColor);
    batch.end();

    if (settingsManager.isDebugMode()) {
      renderResources.getShapeRenderer().setProjectionMatrix(camera.combined);
      hitboxDebugRenderer.render(
          renderResources.getShapeRenderer(), renderableModels, offsetX, offsetY);
    }
  }

  // Returns visible models within RENDER_RANGE of the player, sorted front-to-back by isometric
  // depth so that closer objects are drawn on top of farther ones.
  private List<Model> getRenderableModels(Model playerModel) {
    return GameContext.get()
        .modelManager
        .getSurroundedChunk(playerModel)
        .flatMap(modelChunk -> modelChunk.map.values().stream())
        .filter(
            model ->
                model.isVisible
                    && model.position.manhattanDistanceTo(playerModel.position) <= RENDER_RANGE)
        .sorted(Comparator.comparingInt(IsometricRenderPolicy::isoDepth))
        .collect(Collectors.toList());
  }

  @Override
  public void resize(int width, int height) {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {
    dispose();
  }

  @Override
  public void dispose() {
    if (gameThread != null && gameThread.isAlive()) {
      gameThread.interrupt();
      try {
        gameThread.join(1000); // wait up to 1 s for graceful shutdown
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      gameThread = null;
    }

    if (gameContext != null && gameContext.gameMode == GameMode.STANDALONE) {
      ((ServerModelManager) gameContext.modelManager).saveGameData();
    }

    if (hudManager != null) {
      hudManager.dispose();
      hudManager = null;
    }
    if (menuOverlay != null) {
      menuOverlay.dispose();
    }

    if (backgroundRenderer != null) {
      backgroundRenderer.dispose();
      backgroundRenderer = null;
    }
    gameContext = null;
    libgdxTextureRenderer = null;
  }
}
