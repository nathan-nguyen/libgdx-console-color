package com.noiprocs;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.noiprocs.core.GameContext;
import com.noiprocs.input.InputController;
import com.noiprocs.ui.console.ConsoleUIConfig;
import com.noiprocs.ui.console.hitbox.ConsoleHitboxManager;
import com.noiprocs.ui.console.sprite.ConsoleSpriteManager;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LibGDXApp extends ApplicationAdapter {
  private SpriteBatch batch;
  private BitmapFont font;
  private LibGDXGameScreen gameScreen;
  private GameContext gameContext;
  private Thread gameThread;
  private InputController inputController;

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

  private BitmapFont generateMonospaceFont() {
    // Prioritize fonts that match Java's "monospaced" logical font
    // On macOS: Menlo (modern) or Courier (legacy)
    // On Windows: Courier New
    String[] monospaceFonts =
        new String[] {
          "/System/Library/Fonts/Menlo.ttc", // macOS default monospaced (modern)
          "/System/Library/Fonts/Courier.dfont", // macOS Courier (legacy)
          "/System/Library/Fonts/Supplemental/Courier New.ttf", // macOS Courier New
          "C:/Windows/Fonts/cour.ttf", // Windows Courier
          "C:/Windows/Fonts/consola.ttf", // Windows Consolas
          "/usr/share/fonts/truetype/dejavu/DejaVuSansMono.ttf" // Linux
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
    parameter.characters =
        " !\"#$%&'()*+,-./0123456789:;<=>?@"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`"
            + "abcdefghijklmnopqrstuvwxyz{|}~"
            + "∙│═╱╲▀▄█▌▐▒▓▲◊░"
            + "╔╗╚╝║╠╣"; // Double-line box drawing characters for HUD borders
    parameter.color = Color.WHITE;

    BitmapFont monoFont = generator.generateFont(parameter);
    generator.dispose();
    monoFont.setUseIntegerPositions(true);
    // No scaling - match Swing's natural rendering
    return monoFont;
  }

  @Override
  public void dispose() {
    batch.dispose();
    font.dispose();
    // Game thread will be terminated when application exits
  }
}

