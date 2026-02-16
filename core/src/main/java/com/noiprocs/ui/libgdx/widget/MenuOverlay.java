package com.noiprocs.ui.libgdx.widget;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.noiprocs.LibGDXApp;
import com.noiprocs.core.control.command.DisconnectCommand;

public class MenuOverlay extends Table {
  private final Table menuBox;
  private Runnable onClose;

  public MenuOverlay(LibGDXApp app, Skin skin) {
    setFillParent(true);
    setVisible(false);
    setTouchable(Touchable.enabled);

    // Add semi-transparent background to make the entire overlay touchable
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
    menuBox.add(titleLabel).expandX().center().padBottom(20);
    menuBox.row();

    // Debug mode checkbox
    CheckBox debugModeCheckbox = new CheckBox(" Debug Mode", skin);
    debugModeCheckbox.setChecked(app.getSettingsManager().isDebugMode());
    debugModeCheckbox.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            boolean isChecked = debugModeCheckbox.isChecked();
            app.getSettingsManager().setDebugMode(isChecked);
            app.getSettingsManager().save();
          }
        });
    menuBox.add(debugModeCheckbox).left().padBottom(20);
    menuBox.row();

    TextButton mainMenuButton = new TextButton("Main Menu", skin);
    mainMenuButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (app.getGameContext() != null && !app.getGameContext().isServer) {
              DisconnectCommand disconnectCommand =
                  new DisconnectCommand(app.getGameContext().username);
              app.getGameContext().controlManager.processInput(disconnectCommand);
            }
            app.showMainMenu();
          }
        });
    menuBox.add(mainMenuButton).width(300).height(60);
    menuBox.row();

    add(menuBox).width(400).height(350);

    // Close overlay when clicking outside the menu box - use capture listener
    addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            // If click target is not menuBox or its descendant, close the overlay
            Actor target = event.getTarget();
            if (target != menuBox && !isDescendantOf(target, menuBox)) {
              close();
              event.stop(); // Stop the event from propagating
              return true;
            }
            return false;
          }
        });
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
    // Don't capture hits if overlay is not visible
    if (!isVisible()) {
      return null;
    }

    // Always return self for hits within bounds to ensure we capture all touch events
    if (touchable && getTouchable() == Touchable.enabled) {
      if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
        // Check if hit is on menuBox first
        Actor hit =
            menuBox.hit(
                menuBox.parentToLocalCoordinates(new Vector2(x, y)).x,
                menuBox.parentToLocalCoordinates(new Vector2(x, y)).y,
                true);
        if (hit != null) {
          return hit;
        }
        // If not on menuBox, return self to capture the event
        return this;
      }
    }
    return null;
  }
}
