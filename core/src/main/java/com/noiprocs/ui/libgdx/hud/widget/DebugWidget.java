package com.noiprocs.ui.libgdx.hud.widget;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class DebugWidget {
  public static Drawable getDrawableBackground() {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(0.18f, 0.18f, 0.18f, 0.9f);
    pixmap.fill();
    Texture panelTexture = new Texture(pixmap);
    pixmap.dispose();
    return new TextureRegionDrawable(panelTexture);
  }
}
