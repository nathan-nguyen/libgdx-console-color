package com.noiprocs.settings;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

/**
 * Manages persistent game settings using LibGDX Preferences. Stores username, hostname, and port
 * with sensible defaults.
 */
public class SettingsManager {
  private static final String PREFS_NAME = "libgdx-console-color-settings";

  // Preference keys
  private static final String KEY_USERNAME = "username";
  private static final String KEY_HOSTNAME = "hostname";
  private static final String KEY_PORT = "port";

  // Default values
  private static final String DEFAULT_USERNAME = "player";
  private static final String DEFAULT_HOSTNAME = "localhost";
  private static final int DEFAULT_PORT = 8080;

  private final Preferences prefs;

  public SettingsManager() {
    this.prefs = Gdx.app.getPreferences(PREFS_NAME);
  }

  /**
   * Get the stored username.
   *
   * @return Username (default: "player")
   */
  public String getUsername() {
    return prefs.getString(KEY_USERNAME, DEFAULT_USERNAME);
  }

  /**
   * Set the username.
   *
   * @param username Username to store
   */
  public void setUsername(String username) {
    prefs.putString(KEY_USERNAME, username);
  }

  /**
   * Get the stored hostname.
   *
   * @return Hostname (default: "localhost")
   */
  public String getHostname() {
    return prefs.getString(KEY_HOSTNAME, DEFAULT_HOSTNAME);
  }

  /**
   * Set the hostname.
   *
   * @param hostname Hostname to store
   */
  public void setHostname(String hostname) {
    prefs.putString(KEY_HOSTNAME, hostname);
  }

  /**
   * Get the stored port.
   *
   * @return Port (default: 8080)
   */
  public int getPort() {
    return prefs.getInteger(KEY_PORT, DEFAULT_PORT);
  }

  /**
   * Set the port.
   *
   * @param port Port to store
   */
  public void setPort(int port) {
    prefs.putInteger(KEY_PORT, port);
  }

  /**
   * Save all settings to persistent storage. Must be called after setting values to persist them.
   */
  public void save() {
    prefs.flush();
  }
}
