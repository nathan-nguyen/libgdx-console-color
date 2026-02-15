package com.noiprocs.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.noiprocs.LibGDXApp;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
  public static void main(String[] args) {
    if (StartupHelper.startNewJvmIfRequired())
      return; // This handles macOS support and helps on Windows.

    // Settings are now managed by SettingsManager and loaded from the main menu
    createApplication();
  }

  private static Lwjgl3Application createApplication() {
    // Create desktop-specific keyboard input controller
    DesktopKeyboardInputController inputController = new DesktopKeyboardInputController();

    return new Lwjgl3Application(
        new LibGDXApp("pc", "client", inputController), getDefaultConfiguration());
  }

  private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    configuration.setTitle("libgdx-console-color");
    configuration.useVsync(true);
    configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);

    // Match Swing window size exactly (440x690)
    // With margins removed, content should now fill the window perfectly
    configuration.setWindowedMode(440, 690);

    return configuration;
  }
}
