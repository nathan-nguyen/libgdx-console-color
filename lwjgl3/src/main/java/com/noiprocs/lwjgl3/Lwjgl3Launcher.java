package com.noiprocs.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.noiprocs.LibGDXApp;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
  public static void main(String[] args) {
    if (StartupHelper.startNewJvmIfRequired())
      return; // This handles macOS support and helps on Windows.

    // Parse command line arguments
    if (args.length < 5) {
      System.err.println(
          "Usage: java Lwjgl3Launcher <platform> <username> <type> <hostname> <port>");
      System.err.println("Example: java Lwjgl3Launcher pc noiprocs client 192.168.50.255 8080");
      System.exit(1);
    }

    String platform = args[0];
    String username = args[1];
    String type = args[2];
    String hostname = args[3];
    int port = Integer.parseInt(args[4]);

    createApplication(platform, username, type, hostname, port);
  }

  private static Lwjgl3Application createApplication(
      String platform, String username, String type, String hostname, int port) {
    return new Lwjgl3Application(
        new LibGDXApp(platform, username, type, hostname, port), getDefaultConfiguration());
  }

  private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
    Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
    configuration.setTitle("libgdx-console-color");
    configuration.useVsync(true);
    configuration.setForegroundFPS(
        Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);

    // Match Swing window size exactly (440x690)
    // With margins removed, content should now fill the window perfectly
    configuration.setWindowedMode(440, 690);

    return configuration;
  }
}
