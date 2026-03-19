package com.noiprocs.ui.libgdx.widget;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.GameMode;
import com.noiprocs.core.control.command.DisconnectCommand;
import com.noiprocs.settings.HotbarLocation;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.libgdx.util.UIStyleHelper;

public class MenuOverlay extends Table {
  private final Table menuBox;
  private final Skin skin;
  private final Stage stage;
  private Runnable onClose;

  public MenuOverlay(
      SettingsManager settingsManager,
      Runnable onMainMenu,
      BitmapFont font,
      Viewport viewport,
      SpriteBatch batch) {
    this.skin = UIStyleHelper.createSkin(font);
    this.stage = new Stage(viewport, batch);
    this.stage.addActor(this);
    setFillParent(true);
    setVisible(false);
    setTouchable(Touchable.enabled);

    Pixmap overlayBgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    overlayBgPixmap.setColor(0f, 0f, 0f, 0.5f);
    overlayBgPixmap.fill();
    Texture overlayBgTexture = new Texture(overlayBgPixmap);
    overlayBgPixmap.dispose();
    setBackground(new TextureRegionDrawable(overlayBgTexture));

    menuBox = new Table();
    menuBox.setTouchable(Touchable.enabled);

    Pixmap menuBgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    menuBgPixmap.setColor(0.2f, 0.2f, 0.2f, 0.95f);
    menuBgPixmap.fill();
    Texture menuBgTexture = new Texture(menuBgPixmap);
    menuBgPixmap.dispose();
    menuBox.setBackground(new TextureRegionDrawable(menuBgTexture));
    menuBox.pad(40);

    Label titleLabel = new Label("Menu", skin);
    titleLabel.setFontScale(1.5f);
    menuBox.add(titleLabel).expandX().center().padBottom(8);
    menuBox.row();

    GameContext gameContext = GameContext.get();
    String playerName = gameContext != null ? gameContext.username : "";
    Label playerNameLabel = new Label(playerName, skin);
    menuBox.add(playerNameLabel).expandX().center().padBottom(20);
    menuBox.row();

    CheckBox debugModeCheckbox = new CheckBox(" Debug Mode", skin);
    debugModeCheckbox.setChecked(settingsManager.isDebugMode());
    debugModeCheckbox.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            boolean isChecked = debugModeCheckbox.isChecked();
            settingsManager.setDebugMode(isChecked);
            settingsManager.save();
          }
        });
    menuBox.add(debugModeCheckbox).left().padBottom(20);
    menuBox.row();

    CheckBox showWallsCheckbox = new CheckBox(" Show Walls", skin);
    showWallsCheckbox.setChecked(settingsManager.isShowWalls());
    showWallsCheckbox.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            boolean isChecked = showWallsCheckbox.isChecked();
            settingsManager.setShowWalls(isChecked);
            settingsManager.save();
          }
        });
    menuBox.add(showWallsCheckbox).left().padBottom(20);
    menuBox.row();

    CheckBox occludeCheckbox = new CheckBox(" Occlude", skin);
    occludeCheckbox.setChecked(settingsManager.isOcclude());
    occludeCheckbox.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            boolean isChecked = occludeCheckbox.isChecked();
            settingsManager.setOcclude(isChecked);
            settingsManager.save();
          }
        });
    menuBox.add(occludeCheckbox).left().padBottom(20);
    menuBox.row();

    CheckBox hotbarBottomCheckbox = new CheckBox(" Hotbar at Bottom", skin);
    hotbarBottomCheckbox.setChecked(settingsManager.getHotbarLocation() == HotbarLocation.BOTTOM);
    hotbarBottomCheckbox.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            HotbarLocation location =
                hotbarBottomCheckbox.isChecked() ? HotbarLocation.BOTTOM : HotbarLocation.TOP;
            settingsManager.setHotbarLocation(location);
            settingsManager.save();
          }
        });
    menuBox.add(hotbarBottomCheckbox).left().padBottom(20);
    menuBox.row();

    TextButton mainMenuButton = new TextButton("Main Menu", skin);
    mainMenuButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (gameContext != null && gameContext.gameMode == GameMode.CLIENT) {
              DisconnectCommand disconnectCommand = new DisconnectCommand(gameContext.username);
              gameContext.controlManager.processInput(disconnectCommand);
            }
            onMainMenu.run();
          }
        });
    menuBox.add(mainMenuButton).width(300).height(60);
    menuBox.row();

    add(menuBox).width(400).height(510);

    addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();
            if (target != menuBox && !isDescendantOf(target, menuBox)) {
              close();
              event.stop();
              return true;
            }
            return false;
          }
        });
  }

  public Stage getStage() {
    return stage;
  }

  public void render(float delta) {
    stage.act(delta);
    stage.draw();
  }

  public void dispose() {
    stage.dispose();
    skin.dispose();
  }

  public void setOnClose(Runnable onClose) {
    this.onClose = onClose;
  }

  public void close() {
    setVisible(false);
    if (onClose != null) {
      onClose.run();
    }
  }

  private boolean isDescendantOf(Actor actor, Actor parent) {
    if (actor == null) return false;
    Actor current = actor;
    while (current != null) {
      if (current == parent) return true;
      current = current.getParent();
    }
    return false;
  }

  @Override
  public Actor hit(float x, float y, boolean touchable) {
    if (!isVisible()) {
      return null;
    }

    if (touchable && getTouchable() == Touchable.enabled) {
      if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
        Actor hit =
            menuBox.hit(
                menuBox.parentToLocalCoordinates(new Vector2(x, y)).x,
                menuBox.parentToLocalCoordinates(new Vector2(x, y)).y,
                true);
        if (hit != null) {
          return hit;
        }
        return this;
      }
    }
    return null;
  }
}
