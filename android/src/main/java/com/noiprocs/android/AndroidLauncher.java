package com.noiprocs.android;

import android.os.Bundle;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.noiprocs.LibGDXApp;
import com.noiprocs.input.InputController;
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

    // Create AndroidApp which extends LibGDXApp with touch controls
    // Settings will be loaded from SettingsManager when user clicks Play
    AndroidApp app = new AndroidApp(PLATFORM, TYPE);

    initialize(app, configuration);
  }
}

/** Android-specific extension of LibGDXApp that adds touch controls. */
class AndroidApp extends LibGDXApp {
  private TouchInputController touchInputController;
  private VirtualControlRenderer virtualControlRenderer;

  public AndroidApp(String platform, String type) {
    super(platform, type, null);
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

  @Override
  public InputController getInputController() {
    // Return touch input controller for use by GameScreen
    return touchInputController;
  }

  private void renderTouchControls() {
    virtualControlRenderer.setProjectionMatrix(getCamera().combined);
    TouchState touchState = touchInputController.getTouchState();

    // Render game controls (graphical HUD handles its own input via Scene2D)
    virtualControlRenderer.renderGameControls(
        touchState.getActiveZones(), renderResources.getBatch(), touchState);
  }

  @Override
  public void dispose() {
    super.dispose();
    if (virtualControlRenderer != null) {
      virtualControlRenderer.dispose();
    }
  }
}
