package com.noiprocs.ui.menu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Helper class to generate programmatic Scene2D Skin for menu UI. Creates styles for labels,
 * buttons, and text fields.
 */
public class UIStyleHelper {

  /**
   * Creates a Scene2D Skin with programmatically generated styles.
   *
   * @param font BitmapFont to use for text rendering
   * @return Configured Skin with label, button, and text field styles
   */
  public static Skin createSkin(BitmapFont font) {
    Skin skin = new Skin();

    // NOTE: Don't add the font to the skin - it's a shared resource managed by LibGDXApp
    // Adding it would cause it to be disposed when the skin is disposed

    // Helper method to create a colored texture
    Pixmap pixmap = new Pixmap(1, 1, Format.RGBA8888);

    // Create white texture
    pixmap.setColor(Color.WHITE);
    pixmap.fill();
    Texture whiteTexture = new Texture(pixmap);

    // Create button textures
    pixmap.setColor(0.3f, 0.3f, 0.3f, 1f); // Dark gray
    pixmap.fill();
    Texture buttonUpTexture = new Texture(pixmap);

    pixmap.setColor(0.2f, 0.2f, 0.2f, 1f); // Darker gray
    pixmap.fill();
    Texture buttonDownTexture = new Texture(pixmap);

    pixmap.setColor(0.4f, 0.4f, 0.4f, 1f); // Lighter gray
    pixmap.fill();
    Texture buttonOverTexture = new Texture(pixmap);

    // Create text field textures
    pixmap.setColor(0.25f, 0.25f, 0.25f, 1f);
    pixmap.fill();
    Texture textFieldBgTexture = new Texture(pixmap);

    pixmap.setColor(0.3f, 0.3f, 0.3f, 1f);
    pixmap.fill();
    Texture textFieldFocusedBgTexture = new Texture(pixmap);

    pixmap.setColor(0.5f, 0.5f, 0.8f, 0.5f);
    pixmap.fill();
    Texture selectionTexture = new Texture(pixmap);

    pixmap.dispose();

    // Add textures to skin so they get disposed properly
    skin.add("white", whiteTexture);
    skin.add("button-up", buttonUpTexture);
    skin.add("button-down", buttonDownTexture);
    skin.add("button-over", buttonOverTexture);
    skin.add("textfield-bg", textFieldBgTexture);
    skin.add("textfield-focused-bg", textFieldFocusedBgTexture);
    skin.add("selection", selectionTexture);

    // Ensure font color is white
    font.setColor(Color.WHITE);

    // Label style (white text)
    LabelStyle labelStyle = new LabelStyle();
    labelStyle.font = font;
    labelStyle.fontColor = new Color(1f, 1f, 1f, 1f); // Explicit white color
    skin.add("default", labelStyle);

    // Text button style
    TextButtonStyle buttonStyle = new TextButtonStyle();
    buttonStyle.font = font;
    buttonStyle.fontColor = new Color(1f, 1f, 1f, 1f); // Explicit white color
    buttonStyle.downFontColor = new Color(0.75f, 0.75f, 0.75f, 1f);
    buttonStyle.overFontColor = new Color(0.9f, 0.9f, 0.9f, 1f);
    buttonStyle.up = new TextureRegionDrawable(buttonUpTexture);
    buttonStyle.down = new TextureRegionDrawable(buttonDownTexture);
    buttonStyle.over = new TextureRegionDrawable(buttonOverTexture);
    skin.add("default", buttonStyle);

    // Text field style
    TextFieldStyle textFieldStyle = new TextFieldStyle();
    textFieldStyle.font = font;
    textFieldStyle.fontColor = new Color(1f, 1f, 1f, 1f); // Explicit white color
    textFieldStyle.focusedFontColor = new Color(1f, 1f, 1f, 1f);
    textFieldStyle.disabledFontColor = new Color(0.5f, 0.5f, 0.5f, 1f);
    textFieldStyle.background = new TextureRegionDrawable(textFieldBgTexture);
    textFieldStyle.focusedBackground = new TextureRegionDrawable(textFieldFocusedBgTexture);
    textFieldStyle.cursor = new TextureRegionDrawable(whiteTexture);
    textFieldStyle.selection = new TextureRegionDrawable(selectionTexture);
    skin.add("default", textFieldStyle);

    return skin;
  }
}
