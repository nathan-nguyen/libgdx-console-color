package com.noiprocs.resources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/** Utility class for loading game resources. */
public class ResourceLoader {
  private static final String FONT_CHARACTERS_JSON = "font-characters.json";
  private static final String MONOSPACE_FONTS_JSON = "monospace-fonts.json";

  private ResourceLoader() {
    // Utility class - prevent instantiation
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

  /**
   * Loads the monospace font paths from the monospace-fonts.json file.
   *
   * @return Array of font file paths to search for monospace fonts
   */
  public static String[] loadMonospaceFonts() {
    FileHandle monospaceFontsFile = Gdx.files.classpath(MONOSPACE_FONTS_JSON);
    JsonReader jsonReader = new JsonReader();
    JsonValue root = jsonReader.parse(monospaceFontsFile);
    JsonValue fontsArray = root.get("fonts");
    String[] fonts = new String[fontsArray.size];
    int i = 0;
    for (JsonValue font : fontsArray) {
      fonts[i++] = font.asString();
    }
    return fonts;
  }
}
