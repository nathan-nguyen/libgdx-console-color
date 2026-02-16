package com.noiprocs.ui.libgdx.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.noiprocs.LibGDXApp;
import com.noiprocs.core.GameContext;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.ui.console.hitbox.ConsoleHitboxManager;
import com.noiprocs.ui.console.sprite.ConsoleSpriteManager;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import com.noiprocs.ui.libgdx.hud.HUDManager;
import com.noiprocs.ui.libgdx.util.UIStyleHelper;
import com.noiprocs.ui.libgdx.widget.MenuOverlay;

/**
 * Game screen wrapper that integrates the existing character-grid rendering into the Screen
 * interface. Handles game initialization, input, and rendering.
 */
public class GameScreen implements Screen {
  private final LibGDXApp app;
  private final String username;
  private final String hostname;
  private final int port;
  private final String type;
  private final String platform;

  private LibGDXGameScreen gameScreen;
  private GameContext gameContext;
  private Thread gameThread;
  private Stage uiStage;
  private Skin skin;
  private MenuOverlay menuOverlay;
  private Table buttonTable;
  private HUDManager hudManager;

  public GameScreen(
      LibGDXApp app, String username, String hostname, int port, String type, String platform) {
    this.app = app;
    this.username = username;
    this.hostname = hostname;
    this.port = port;
    this.type = type;
    this.platform = platform;
  }

  @Override
  public void show() {
    // Calculate game screen dimensions based on virtual screen size and character size
    float virtualHeight = app.getViewport().getWorldHeight();
    float virtualWidth = app.getViewport().getWorldWidth();

    int screenHeight = Math.round(virtualHeight / UIConfig.CHAR_HEIGHT);
    int screenWidth = Math.round(virtualWidth / UIConfig.CHAR_WIDTH);
    gameScreen = new LibGDXGameScreen(screenHeight, screenWidth, 120);

    // Initialize gameContext
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

    // Register game context with app for input controllers
    app.setGameContext(gameContext);

    // Start game thread
    gameThread = new Thread(gameContext::run);
    gameThread.start();

    setupUI();

    // Create graphical HUD manager
    hudManager = new HUDManager(gameContext, gameScreen, app.getViewport(), app.getFont());
    gameScreen.setHudManager(hudManager);

    // Pass settings manager to game screen for debug mode
    gameScreen.setSettingsManager(app.getSettingsManager());

    // Setup input multiplexer with HUD stage priority
    setupInputMultiplexer();
  }

  private void setupUI() {
    uiStage = new Stage(app.getViewport(), app.getBatch());
    skin = UIStyleHelper.createSkin(app.getFont());

    TextButton menuButton = new TextButton("!!!", skin);
    menuButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            toggleMenu();
          }
        });

    TextButton equipmentButton = new TextButton("E", skin);
    equipmentButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            openEquipmentHUD();
          }
        });

    buttonTable = new Table();
    buttonTable.setFillParent(true);
    buttonTable.top().right().pad(10);
    buttonTable.add(equipmentButton).size(50, 50).padRight(10);
    buttonTable.add(menuButton).size(50, 50);
    uiStage.addActor(buttonTable);

    menuOverlay = new MenuOverlay(app, skin);
    menuOverlay.setOnClose(() -> buttonTable.setVisible(true));
    uiStage.addActor(menuOverlay);

    // NOTE: InputMultiplexer will be updated after hudManager is created
    // Moved to show() method after hudManager initialization
  }

  private void openEquipmentHUD() {
    if (hudManager != null) {
      hudManager.openEquipmentHUD();
    }
  }

  private void setupInputMultiplexer() {
    InputMultiplexer multiplexer = new InputMultiplexer();
    // Priority 1: Graphical HUD stage (if open, captures all input)
    if (hudManager != null) {
      multiplexer.addProcessor(hudManager.getHudStage());
    }
    // Priority 2: UI stage (menu overlay and buttons)
    multiplexer.addProcessor(uiStage);
    Gdx.input.setInputProcessor(multiplexer);
  }

  private void toggleMenu() {
    boolean show = !menuOverlay.isVisible();
    menuOverlay.setVisible(show);
    buttonTable.setVisible(!show);
  }

  @Override
  public void render(float delta) {
    // Handle input
    if (app.getInputController() != null) {
      app.getInputController().handleInput(gameContext, gameScreen);
    }

    // Clear screen
    ScreenUtils.clear(0f, 0f, 0f, 1f);

    // Update camera
    app.getCamera().update();
    app.getBatch().setProjectionMatrix(app.getCamera().combined);

    // Render game screen
    app.getBatch().begin();
    gameScreen.render(0);
    gameScreen.renderWithBatch(
        app.getBatch(),
        app.getFont(),
        UIConfig.CHAR_WIDTH,
        UIConfig.CHAR_HEIGHT,
        app.getViewport().getWorldHeight());
    app.getBatch().end();

    // Render virtual controls if available (Android only)
    app.renderVirtualControls();

    // Render graphical HUD (if open)
    if (hudManager != null && hudManager.isOpen()) {
      hudManager.render(delta);
    }

    // Render UI overlay
    uiStage.act(delta);
    uiStage.draw();
  }

  @Override
  public void resize(int width, int height) {
    // Viewport is managed by LibGDXApp and updated by Game base class
  }

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
    // Stop game thread
    if (gameThread != null && gameThread.isAlive()) {
      gameThread.interrupt();
      try {
        gameThread.join(1000); // Wait up to 1 second for thread to stop
      } catch (InterruptedException e) {
        // Thread interrupted while waiting
        Thread.currentThread().interrupt();
      }
      gameThread = null;
    }

    // Clear game screen and context references in app
    app.setGameContext(null);

    // Cleanup resources
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
