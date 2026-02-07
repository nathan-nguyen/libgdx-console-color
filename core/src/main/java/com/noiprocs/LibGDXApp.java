package com.noiprocs;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.ScreenUtils;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.control.command.InputCommand;
import com.noiprocs.ui.console.ConsoleUIConfig;
import com.noiprocs.ui.console.hitbox.ConsoleHitboxManager;
import com.noiprocs.ui.console.sprite.ConsoleSpriteManager;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import java.util.HashSet;
import java.util.Set;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class LibGDXApp extends ApplicationAdapter {
  private SpriteBatch batch;
  private BitmapFont font;
  private LibGDXGameScreen gameScreen;
  private GameContext gameContext;
  private Thread gameThread;

  // Configuration from command line
  private String platform;
  private String username;
  private String type;
  private String hostname;
  private int port;

  // Font metrics for monospace rendering
  private float charWidth;
  private float charHeight;

  // Input handling
  private final Set<Character> keyPressedSet = new HashSet<>();

  public LibGDXApp(String platform, String username, String type, String hostname, int port) {
    this.platform = platform;
    this.username = username;
    this.type = type;
    this.hostname = hostname;
    this.port = port;
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
    // Handle crafting HUD
    if (gameScreen.hud.craftingHud.isOpen()) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        gameScreen.hud.craftingHud.close();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
        gameScreen.hud.craftingHud.close();
        gameScreen.hud.equipmentHud.open();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        gameScreen.hud.craftingHud.craftSelectedItem();
        return;
      }
      // Arrow key navigation
      if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        gameScreen.hud.craftingHud.handleNavigation('w');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        gameScreen.hud.craftingHud.handleNavigation('s');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
        gameScreen.hud.craftingHud.handleNavigation('a');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
        gameScreen.hud.craftingHud.handleNavigation('d');
        return;
      }
      return;
    }

    // Handle equipment HUD
    if (gameScreen.hud.equipmentHud.isOpen()) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        gameScreen.hud.equipmentHud.close();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
        gameScreen.hud.equipmentHud.close();
        gameScreen.hud.craftingHud.open();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        gameScreen.hud.equipmentHud.handleEquipmentAction();
        return;
      }
      // Arrow key navigation
      if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        gameScreen.hud.equipmentHud.handleNavigation('w');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        gameScreen.hud.equipmentHud.handleNavigation('s');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
        gameScreen.hud.equipmentHud.handleNavigation('a');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
        gameScreen.hud.equipmentHud.handleNavigation('d');
        return;
      }
      // Handle number keys 1-4 for inventory swapping
      if (!gameScreen.hud.equipmentHud.isEquipmentSelected()) {
        for (int i = 0; i < 4; i++) {
          if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
            int targetSlot = i;
            int currentSlot = gameScreen.hud.equipmentHud.getSelectedSlot();
            if (targetSlot != currentSlot) {
              gameContext.controlManager.swapInventorySlots(currentSlot, targetSlot);
            }
            return;
          }
        }
      }
      return;
    }

    // Handle chest interaction HUD
    if (gameScreen.hud.inventoryInteractionHud.isChestOpen()) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        gameScreen.hud.inventoryInteractionHud.close();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
        gameScreen.hud.inventoryInteractionHud.transferSelectedItem();
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
        gameScreen.hud.inventoryInteractionHud.handleEquipmentAction();
        return;
      }
      // Arrow key navigation
      if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
        gameScreen.hud.inventoryInteractionHud.handleNavigation('w');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
        gameScreen.hud.inventoryInteractionHud.handleNavigation('s');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
        gameScreen.hud.inventoryInteractionHud.handleNavigation('a');
        return;
      }
      if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
        gameScreen.hud.inventoryInteractionHud.handleNavigation('d');
        return;
      }
      return;
    }

    // Check for 'e' key to open equipment HUD
    if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
      gameScreen.hud.equipmentHud.open();
      return;
    }

    // Handle movement keys
    boolean anyKeyPressed = false;
    if (Gdx.input.isKeyPressed(Input.Keys.W)) {
      processKey('w');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.A)) {
      processKey('a');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.S)) {
      processKey('s');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.D)) {
      processKey('d');
      anyKeyPressed = true;
    }
    if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
      processKey(' ');
      anyKeyPressed = true;
    }

    // If no movement keys pressed, send halt command
    if (!anyKeyPressed && !keyPressedSet.isEmpty()) {
      keyPressedSet.clear();
      gameContext.controlManager.processInput(new InputCommand(gameContext.username, "h"));
    }
  }

  private void processKey(char key) {
    if (!keyPressedSet.contains(key)) {
      keyPressedSet.add(key);
      gameContext.controlManager.processInput(new InputCommand(gameContext.username, key));
    }
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
            + "∙│═╔╗╚╝╱╲▀▄█▌▐▒▓▲◊"
            + "║╠╣"; // Double-line box drawing characters for HUD borders
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

