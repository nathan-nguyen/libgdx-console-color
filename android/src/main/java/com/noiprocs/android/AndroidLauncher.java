package com.noiprocs.android;

import android.os.Bundle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.noiprocs.LibGDXApp;
import com.noiprocs.resources.UIConfig;

/** Launches the Android application with touch controls. */
public class AndroidLauncher extends AndroidApplication {
  private static final String PLATFORM = "android";
  private static final String TYPE = "client";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AndroidApplicationConfiguration configuration = new AndroidApplicationConfiguration();
    configuration.useImmersiveMode = true;

    // Get parameters from intent or use defaults
    String username = "player";
    String hostname = "192.168.1.3";
    int port = 8080;

    // Create AndroidApp which extends LibGDXApp with touch controls
    AndroidApp app = new AndroidApp(PLATFORM, username, TYPE, hostname, port);

    initialize(app, configuration);
  }
}

/**
 * Android-specific extension of LibGDXApp that adds touch controls.
 */
class AndroidApp extends LibGDXApp {
  private TouchInputController touchInputController;
  private VirtualControlRenderer virtualControlRenderer;

  public AndroidApp(String platform, String username, String type, String hostname, int port) {
    super(platform, username, type, hostname, port, null);
  }

  @Override
  protected void initializeVirtualDimensions() {
    // Get actual screen dimensions
    float screenWidth = Gdx.graphics.getWidth();
    float screenHeight = Gdx.graphics.getHeight();

    // Calculate aspect ratios
    float baseAspectRatio = UIConfig.BASE_VIRTUAL_HEIGHT / UIConfig.BASE_VIRTUAL_WIDTH;
    float screenAspectRatio = screenHeight / screenWidth;

    if (screenAspectRatio > baseAspectRatio) {
      // Screen is taller - fit width, scale height
      virtualWidth = UIConfig.BASE_VIRTUAL_WIDTH;
      virtualHeight = UIConfig.BASE_VIRTUAL_WIDTH * screenAspectRatio;
    } else {
      // Screen is wider - fit height, scale width
      virtualHeight = UIConfig.BASE_VIRTUAL_HEIGHT;
      virtualWidth = UIConfig.BASE_VIRTUAL_HEIGHT / screenAspectRatio;
    }

    // Initialize control zone positions based on scaled virtual dimensions
    ControlZone.initializePositions(virtualWidth, virtualHeight);
  }

  @Override
  public void create() {
    super.create();

    // Now that viewport is created, we can initialize touch controls
    touchInputController = new TouchInputController(getViewport());
    virtualControlRenderer = new VirtualControlRenderer();

    // Set up the virtual controls renderer callback
    setVirtualControlsRenderer(this::renderTouchControls);
  }

  private void renderTouchControls() {
    virtualControlRenderer.setProjectionMatrix(getCamera().combined);
    TouchState touchState = touchInputController.getTouchState();

    if (touchState.isHudMode()) {
      // Render HUD navigation controls
      TouchState.HudType hudType = touchState.getHudType(getGameScreen());
      boolean showEquipmentAction = hudType == TouchState.HudType.CHEST;
      boolean showQuickSlots = hudType == TouchState.HudType.EQUIPMENT;
      virtualControlRenderer.renderHudControls(
          touchState.getActiveZones(), getBatch(), getFont(), showEquipmentAction, showQuickSlots);
    } else {
      // Render game controls
      virtualControlRenderer.renderGameControls(
          touchState.getActiveZones(), getBatch(), getFont(), touchState);
    }
  }

  @Override
  public void render() {
    // Handle touch input before rendering
    if (touchInputController != null) {
      touchInputController.handleInput(getGameContext(), getGameScreen());
    }
    super.render();
  }

  @Override
  public void dispose() {
    super.dispose();
    if (virtualControlRenderer != null) {
      virtualControlRenderer.dispose();
    }
  }
}
