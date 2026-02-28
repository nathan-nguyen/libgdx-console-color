package com.noiprocs.ui.libgdx.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.input.InputController;
import com.noiprocs.resources.RenderResources;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.console.hitbox.ConsoleHitboxManager;
import com.noiprocs.ui.console.sprite.ConsoleSpriteManager;
import com.noiprocs.ui.libgdx.HitboxDebugRenderer;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import com.noiprocs.ui.libgdx.hud.HUDManager;
import com.noiprocs.ui.libgdx.sprite.LibGDXSpriteManager;
import com.noiprocs.ui.libgdx.util.UIStyleHelper;
import com.noiprocs.ui.libgdx.widget.MenuOverlay;
import java.util.function.Consumer;

/**
 * Game screen wrapper that integrates the existing character-grid rendering into the Screen
 * interface. Handles game initialization, input, and rendering.
 */
public class GameScreen implements Screen {
  private final Viewport viewport;
  private final OrthographicCamera camera;
  private final RenderResources renderResources;
  private final InputController inputController;
  private final SettingsManager settingsManager;
  private final Runnable virtualControlsRenderer;
  private final Consumer<GameContext> gameContextRegistrar;
  private final Runnable showMainMenu;
  private final String username;
  private final String hostname;
  private final int port;
  private final String type;
  private final String platform;

  private LibGDXGameScreen gameScreen;
  private HitboxDebugRenderer hitboxDebugRenderer;
  private GameContext gameContext;
  private Thread gameThread;
  private Stage uiStage;
  private Skin skin;
  private MenuOverlay menuOverlay;
  private HUDManager hudManager;

  public GameScreen(
      Viewport viewport,
      OrthographicCamera camera,
      RenderResources renderResources,
      InputController inputController,
      SettingsManager settingsManager,
      Runnable virtualControlsRenderer,
      Consumer<GameContext> gameContextRegistrar,
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
    this.virtualControlsRenderer = virtualControlsRenderer;
    this.gameContextRegistrar = gameContextRegistrar;
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

    int screenHeight = Math.round(virtualHeight / UIConfig.CHAR_SIZE);
    int screenWidth = Math.round(virtualWidth / UIConfig.CHAR_SIZE);
    gameScreen =
        new LibGDXGameScreen(
            screenHeight,
            screenWidth,
            12000,
            new LibGDXSpriteManager(renderResources.getModelTextureLoader()));
    hitboxDebugRenderer = new HitboxDebugRenderer(gameScreen);

    gameContext =
        GameContext.build(
            platform,
            username,
            type,
            hostname,
            port,
            new ConsoleHitboxManager(),
            new ConsoleSpriteManager(),
            gameScreen);

    gameContextRegistrar.accept(gameContext);

    gameThread = new Thread(gameContext::run);
    gameThread.start();

    setupUI();

    hudManager =
        new HUDManager(
            gameScreen,
            viewport,
            renderResources.getPanelFont(),
            renderResources.getHudFont(),
            renderResources.getItemTextureManager(),
            settingsManager,
            this::toggleMenu);
    gameScreen.setHudManager(hudManager);
    menuOverlay.setOnClose(() -> hudManager.setButtonsVisible(true));

    setupInputMultiplexer();
  }

  private void setupUI() {
    uiStage = new Stage(viewport, renderResources.getBatch());
    skin = UIStyleHelper.createSkin(renderResources.getHudFont());

    menuOverlay = new MenuOverlay(settingsManager, showMainMenu, skin);
    uiStage.addActor(menuOverlay);
  }

  private void setupInputMultiplexer() {
    InputMultiplexer multiplexer = new InputMultiplexer();
    if (hudManager != null) {
      multiplexer.addProcessor(hudManager.getHudStage());
    }
    multiplexer.addProcessor(uiStage);
    Gdx.input.setInputProcessor(multiplexer);
  }

  private void toggleMenu() {
    boolean show = !menuOverlay.isVisible();
    menuOverlay.setVisible(show);
    hudManager.setButtonsVisible(!show);
  }

  @Override
  public void render(float delta) {
    if (inputController != null) {
      inputController.handleInput(gameScreen);
    }

    ScreenUtils.clear(0f, 0f, 0f, 1f);

    camera.update();
    renderResources.getBatch().setProjectionMatrix(camera.combined);

    renderResources.getBatch().begin();
    gameScreen.render(0);
    gameScreen.renderWithBatch(
        renderResources.getBatch(),
        renderResources.getFont(),
        UIConfig.CHAR_SIZE,
        UIConfig.CHAR_SIZE,
        viewport.getWorldHeight(),
        settingsManager.isShowWalls());
    renderResources.getBatch().end();

    if (settingsManager.isDebugMode()) {
      renderResources.getShapeRenderer().setProjectionMatrix(camera.combined);
      hitboxDebugRenderer.render(
          renderResources.getShapeRenderer(),
          UIConfig.CHAR_SIZE,
          UIConfig.CHAR_SIZE,
          viewport.getWorldHeight());
    }

    virtualControlsRenderer.run();

    if (hudManager != null) {
      hudManager.render(delta);
    }

    uiStage.act(delta);
    uiStage.draw();
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
        gameThread.join(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      gameThread = null;
    }

    gameContextRegistrar.accept(null);

    if (hudManager != null) {
      hudManager.dispose();
      hudManager = null;
    }
    if (uiStage != null) {
      uiStage.dispose();
    }
    if (skin != null) {
      skin.dispose();
    }

    gameContext = null;
    gameScreen = null;
  }
}
