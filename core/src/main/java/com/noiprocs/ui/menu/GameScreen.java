package com.noiprocs.ui.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.ScreenUtils;
import com.noiprocs.LibGDXApp;
import com.noiprocs.core.GameContext;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.ui.console.hitbox.ConsoleHitboxManager;
import com.noiprocs.ui.console.sprite.ConsoleSpriteManager;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;

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

    int screenHeight =
        Math.round(virtualHeight / UIConfig.CHAR_HEIGHT) - 4; // 4: 2 border + 2 player info
    int screenWidth = Math.round(virtualWidth / UIConfig.CHAR_WIDTH) - 2; // 2: 2 borders
    gameScreen = new LibGDXGameScreen(screenHeight, screenWidth, 120);

    // Register game screen with app for virtual controls (Android)
    app.setGameScreen(gameScreen);

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

    // Set input processor to null (InputController polls directly)
    Gdx.input.setInputProcessor(null);
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
    app.setGameScreen(null);
    app.setGameContext(null);

    // Cleanup game resources
    gameContext = null;
    gameScreen = null;
  }
}
