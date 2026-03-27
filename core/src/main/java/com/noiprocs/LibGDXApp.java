package com.noiprocs;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.input.InputController;
import com.noiprocs.resources.RenderResources;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.console.ConsoleUIConfig;
import com.noiprocs.ui.libgdx.GameScreen;
import com.noiprocs.ui.libgdx.MainMenuScreen;
import com.noiprocs.ui.libgdx.SettingsScreen;
import com.noiprocs.ui.libgdx.sprite.LibgdxSpriteConfigLoader;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LibGDXApp extends Game {
  // Virtual screen dimensions (scaled for device)
  protected float virtualWidth;
  protected float virtualHeight;

  private OrthographicCamera camera;
  private Viewport viewport;
  private Stage virtualControlsStage;
  private SettingsManager settingsManager;

  // Configuration
  private final String platform;
  private final String type;
  private final InputController inputController;

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

  /** Get the input controller. */
  public InputController getInputController() {
    return inputController;
  }

  /** Set the virtual controls stage (for Android platform). */
  public void setVirtualControlsStage(Stage stage) {
    this.virtualControlsStage = stage;
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
    LibgdxSpriteConfigLoader.reset();

    // Initialize virtual dimensions (can be overridden by subclasses before calling super.create())
    initializeVirtualDimensions();

    // Setup camera and viewport for proper scaling
    camera = new OrthographicCamera();
    camera.setToOrtho(false, virtualWidth, virtualHeight);
    viewport = new FitViewport(virtualWidth, virtualHeight, camera);
    viewport.apply();

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
            viewport,
            camera,
            getInputController(),
            settingsManager,
            virtualControlsStage,
            this::showMainMenu,
            settingsManager.getUsername(),
            settingsManager.getHostname(),
            settingsManager.getPort(),
            type,
            platform));
  }

  /** Navigate to game screen in standalone (single player) mode. */
  public void showStandaloneGame() {
    setScreen(
        new GameScreen(
            viewport,
            camera,
            getInputController(),
            settingsManager,
            virtualControlsStage,
            this::showMainMenu,
            settingsManager.getUsername(),
            settingsManager.getHostname(),
            settingsManager.getPort(),
            "standalone",
            platform));
  }

  @Override
  public void dispose() {
    RenderResources.get().dispose();
  }
}
