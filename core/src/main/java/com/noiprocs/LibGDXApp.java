package com.noiprocs;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.input.InputController;
import com.noiprocs.ui.console.ConsoleUIConfig;
import com.noiprocs.ui.console.hitbox.ConsoleHitboxManager;
import com.noiprocs.ui.console.sprite.ConsoleSpriteManager;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LibGDXApp extends ApplicationAdapter {
  private static final String FONT_CHARACTERS_JSON = "font-characters.json";

  // Virtual screen dimensions (matches desktop window size)
  private static final float VIRTUAL_WIDTH = 440f;
  private static final float VIRTUAL_HEIGHT = 690f;

  // Desktop monospace character aspect ratio (width / height)
  // Calculated from desktop fonts: charWidth / charHeight
  // For Menlo/Courier at size 12 with height 15: ~7.2 / 15 = 0.48
  private static final float DESKTOP_CHAR_ASPECT_RATIO = 0.47f;

  private SpriteBatch batch;
  private BitmapFont font;
  private LibGDXGameScreen gameScreen;
  private GameContext gameContext;
  private Thread gameThread;
  private InputController inputController;
  private OrthographicCamera camera;
  private Viewport viewport;

  // Configuration from command line
  private String platform;
  private String username;
  private String type;
  private String hostname;
  private int port;

  // Font metrics for monospace rendering
  private float charWidth;
  private float charHeight;

  public LibGDXApp(
      String platform,
      String username,
      String type,
      String hostname,
      int port,
      InputController inputController) {
    this.platform = platform;
    this.username = username;
    this.type = type;
    this.hostname = hostname;
    this.port = port;
    this.inputController = inputController;
  }

  @Override
  public void create() {
    // Setup camera and viewport for proper scaling
    camera = new OrthographicCamera();
    camera.setToOrtho(false, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
    viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
    viewport.apply();

    batch = new SpriteBatch();

    // Generate a monospace font using FreeType
    font = generateMonospaceFont();

    // Disable markup to prevent any text formatting interference
    font.getData().markupEnabled = false;

    // Calculate monospace character dimensions
    // Use the box drawing character to ensure consistent width across all characters
    BitmapFont.Glyph boxGlyph = font.getData().getGlyph('═');
    BitmapFont.Glyph textGlyph = font.getData().getGlyph('M');
    BitmapFont.Glyph spaceGlyph = font.getData().getGlyph(' ');

    // Use the maximum width to ensure all characters fit properly
    float boxWidth = (boxGlyph != null) ? boxGlyph.xadvance : 12f;
    float textWidth = (textGlyph != null) ? textGlyph.xadvance : 12f;
    float spaceWidth = (spaceGlyph != null) ? spaceGlyph.xadvance : 12f;
    charWidth = Math.max(Math.max(boxWidth, textWidth), spaceWidth);

    // Force all glyphs to use the same advance width for true monospacing
    BitmapFont.BitmapFontData fontData = font.getData();
    for (int i = 0; i < fontData.glyphs.length; i++) {
      BitmapFont.Glyph[] page = fontData.glyphs[i];
      if (page != null) {
        for (int j = 0; j < page.length; j++) {
          if (page[j] != null) {
            page[j].xadvance = (int) charWidth;
          }
        }
      }
    }
    fontData.spaceXadvance = charWidth;

    // Adjust line height to match Swing's rendering
    // Total lines: ~46 (4 player info + 42 map), Window height: 690
    // Target line height: 690 / 46 = 15 pixels
    charHeight = 15f;

    // Android: Override charWidth to match desktop aspect ratio
    if ("android".equals(platform)) {
      charWidth = charHeight * DESKTOP_CHAR_ASPECT_RATIO;
    }

    ConsoleUIConfig.CLEAR_SCREEN = false;

    gameScreen = new LibGDXGameScreen();

    // Initialize gameContext
    gameContext =
        GameContext.build(
            platform,
            username,
            type,
            hostname,
            port,
            new ConsoleHitboxManager(),
            new ConsoleSpriteManager(),
            gameScreen);

    // Start game thread
    gameThread = new Thread(gameContext::run);
    gameThread.start();
  }

  @Override
  public void render() {
    // Handle input
    handleInput();

    // Clear screen
    ScreenUtils.clear(0f, 0f, 0f, 1f);

    // Update camera
    camera.update();
    batch.setProjectionMatrix(camera.combined);

    // Render game screen
    batch.begin();
    gameScreen.render(0);
    gameScreen.renderWithBatch(batch, font, charWidth, charHeight);
    batch.end();
  }

  private void handleInput() {
    // Delegate to platform-specific input controller
    inputController.handleInput(gameContext, gameScreen);
  }

  private String loadFontCharacters() {
    FileHandle fontCharsFile = Gdx.files.classpath(FONT_CHARACTERS_JSON);
    JsonReader jsonReader = new JsonReader();
    JsonValue root = jsonReader.parse(fontCharsFile);
    return root.getString("characters");
  }

  private BitmapFont generateMonospaceFont() {
    // Prioritize fonts that match Java's "monospaced" logical font
    // On macOS: Menlo (modern) or Courier (legacy)
    // On Windows: Courier New
    // On Android: RobotoMono or DroidSansMono
    String[] monospaceFonts =
        new String[] {
          "/System/Library/Fonts/Menlo.ttc", // macOS default monospaced (modern)
          "/System/Library/Fonts/Courier.dfont", // macOS Courier (legacy)
          "/System/Library/Fonts/Supplemental/Courier New.ttf", // macOS Courier New
          "C:/Windows/Fonts/cour.ttf", // Windows Courier
          "C:/Windows/Fonts/consola.ttf", // Windows Consolas
          "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf", // Linux
          "/system/fonts/RobotoMono-Regular.ttf", // Android (newer versions)
          "/system/fonts/DroidSansMono.ttf", // Android (older versions)
          "/system/fonts/CutiveMono-Regular.ttf" // Android (alternative)
        };

    FreeTypeFontGenerator generator = null;
    for (String fontPath : monospaceFonts) {
      try {
        if (Gdx.files.absolute(fontPath).exists()) {
          generator = new FreeTypeFontGenerator(Gdx.files.absolute(fontPath));
          break;
        }
      } catch (Exception e) {
        // Try next
      }
    }

    if (generator == null) {
      BitmapFont defaultFont = new BitmapFont();
      defaultFont.setUseIntegerPositions(true);
      defaultFont.getData().setScale(1.2f);
      defaultFont.setColor(Color.WHITE);
      return defaultFont;
    }

    FreeTypeFontParameter parameter = new FreeTypeFontParameter();
    parameter.size = 12;
    parameter.mono = true;
    parameter.characters = loadFontCharacters();
    parameter.color = Color.WHITE;

    BitmapFont monoFont = generator.generateFont(parameter);
    generator.dispose();
    monoFont.setUseIntegerPositions(true);
    // No scaling - match Swing's natural rendering
    return monoFont;
  }

  @Override
  public void resize(int width, int height) {
    // Update viewport when screen size changes (important for Android)
    viewport.update(width, height);
  }

  @Override
  public void dispose() {
    batch.dispose();
    font.dispose();
    // Game thread will be terminated when application exits
  }
}

