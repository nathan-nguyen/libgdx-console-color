package com.noiprocs.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Utility class for loading game resources. */
public class ResourceLoader {
  private static final String FONT_CHARACTERS_JSON = "font-characters.json";

  /**
   * Loads a texture for the given resource with Linear filtering applied.
   *
   * @param resource the resource to load
   * @return a new Texture instance; caller is responsible for disposal
   */
  public static Texture loadTexture(GameResource resource) {
    Texture texture = new Texture(Gdx.files.internal(resource.getPath()));
    texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    return texture;
  }

  /**
   * Generates a BitmapFont from the given resource using the provided parameters.
   *
   * @param resource the font file resource to load
   * @param parameter font generation parameters
   * @return a new BitmapFont instance; caller is responsible for disposal
   */
  public static BitmapFont loadFont(GameResource resource, FreeTypeFontParameter parameter) {
    FreeTypeFontGenerator generator =
        new FreeTypeFontGenerator(Gdx.files.internal(resource.getPath()));
    BitmapFont font = generator.generateFont(parameter);
    generator.dispose();
    return font;
  }

  /**
   * Loads the font characters from the font-characters.json file.
   *
   * @return String containing all font characters to be included in the generated font
   */
  public static String loadFontCharacters() {
    FileHandle fontCharsFile = Gdx.files.classpath(FONT_CHARACTERS_JSON);
    JsonReader jsonReader = new JsonReader();
    JsonValue root = jsonReader.parse(fontCharsFile);
    return root.getString("characters");
  }
}
