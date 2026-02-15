package com.noiprocs;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.input.InputController;
import com.noiprocs.resources.FontGenerator;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.console.ConsoleUIConfig;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import com.noiprocs.ui.menu.GameScreen;
import com.noiprocs.ui.menu.MainMenuScreen;
import com.noiprocs.ui.menu.SettingsScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LibGDXApp extends Game {
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
  private SettingsManager settingsManager;

  // Configuration
  private final String platform;
  private final String type;

  public LibGDXApp(String platform, String type, InputController inputController) {
    this.platform = platform;
    this.type = type;
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

  /** Set the game screen (called by GameScreen wrapper). */
  public void setGameScreen(LibGDXGameScreen gameScreen) {
    this.gameScreen = gameScreen;
  }

  /** Get the game context. */
  public GameContext getGameContext() {
    return gameContext;
  }

  /** Set the game context (called by GameScreen wrapper). */
  public void setGameContext(GameContext gameContext) {
    this.gameContext = gameContext;
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

    // Initialize settings manager
    settingsManager = new SettingsManager();

    // Launch main menu screen
    setScreen(new MainMenuScreen(this));
  }

  /** Get the settings manager. */
  public SettingsManager getSettingsManager() {
    return settingsManager;
  }

  /** Navigate to main menu screen. */
  public void showMainMenu() {
    setScreen(new MainMenuScreen(this));
  }

  /** Navigate to settings screen. */
  public void showSettings() {
    setScreen(new SettingsScreen(this));
  }

  /** Navigate to game screen with current settings. */
  public void showGame() {
    setScreen(
        new GameScreen(
            this,
            settingsManager.getUsername(),
            settingsManager.getHostname(),
            settingsManager.getPort(),
            type,
            platform));
  }

  /** Render virtual controls (called by GameScreen for Android). */
  public void renderVirtualControls() {
    if (renderVirtualControls != null) {
      renderVirtualControls.run();
    }
  }

  @Override
  public void dispose() {
    batch.dispose();
    font.dispose();
    // Game thread will be terminated when application exits
  }
}
