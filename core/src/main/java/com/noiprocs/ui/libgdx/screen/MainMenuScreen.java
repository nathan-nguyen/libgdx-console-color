package com.noiprocs.ui.libgdx.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.noiprocs.LibGDXApp;
import com.noiprocs.ui.libgdx.util.UIStyleHelper;

/**
 * Main menu screen with Play, Settings, and Exit buttons. Uses Scene2D for UI layout and input
 * handling.
 */
public class MainMenuScreen implements Screen {
  private final LibGDXApp app;
  private Stage stage;
  private Skin skin;

  public MainMenuScreen(LibGDXApp app) {
    this.app = app;
  }

  @Override
  public void show() {
    // Create stage for UI
    stage = new Stage(app.getViewport(), app.getBatch());

    // Create skin for UI elements
    skin = UIStyleHelper.createSkin(app.getFont());

    // Create UI table
    Table table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    // Title label
    Label titleLabel = new Label("Console Color", skin);
    table.add(titleLabel).colspan(1).padBottom(30);
    table.row();

    // Play button
    TextButton playButton = new TextButton("Play", skin);
    playButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            app.showGame();
          }
        });
    table.add(playButton).width(200).height(50).pad(10);
    table.row();

    // Settings button
    TextButton settingsButton = new TextButton("Settings", skin);
    settingsButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            app.showSettings();
          }
        });
    table.add(settingsButton).width(200).height(50).pad(10);
    table.row();

    // Exit button
    TextButton exitButton = new TextButton("Exit", skin);
    exitButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            Gdx.app.exit();
          }
        });
    table.add(exitButton).width(200).height(50).pad(10);

    // Set input processor to stage
    Gdx.input.setInputProcessor(stage);
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
