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
  private static final String KEY_DEBUG_MODE = "debugMode";
  private static final String KEY_SHOW_WALLS = "showWalls";
  private static final String KEY_OCCLUDE = "occlude";
  private static final String KEY_HOTBAR_LOCATION = "hotbarLocation";

  // Default values
  private static final String DEFAULT_USERNAME = "player";
  private static final String DEFAULT_HOSTNAME = "localhost";
  private static final int DEFAULT_PORT = 8080;
  private static final boolean DEFAULT_DEBUG_MODE = false;
  private static final boolean DEFAULT_SHOW_WALLS = true;
  private static final boolean DEFAULT_OCCLUDE = false;
  private static final HotbarLocation DEFAULT_HOTBAR_LOCATION = HotbarLocation.TOP;

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
   * Get the debug mode setting.
   *
   * @return True if debug mode is enabled (default: false)
   */
  public boolean isDebugMode() {
    return prefs.getBoolean(KEY_DEBUG_MODE, DEFAULT_DEBUG_MODE);
  }

  /**
   * Set the debug mode setting.
   *
   * @param debugMode True to enable debug mode
   */
  public void setDebugMode(boolean debugMode) {
    prefs.putBoolean(KEY_DEBUG_MODE, debugMode);
  }

  public boolean isShowWalls() {
    return prefs.getBoolean(KEY_SHOW_WALLS, DEFAULT_SHOW_WALLS);
  }

  public void setShowWalls(boolean showWalls) {
    prefs.putBoolean(KEY_SHOW_WALLS, showWalls);
  }

  public boolean isOcclude() {
    return prefs.getBoolean(KEY_OCCLUDE, DEFAULT_OCCLUDE);
  }

  public void setOcclude(boolean occlude) {
    prefs.putBoolean(KEY_OCCLUDE, occlude);
  }

  public HotbarLocation getHotbarLocation() {
    String value = prefs.getString(KEY_HOTBAR_LOCATION, DEFAULT_HOTBAR_LOCATION.name());
    try {
      return HotbarLocation.valueOf(value);
    } catch (IllegalArgumentException e) {
      return DEFAULT_HOTBAR_LOCATION;
    }
  }

  public void setHotbarLocation(HotbarLocation location) {
    prefs.putString(KEY_HOTBAR_LOCATION, location.name());
  }

  /**
   * Save all settings to persistent storage. Must be called after setting values to persist them.
   */
  public void save() {
    prefs.flush();
  }
}
