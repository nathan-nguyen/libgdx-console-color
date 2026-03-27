package com.noiprocs.ui.libgdx.widget;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
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
import com.noiprocs.gameplay.control.command.ExitMazeCommand;
import com.noiprocs.resources.RenderResources;
import com.noiprocs.settings.HotbarLocation;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.libgdx.util.UIStyleHelper;
import java.lang.reflect.Field;
import java.util.Map;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

public class MenuOverlay extends Table {
  private final Table menuBox;
  private final Skin skin;
  private final Stage stage;
  private Runnable onClose;

  public MenuOverlay(SettingsManager settingsManager, Runnable onMainMenu, Viewport viewport) {
    RenderResources renderResources = RenderResources.get();
    this.skin = UIStyleHelper.createSkin(renderResources.getHudFont());
    this.stage = new Stage(viewport, renderResources.getBatch());
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
            applyLogLevel(isChecked);
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

    TextButton destroyMazeButton = new TextButton("Return Home", skin);
    destroyMazeButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            Dialog dialog =
                new Dialog("", skin) {
                  @Override
                  protected void result(Object object) {
                    if (Boolean.TRUE.equals(object)) {
                      gameContext.controlManager.processInput(
                          new ExitMazeCommand(gameContext.username));
                      close();
                    }
                  }
                };
            Label confirmLabel = new Label("Destroy the active maze?", skin);
            dialog.getContentTable().pad(30);
            dialog.text(confirmLabel);
            dialog.button("Confirm", true);
            dialog.button("Cancel", false);
            dialog.getButtonTable().pad(10).padBottom(20);
            dialog
                .getButtonTable()
                .getCells()
                .forEach(cell -> cell.width(140).height(50).padLeft(10).padRight(10));
            dialog.show(stage);
          }
        });
    menuBox.add(destroyMazeButton).width(300).height(60).padBottom(10);
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

    add(menuBox).width(400).height(590);

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

  private static void applyLogLevel(boolean debug) {
    // Try log4j2 first (desktop) — use reflection so ART doesn't reject missing classes
    try {
      Class<?> configurator = Class.forName("org.apache.logging.log4j.core.config.Configurator");
      Class<?> levelClass = Class.forName("org.apache.logging.log4j.Level");
      Object level = levelClass.getField(debug ? "DEBUG" : "INFO").get(null);
      configurator.getMethod("setRootLevel", levelClass).invoke(null, level);
      return;
    } catch (Exception ignored) {
    }
    // Fallback: slf4j-simple (Android)
    try {
      ILoggerFactory factory = LoggerFactory.getILoggerFactory();
      Field mapField = factory.getClass().getDeclaredField("loggerMap");
      mapField.setAccessible(true);
      int level = debug ? 10 : 20; // DEBUG_INT : INFO_INT
      for (Object logger : ((Map<?, ?>) mapField.get(factory)).values()) {
        Field f = logger.getClass().getDeclaredField("currentLogLevel");
        f.setAccessible(true);
        f.set(logger, level);
      }
    } catch (Exception ignored) {
    }
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
