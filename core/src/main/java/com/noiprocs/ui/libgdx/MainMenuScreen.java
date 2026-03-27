package com.noiprocs.ui.libgdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.noiprocs.LibGDXApp;
import com.noiprocs.resources.GameResource;
import com.noiprocs.resources.RenderResources;
import com.noiprocs.resources.ResourceLoader;
import com.noiprocs.ui.libgdx.util.UIStyleHelper;

/**
 * Main menu screen with Play, Settings, and Exit buttons. Uses Scene2D for UI layout and input
 * handling.
 */
public class MainMenuScreen implements Screen {
  private final LibGDXApp app;
  private Stage stage;
  private Skin skin;
  private Texture backgroundTexture;

  public MainMenuScreen(LibGDXApp app) {
    this.app = app;
  }

  @Override
  public void show() {
    backgroundTexture = ResourceLoader.loadTexture(GameResource.BACKGROUND_MAIN_MENU);
    RenderResources renderResources = RenderResources.get();

    // Create stage for UI
    stage = new Stage(app.getViewport(), renderResources.getBatch());

    // Create skin for UI elements
    skin = UIStyleHelper.createSkin(renderResources.getHudFont());

    // Create UI table
    Table table = new Table();
    table.setFillParent(true);
    stage.addActor(table);

    // Title label
    Label titleLabel = new Label("Maze Runner", skin);
    table.add(titleLabel).colspan(1).padBottom(30);
    table.row();

    // Single Player button
    TextButton singlePlayerButton = new TextButton("Single Player", skin);
    singlePlayerButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            app.showStandaloneGame();
          }
        });
    table.add(singlePlayerButton).width(200).height(50).pad(10);
    table.row();

    // Multiplayer button
    TextButton playButton = new TextButton("Multiplayer", skin);
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
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

    float w = app.getViewport().getWorldWidth();
    float h = app.getViewport().getWorldHeight();
    float scale = Math.max(w / backgroundTexture.getWidth(), h / backgroundTexture.getHeight());
    float drawW = backgroundTexture.getWidth() * scale;
    float drawH = backgroundTexture.getHeight() * scale;
    SpriteBatch batch = RenderResources.get().getBatch();
    batch.setProjectionMatrix(app.getViewport().getCamera().combined);
    batch.begin();
    batch.draw(backgroundTexture, (w - drawW) / 2f, (h - drawH) / 2f, drawW, drawH);
    batch.end();

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
    if (backgroundTexture != null) {
      backgroundTexture.dispose();
      backgroundTexture = null;
    }
  }
}
