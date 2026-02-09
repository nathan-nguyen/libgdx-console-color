package com.noiprocs.android;

import android.os.Bundle;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.noiprocs.LibGDXApp;

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
    String hostname = "192.168.50.49";
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
      boolean showEquipmentAction =
          touchState.getHudType(getGameScreen()) == TouchState.HudType.CHEST;
      virtualControlRenderer.renderHudControls(
          touchState.getActiveZones(), getBatch(), getFont(), showEquipmentAction);
    } else {
      // Render game controls
      virtualControlRenderer.renderGameControls(
          touchState.getActiveZones(), getBatch(), getFont());
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
