package com.noiprocs.ui.libgdx.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldFilter;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.noiprocs.LibGDXApp;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.libgdx.util.UIStyleHelper;

/**
 * Settings screen for configuring username, hostname, and port. Validates input and persists
 * settings using SettingsManager.
 */
public class SettingsScreen implements Screen {
  private final LibGDXApp app;
  private Stage stage;
  private Skin skin;
  private TextField usernameField;
  private TextField hostnameField;
  private TextField portField;
  private Label errorLabel;

  public SettingsScreen(LibGDXApp app) {
    this.app = app;
  }

  @Override
  public void show() {
    // Create stage for UI
    stage = new Stage(app.getViewport(), app.getRenderResources().getBatch());

    // Create skin for UI elements
    skin = UIStyleHelper.createSkin(app.getRenderResources().getFont());

    // Get current settings
    SettingsManager settings = app.getSettingsManager();

    // Create UI table
    Table table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    // Title label
    Label titleLabel = new Label("Settings", skin);
    table.add(titleLabel).colspan(2).padBottom(30);
    table.row();

    // Username row
    Label usernameLabel = new Label("Username:", skin);
    table.add(usernameLabel).padRight(10).padBottom(10);
    usernameField = new TextField(settings.getUsername(), skin);
    table.add(usernameField).width(200).padBottom(10);
    table.row();

    // Hostname row
    Label hostnameLabel = new Label("Hostname:", skin);
    table.add(hostnameLabel).padRight(10).padBottom(10);
    hostnameField = new TextField(settings.getHostname(), skin);
    table.add(hostnameField).width(200).padBottom(10);
    table.row();

    // Port row
    Label portLabel = new Label("Port:", skin);
    table.add(portLabel).padRight(10).padBottom(10);
    portField = new TextField(String.valueOf(settings.getPort()), skin);
    portField.setTextFieldFilter(new TextFieldFilter.DigitsOnlyFilter());
    table.add(portField).width(200).padBottom(10);
    table.row();

    // Error label (initially hidden)
    errorLabel = new Label("", skin);
    errorLabel.setColor(Color.RED);
    table.add(errorLabel).colspan(2).padBottom(10);
    table.row();

    // Buttons row
    Table buttonTable = new Table();

    // Save button
    TextButton saveButton = new TextButton("Save", skin);
    saveButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            saveSettings();
          }
        });
    buttonTable.add(saveButton).width(150).height(50).pad(10);

    // Back button
    TextButton backButton = new TextButton("Back", skin);
    backButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            app.showMainMenu();
          }
        });
    buttonTable.add(backButton).width(150).height(50).pad(10);

    table.add(buttonTable).colspan(2);

    // Add keyboard shortcuts
    stage.addListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
              app.showMainMenu();
              return true;
            } else if (keycode == Input.Keys.ENTER) {
              saveSettings();
              return true;
            }
            return false;
          }
        });

    // Set input processor to stage
    Gdx.input.setInputProcessor(stage);
  }

  private void saveSettings() {
    // Validate input
    String username = usernameField.getText().trim();
    String hostname = hostnameField.getText().trim();
    String portText = portField.getText().trim();

    if (username.isEmpty()) {
      showError("Username cannot be empty");
      return;
    }

    if (hostname.isEmpty()) {
      showError("Hostname cannot be empty");
      return;
    }

    if (portText.isEmpty()) {
      showError("Port cannot be empty");
      return;
    }

    int port;
    try {
      port = Integer.parseInt(portText);
      if (port < 1 || port > 65535) {
        showError("Port must be between 1 and 65535");
        return;
      }
    } catch (NumberFormatException e) {
      showError("Port must be a valid number");
      return;
    }

    // Save settings
    SettingsManager settings = app.getSettingsManager();
    settings.setUsername(username);
    settings.setHostname(hostname);
    settings.setPort(port);
    settings.save();

    // Return to main menu
    app.showMainMenu();
  }

  private void showError(String message) {
    errorLabel.setText(message);
  }

  @Override
  public void render(float delta) {
    // Clear screen with black background
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    // Update and draw stage
    stage.act(delta);
    stage.draw();
  }

  @Override
  public void resize(int width, int height) {
    // Update viewport when screen size changes
    stage.getViewport().update(width, height, true);
  }

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {
    // Cleanup when screen is hidden
    dispose();
  }

  @Override
  public void dispose() {
    if (stage != null) {
      stage.dispose();
      stage = null;
    }
    if (skin != null) {
      skin.dispose();
      skin = null;
    }
  }
}
