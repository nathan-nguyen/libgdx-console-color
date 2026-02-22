package com.noiprocs.ui.libgdx.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.control.command.InputCommand;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.input.InputController;
import com.noiprocs.resources.RenderResources;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.settings.SettingsManager;
import com.noiprocs.ui.console.hitbox.ConsoleHitboxManager;
import com.noiprocs.ui.console.sprite.ConsoleSpriteManager;
import com.noiprocs.ui.libgdx.LibGDXGameScreen;
import com.noiprocs.ui.libgdx.hud.HUDManager;
import com.noiprocs.ui.libgdx.hud.panel.PlayerInfoHUD;
import com.noiprocs.ui.libgdx.util.UIStyleHelper;
import com.noiprocs.ui.libgdx.widget.MenuOverlay;
import java.util.function.Consumer;

/**
 * Game screen wrapper that integrates the existing character-grid rendering into the Screen
 * interface. Handles game initialization, input, and rendering.
 */
public class GameScreen implements Screen {
  private final Viewport viewport;
  private final OrthographicCamera camera;
  private final RenderResources renderResources;
  private final InputController inputController;
  private final SettingsManager settingsManager;
  private final Runnable virtualControlsRenderer;
  private final Consumer<GameContext> gameContextRegistrar;
  private final Runnable showMainMenu;
  private final String username;
  private final String hostname;
  private final int port;
  private final String type;
  private final String platform;

  private LibGDXGameScreen gameScreen;
  private GameContext gameContext;
  private Thread gameThread;
  private Stage uiStage;
  private Skin skin;
  private MenuOverlay menuOverlay;
  private Table buttonTable;
  private HUDManager hudManager;
  private PlayerInfoHUD playerInfoHUD;

  public GameScreen(
      Viewport viewport,
      OrthographicCamera camera,
      RenderResources renderResources,
      InputController inputController,
      SettingsManager settingsManager,
      Runnable virtualControlsRenderer,
      Consumer<GameContext> gameContextRegistrar,
      Runnable showMainMenu,
      String username,
      String hostname,
      int port,
      String type,
      String platform) {
    this.viewport = viewport;
    this.camera = camera;
    this.renderResources = renderResources;
    this.inputController = inputController;
    this.settingsManager = settingsManager;
    this.virtualControlsRenderer = virtualControlsRenderer;
    this.gameContextRegistrar = gameContextRegistrar;
    this.showMainMenu = showMainMenu;
    this.username = username;
    this.hostname = hostname;
    this.port = port;
    this.type = type;
    this.platform = platform;
  }

  @Override
  public void show() {
    float virtualHeight = viewport.getWorldHeight();
    float virtualWidth = viewport.getWorldWidth();

    int screenHeight = Math.round(virtualHeight / UIConfig.CHAR_HEIGHT);
    int screenWidth = Math.round(virtualWidth / UIConfig.CHAR_WIDTH);
    gameScreen = new LibGDXGameScreen(screenHeight, screenWidth, 120);

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

    gameContextRegistrar.accept(gameContext);

    gameThread = new Thread(gameContext::run);
    gameThread.start();

    setupUI();

    hudManager =
        new HUDManager(
            gameScreen,
            viewport,
            renderResources.getPanelFont(),
            renderResources.getItemTextureManager());
    gameScreen.setHudManager(hudManager);

    setupInputMultiplexer();
  }

