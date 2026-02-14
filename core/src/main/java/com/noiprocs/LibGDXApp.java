package com.noiprocs;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.input.InputController;
import com.noiprocs.resources.FontGenerator;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.ui.console.ConsoleUIConfig;
import com.noiprocs.ui.console.hitbox.ConsoleHitboxManager;
import com.noiprocs.ui.console.sprite.ConsoleSpriteManager;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LibGDXApp extends ApplicationAdapter {
  // Virtual screen dimensions (scaled for device)
  protected float virtualWidth;
  protected float virtualHeight;

  private SpriteBatch batch;
  private BitmapFont font;
  private LibGDXGameScreen gameScreen;
  private GameContext gameContext;
  private final InputController inputController;
  private OrthographicCamera camera;
  private Viewport viewport;
  private Runnable renderVirtualControls; // For Android touch controls

  // Configuration from command line
  private final String platform;
  private final String username;
  private final String type;
  private final String hostname;
  private final int port;

  public LibGDXApp(
      String platform,
      String username,
      String type,
      String hostname,
      int port,
      InputController inputController) {
    this.platform = platform;
    this.username = username;
    this.type = type;
    this.hostname = hostname;
    this.port = port;
    this.inputController = inputController;
  }

  /** Get the viewport for coordinate conversion (used by input controllers). */
  public Viewport getViewport() {
    return viewport;
  }

  /** Get the camera for rendering virtual controls. */
  public OrthographicCamera getCamera() {
    return camera;
  }

  /** Get the sprite batch for rendering. */
  public SpriteBatch getBatch() {
    return batch;
  }

  /** Get the font for rendering text. */
  public BitmapFont getFont() {
    return font;
  }

  /** Get the game screen for HUD state access. */
  public LibGDXGameScreen getGameScreen() {
    return gameScreen;
  }

  /** Get the game context. */
  public GameContext getGameContext() {
    return gameContext;
  }

  /** Get the input controller. */
  public InputController getInputController() {
    return inputController;
  }

  /** Set the virtual control renderer callback (for Android platform). */
  public void setVirtualControlsRenderer(Runnable renderer) {
    this.renderVirtualControls = renderer;
  }

  /**
   * Initializes virtual dimensions. Can be overridden by subclasses for platform-specific scaling.
   * Default implementation uses base dimensions.
   */
  protected void initializeVirtualDimensions() {
    virtualWidth = UIConfig.BASE_VIRTUAL_WIDTH;
    virtualHeight = UIConfig.BASE_VIRTUAL_HEIGHT;
  }

  @Override
  public void create() {
    // Initialize virtual dimensions (can be overridden by subclasses before calling super.create())
    initializeVirtualDimensions();

    // Setup camera and viewport for proper scaling
    camera = new OrthographicCamera();
    camera.setToOrtho(false, virtualWidth, virtualHeight);
    viewport = new FitViewport(virtualWidth, virtualHeight, camera);
    viewport.apply();

    batch = new SpriteBatch();

    // Generate a monospace font using FreeType
    FontGenerator fontGenerator = new FontGenerator();
    font = fontGenerator.generateMonospaceFont();
    ConsoleUIConfig.CLEAR_SCREEN = false;

    // Calculate game screen dimensions based on virtual screen size and character size
    int screenHeight =
        Math.round(virtualHeight / UIConfig.CHAR_HEIGHT) - 4; // 4: 2 border + 2 player info
    int screenWidth = Math.round(virtualWidth / UIConfig.CHAR_WIDTH) - 2; // 2: 2 borders
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

    // Start game thread
    Thread gameThread = new Thread(gameContext::run);
    gameThread.start();
  }

  @Override
  public void render() {
    // Handle input
    handleInput();

    // Clear screen
    ScreenUtils.clear(0f, 0f, 0f, 1f);

    // Update camera
    camera.update();
    batch.setProjectionMatrix(camera.combined);

    // Render game screen
    batch.begin();
    gameScreen.render(0);
    gameScreen.renderWithBatch(
        batch, font, UIConfig.CHAR_WIDTH, UIConfig.CHAR_HEIGHT, virtualHeight);
    batch.end();

    // Render virtual controls if available (Android only)
    if (renderVirtualControls != null) {
      renderVirtualControls.run();
    }
  }

  private void handleInput() {
    // Delegate to platform-specific input controller
    if (inputController != null) {
      inputController.handleInput(gameContext, gameScreen);
    }
  }

  @Override
  public void resize(int width, int height) {
    // Update viewport when screen size changes (important for Android)
    viewport.update(width, height);
  }

  @Override
  public void dispose() {
    batch.dispose();
    font.dispose();
    // Game thread will be terminated when application exits
  }
}
