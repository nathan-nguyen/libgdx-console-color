package com.noiprocs.android;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

/** Scene2d actor that renders virtual touch controls within the Stage draw pass. */
class VirtualControlsActor extends Actor {
  private final VirtualControlRenderer virtualControlRenderer;
  private final TouchInputController touchInputController;

  VirtualControlsActor(
      VirtualControlRenderer virtualControlRenderer, TouchInputController touchInputController) {
    this.virtualControlRenderer = virtualControlRenderer;
    this.touchInputController = touchInputController;
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    TouchState touchState = touchInputController.getTouchState();
    virtualControlRenderer.setProjectionMatrix(batch.getProjectionMatrix());

    // ShapeRenderer cannot be used inside an open SpriteBatch — end it, render shapes, reopen.
    batch.end();
    virtualControlRenderer.renderShapes(touchState);
    batch.begin();

    virtualControlRenderer.renderIcons(
        batch, touchState.getActiveZones(), touchState.isThrowAimPointerActive());
  }
}