  private void setupUI() {
    uiStage = new Stage(viewport, renderResources.getBatch());
    skin = UIStyleHelper.createSkin(renderResources.getHudFont());

    Texture menuIconTex = new Texture(Gdx.files.internal("icons/menu_button.png"));
    menuIconTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    skin.add("menu-icon", menuIconTex);
    TextureRegionDrawable menuNormal = new TextureRegionDrawable(new TextureRegion(menuIconTex));
    ImageButton.ImageButtonStyle menuStyle = new ImageButton.ImageButtonStyle();
    menuStyle.imageUp = menuNormal;
    menuStyle.imageDown = menuNormal.tint(new Color(1, 1, 1, 0.4f));
    ImageButton menuButton = new ImageButton(menuStyle);
    menuButton.getImage().setScaling(Scaling.fit);
    menuButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            toggleMenu();
          }
        });

    Texture equipIconTex = new Texture(Gdx.files.internal("icons/equipment_button.png"));
    equipIconTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    skin.add("equipment-icon", equipIconTex);
    TextureRegionDrawable equipNormal = new TextureRegionDrawable(new TextureRegion(equipIconTex));
    ImageButton.ImageButtonStyle equipStyle = new ImageButton.ImageButtonStyle();
    equipStyle.imageUp = equipNormal;
    equipStyle.imageDown = equipNormal.tint(new Color(1, 1, 1, 0.4f));
    ImageButton equipmentButton = new ImageButton(equipStyle);
    equipmentButton.getImage().setScaling(Scaling.fit);
    equipmentButton.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            openEquipmentHUD();
          }
        });

    buttonTable = new Table();
    buttonTable.setFillParent(true);
    buttonTable.top().right().pad(10);
    buttonTable.add(equipmentButton).size(50, 50).padRight(10);
    buttonTable.add(menuButton).size(50, 50);
    uiStage.addActor(buttonTable);

    menuOverlay = new MenuOverlay(settingsManager, showMainMenu, skin);
    menuOverlay.setOnClose(() -> buttonTable.setVisible(true));
    uiStage.addActor(menuOverlay);

    playerInfoHUD =
        new PlayerInfoHUD(renderResources.getHudFont(), renderResources.getItemTextureManager());
    playerInfoHUD.setOnSlotSelected(
        slotIndex ->
            gameContext.controlManager.processInput(
                new InputCommand(username, String.valueOf(slotIndex + 1))));
    uiStage.addActor(playerInfoHUD);
  }

  private void openEquipmentHUD() {
    if (hudManager != null) {
      hudManager.openEquipmentHUD();
    }
  }

  private void setupInputMultiplexer() {
    InputMultiplexer multiplexer = new InputMultiplexer();
    if (hudManager != null) {
      multiplexer.addProcessor(hudManager.getHudStage());
    }
    multiplexer.addProcessor(uiStage);
    Gdx.input.setInputProcessor(multiplexer);
  }

  private void toggleMenu() {
    boolean show = !menuOverlay.isVisible();
    menuOverlay.setVisible(show);
    buttonTable.setVisible(!show);
  }

  @Override
  public void render(float delta) {
    if (inputController != null) {
      inputController.handleInput(gameScreen);
    }

    ScreenUtils.clear(0f, 0f, 0f, 1f);

    camera.update();
    renderResources.getBatch().setProjectionMatrix(camera.combined);

    renderResources.getBatch().begin();
    gameScreen.render(0);
    gameScreen.renderWithBatch(
        renderResources.getBatch(),
        renderResources.getFont(),
        UIConfig.CHAR_WIDTH,
        UIConfig.CHAR_HEIGHT,
        viewport.getWorldHeight());
    renderResources.getBatch().end();

    virtualControlsRenderer.run();

    if (hudManager != null && hudManager.isOpen()) {
      hudManager.render(delta);
    }

    if (playerInfoHUD != null && gameContext != null) {
      Model playerModel = gameContext.modelManager.getModel(gameContext.username);
      if (playerModel instanceof PlayerModel) {
        playerInfoHUD.update((PlayerModel) playerModel, settingsManager);
      }
    }

    uiStage.act(delta);
    uiStage.draw();
  }

  @Override
  public void resize(int width, int height) {}

  @Override
  public void pause() {}

  @Override
  public void resume() {}

  @Override
  public void hide() {
    dispose();
  }

  @Override
  public void dispose() {
    if (gameThread != null && gameThread.isAlive()) {
      gameThread.interrupt();
      try {
        gameThread.join(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      gameThread = null;
    }

    gameContextRegistrar.accept(null);

    if (hudManager != null) {
      hudManager.dispose();
      hudManager = null;
    }
    if (playerInfoHUD != null) {
      playerInfoHUD.dispose();
      playerInfoHUD = null;
    }
    if (uiStage != null) {
      uiStage.dispose();
    }
    if (skin != null) {
      skin.dispose();
    }

    gameContext = null;
    gameScreen = null;
  }
}
