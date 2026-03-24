package com.noiprocs.ui.libgdx.hud.panel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.item.Inventory;
import com.noiprocs.core.model.item.Item;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.gameplay.control.command.BuyItemCommand;
import com.noiprocs.gameplay.control.command.SellItemCommand;
import com.noiprocs.gameplay.model.mob.MerchantModel;
import com.noiprocs.gameplay.world.MerchantConfigLoader;
import com.noiprocs.gameplay.world.MerchantConfigLoader.ItemEntry;
import com.noiprocs.resources.ItemTextureManager;
import com.noiprocs.ui.libgdx.hud.HUDManager;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotStyle;
import com.noiprocs.ui.libgdx.hud.widget.ItemSlotWidget;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Merchant shop HUD. Displays the merchant's stock on the right and the player's inventory on the
 * left. Click a shop item to buy; click a player item to sell.
 */
public class MerchantHUD {
  private static final Logger logger = LoggerFactory.getLogger(MerchantHUD.class);

  private static final int PLAYER_INVENTORY_SIZE = 9;

  private final HUDManager hudManager;
  private final BitmapFont font;
  private final Table rootTable;
  private final ItemSlotStyle slotStyle;
  private final Viewport viewport;
  private final ItemTextureManager itemTextureManager;
  private final Table mainContainer;

  private String merchantModelId;

  private Table confirmDialog;

  private ItemSlotWidget[] playerInventorySlots;
  private Label[] playerInventoryNameLabels;

  // Each shop row is one Table actor per ItemEntry in config order
  private Table shopListTable;
  private ItemSlotWidget[] shopSlots;

  // Persistent label showing current gold count
  private Label goldLabel;

  // Semi-transparent background textures (allocated once, disposed on dispose())
  private Texture backgroundTexture;
  private Texture panelTexture;

  public MerchantHUD(
      HUDManager hudManager,
      Viewport viewport,
      BitmapFont font,
      ItemSlotStyle slotStyle,
      ItemTextureManager itemTextureManager) {
    this.hudManager = hudManager;
    this.viewport = viewport;
    this.font = font;
    this.slotStyle = slotStyle;
    this.itemTextureManager = itemTextureManager;
    this.mainContainer = new Table();
    this.rootTable = new Table();
    this.rootTable.setFillParent(true);

    buildUI();
  }

  // ── Build ─────────────────────────────────────────────────────────────────

  private void buildUI() {
    rootTable.setTouchable(Touchable.enabled);
    rootTable.center();

    mainContainer.setBackground(createBackground());
    mainContainer.setTouchable(Touchable.enabled);

    rootTable.add(mainContainer);

    rootTable.addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();
            if (target != mainContainer && !isDescendantOf(target, mainContainer)) {
              close();
              event.stop();
              return true;
            }
            return false;
          }
        });
  }

  private void rebuildUI() {
    mainContainer.clear();

    // Header
    Table header = buildHeader();
    mainContainer.add(header).expandX().fillX().pad(5);
    mainContainer.row();

    // Gold display
    Label.LabelStyle labelStyle = makeLabelStyle(Color.YELLOW);
    goldLabel = new Label("", labelStyle);
    goldLabel.setFontScale(0.85f);
    mainContainer.add(goldLabel).padBottom(4);
    mainContainer.row();

    // Two-column content
    Table content = new Table();

    Table playerPanel = buildPlayerInventoryPanel();
    playerPanel.setBackground(getPanelDrawable());
    playerPanel.pad(8, 0, 8, 0);
    content.add(playerPanel).pad(8).top();

    Table shopPanel = buildShopPanel();
    shopPanel.setBackground(getPanelDrawable());
    shopPanel.pad(8, 0, 8, 0);
    content.add(shopPanel).pad(8).top().expandY().fillY();

    mainContainer.add(content).expand().fill().pad(5);

    float maxWidth = Math.min(viewport.getWorldWidth() * 0.9f, 680);
    float maxHeight = Math.min(viewport.getWorldHeight() * 0.9f, 560);
    rootTable.getCell(mainContainer).width(maxWidth).height(maxHeight);

    setupClickListeners();
  }

  private Table buildHeader() {
    Table header = new Table();
    Label title = new Label("MERCHANT SHOP", makeLabelStyle(Color.WHITE));
    title.setFontScale(1.2f);
    header.add(title).expandX().center();
    return header;
  }

  private Table buildPlayerInventoryPanel() {
    Table panel = new Table();
    panel.top();

    Label heading = new Label("Your Inventory", makeLabelStyle(Color.WHITE));
    heading.setFontScale(0.9f);
    panel.add(heading).colspan(3).padBottom(5);
    panel.row();

    playerInventorySlots = new ItemSlotWidget[PLAYER_INVENTORY_SIZE];
    playerInventoryNameLabels = new Label[PLAYER_INVENTORY_SIZE];

    for (int i = 0; i < PLAYER_INVENTORY_SIZE; i++) {
      ItemSlotWidget slot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
      playerInventorySlots[i] = slot;

      Label nameLabel = new Label("", makeLabelStyle(Color.WHITE));
      nameLabel.setFontScale(0.6f);
      nameLabel.setAlignment(Align.center);
      nameLabel.setWrap(true);
      playerInventoryNameLabels[i] = nameLabel;

      Table entry = new Table();
      entry.add(slot).size(48, 48);
      entry.row();
      entry.add(nameLabel).width(48).height(28).padTop(2);
      panel.add(entry).pad(1);

      if ((i + 1) % 3 == 0) panel.row();
    }

    return panel;
  }

  private Table buildShopPanel() {
    Table panel = new Table();
    panel.top();

    Label heading = new Label("Shop", makeLabelStyle(Color.WHITE));
    heading.setFontScale(0.9f);
    panel.add(heading).padBottom(5);
    panel.row();

    // Column header
    Table colHeader = new Table();
    colHeader.add(new Label("", makeLabelStyle(Color.LIGHT_GRAY))).width(48).padRight(4);
    Label nameHeader = new Label("Item", makeLabelStyle(Color.LIGHT_GRAY));
    nameHeader.setFontScale(0.75f);
    colHeader.add(nameHeader).width(110).padRight(4);
    Label priceHeader = new Label("Buy", makeLabelStyle(Color.LIGHT_GRAY));
    priceHeader.setFontScale(0.75f);
    colHeader.add(priceHeader).width(40);
    panel.add(colHeader).padBottom(3);
    panel.row();

    // Shop item rows in a scrollable list
    shopListTable = new Table();
    shopListTable.top().left();

    MerchantModel merchant = getMerchant();
    List<ItemEntry> items =
        getShopItems(merchant).stream()
            .filter(e -> merchant == null || merchant.getStock(e.itemClass) > 0)
            .collect(Collectors.toList());
    shopSlots = new ItemSlotWidget[items.size()];

    for (int i = 0; i < items.size(); i++) {
      ItemEntry entry = items.get(i);
      int stock = merchant != null ? merchant.getStock(entry.itemClass) : 0;

      ItemSlotWidget iconSlot = new ItemSlotWidget(slotStyle, font, false, itemTextureManager);
      shopSlots[i] = iconSlot;

      try {
        Class<?> cls = Class.forName(entry.itemClass);
        String displayName = simpleItemName(entry.itemClass);
        iconSlot.setItem(cls, displayName, stock);
      } catch (ClassNotFoundException e) {
        logger.warn("Unknown merchant item class: {}", entry.itemClass);
      }

      Table row = buildShopRow(iconSlot, entry, stock);
      shopListTable.add(row).expandX().fillX().padBottom(2);
      shopListTable.row();
    }

    // Wrap in a scroll pane so it handles many items gracefully
    ScrollPane scrollPane = new ScrollPane(shopListTable);
    scrollPane.setScrollingDisabled(true, false);
    scrollPane.setFadeScrollBars(false);
    scrollPane.setOverscroll(false, false);

    panel.add(scrollPane).expand().fill();

    return panel;
  }

  private Table buildShopRow(ItemSlotWidget iconSlot, ItemEntry entry, int stock) {
    Table row = new Table();
    row.left();

    row.add(iconSlot).size(48, 48).padRight(4);

    Label nameLabel = new Label(simpleItemName(entry.itemClass), makeLabelStyle(Color.WHITE));
    nameLabel.setFontScale(0.75f);
    row.add(nameLabel).width(110).left().padRight(4);

    Label priceLabel =
        new Label(entry.buyPrice + "g", makeLabelStyle(new Color(0.4f, 1f, 0.4f, 1f)));
    priceLabel.setFontScale(0.75f);
    row.add(priceLabel).width(40).right();

    return row;
  }

  // ── Click listeners ──────────────────────────────────────────────────────

  private void setupClickListeners() {
    for (int i = 0; i < playerInventorySlots.length; i++) {
      final int slotIndex = i;
      ItemSlotWidget slot = playerInventorySlots[i];
      slot.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              if (slot.isEmpty()) return;
              showSellDialog(slotIndex);
            }
          });
    }

    MerchantModel merchant = getMerchant();
    List<ItemEntry> items =
        getShopItems(merchant).stream()
            .filter(e -> merchant == null || merchant.getStock(e.itemClass) > 0)
            .collect(Collectors.toList());
    for (int i = 0; i < items.size() && i < shopSlots.length; i++) {
      final ItemEntry entry = items.get(i);
      final ItemSlotWidget shopSlot = shopSlots[i];
      shopSlot.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
              showBuyDialog(entry, simpleItemName(entry.itemClass));
            }
          });
    }
  }

  // ── Dialogs ──────────────────────────────────────────────────────────────

  private void showBuyDialog(ItemEntry entry, String displayName) {
    MerchantModel merchant = getMerchant();
    if (merchant == null) return;
    int maxAmount = merchant.getStock(entry.itemClass);
    if (maxAmount <= 0) return;
    showConfirmDialog(
        "Buy " + displayName,
        entry.buyPrice,
        maxAmount,
        true,
        amount -> executeBuy(entry.itemClass, amount));
  }

  private void showSellDialog(int slotIndex) {
    GameContext ctx = GameContext.get();
    PlayerModel player = (PlayerModel) ctx.modelManager.getModel(ctx.username);
    if (player == null) return;
    Item item = player.getInventory().getItem(slotIndex);
    if (item == null) return;
    String itemClass = item.getClass().getName();
    int sellPrice = MerchantConfigLoader.get().getSellPrice(itemClass);
    showConfirmDialog(
        "Sell " + simpleItemName(itemClass),
        sellPrice,
        item.amount,
        false,
        amount -> executeSell(slotIndex, amount));
  }

  private void showConfirmDialog(
      String title, int pricePerUnit, int maxAmount, boolean isBuy, IntConsumer onConfirm) {
    if (confirmDialog != null) confirmDialog.remove();

    int[] amount = {1};

    Color previewColor = isBuy ? new Color(1f, 0.4f, 0.4f, 1f) : new Color(0.4f, 1f, 0.4f, 1f);
    String prefix = isBuy ? "-" : "+";

    // Dim overlay
    Table overlay = new Table();
    overlay.setFillParent(true);
    Pixmap dimPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    dimPixmap.setColor(0f, 0f, 0f, 0.55f);
    dimPixmap.fill();
    Texture dimTexture = new Texture(dimPixmap);
    dimPixmap.dispose();
    overlay.setBackground(new TextureRegionDrawable(dimTexture));
    overlay.setTouchable(Touchable.enabled);

    // Dialog box
    Table dialog = new Table();
    Pixmap boxPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    boxPixmap.setColor(0.15f, 0.15f, 0.15f, 0.97f);
    boxPixmap.fill();
    Texture boxTexture = new Texture(boxPixmap);
    boxPixmap.dispose();
    dialog.setBackground(new TextureRegionDrawable(boxTexture));
    dialog.pad(32);

    // Title
    Label titleLabel = new Label(title, makeLabelStyle(Color.WHITE));
    titleLabel.setFontScale(1.0f);
    dialog.add(titleLabel).colspan(3).center().padBottom(12);
    dialog.row();

    // Gold preview
    Label goldPreview =
        new Label(prefix + (pricePerUnit * amount[0]) + "g", makeLabelStyle(previewColor));
    goldPreview.setFontScale(0.9f);
    dialog.add(goldPreview).colspan(3).center().padBottom(24);
    dialog.row();

    // Amount row
    Label minusBtn = new Label("  -  ", makeLabelStyle(Color.YELLOW));
    minusBtn.setFontScale(1.1f);
    Label amountLabel = new Label(String.valueOf(amount[0]), makeLabelStyle(Color.WHITE));
    amountLabel.setFontScale(0.9f);
    amountLabel.setAlignment(Align.center);
    Label plusBtn = new Label("  +  ", makeLabelStyle(Color.YELLOW));
    plusBtn.setFontScale(1.1f);

    minusBtn.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (amount[0] > 1) {
              amount[0]--;
              amountLabel.setText(String.valueOf(amount[0]));
              goldPreview.setText(prefix + (pricePerUnit * amount[0]) + "g");
            }
          }
        });
    plusBtn.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (amount[0] < maxAmount) {
              amount[0]++;
              amountLabel.setText(String.valueOf(amount[0]));
              goldPreview.setText(prefix + (pricePerUnit * amount[0]) + "g");
            }
          }
        });

    dialog.add(minusBtn).padRight(16);
    dialog.add(amountLabel).width(60).center();
    dialog.add(plusBtn).padLeft(16);
    dialog.row();

    // Cancel / Confirm buttons
    Table btnRow = new Table();
    Label cancelBtn = new Label("  Cancel  ", makeLabelStyle(Color.LIGHT_GRAY));
    cancelBtn.setFontScale(0.85f);
    cancelBtn.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            overlay.remove();
            confirmDialog = null;
          }
        });
    Label confirmBtn = new Label("  Confirm  ", makeLabelStyle(new Color(0.4f, 1f, 0.4f, 1f)));
    confirmBtn.setFontScale(0.85f);
    confirmBtn.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            onConfirm.accept(amount[0]);
            overlay.remove();
            confirmDialog = null;
          }
        });
    btnRow.add(cancelBtn).padRight(16);
    btnRow.add(confirmBtn);
    dialog.add(btnRow).colspan(3).center().padTop(40);

    overlay.add(dialog);
    overlay.addCaptureListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Actor target = event.getTarget();
            if (target != dialog && !isDescendantOf(target, dialog)) {
              overlay.remove();
              confirmDialog = null;
              event.stop();
              return true;
            }
            return false;
          }
        });

    confirmDialog = overlay;
    rootTable.getStage().addActor(overlay);
  }

  // ── Commands ─────────────────────────────────────────────────────────────

  private void executeBuy(String itemClass, int amount) {
    GameContext ctx = GameContext.get();
    ctx.controlManager.processInput(
        new BuyItemCommand(ctx.username, merchantModelId, itemClass, amount));
    scheduleRefresh();
  }

  private void executeSell(int inventorySlot, int amount) {
    GameContext ctx = GameContext.get();
    ctx.controlManager.processInput(
        new SellItemCommand(ctx.username, merchantModelId, inventorySlot, amount));
    scheduleRefresh();
  }

  // ── Refresh ──────────────────────────────────────────────────────────────

  public void refresh() {
    GameContext ctx = GameContext.get();
    PlayerModel player = (PlayerModel) ctx.modelManager.getModel(ctx.username);
    if (player == null) return;

    // Gold coins
    int gold = countGold(player);
    if (goldLabel != null) {
      goldLabel.setText("Gold: " + gold);
    }

    // Player inventory
    Inventory inv = player.getInventory();
    for (int i = 0; i < PLAYER_INVENTORY_SIZE; i++) {
      Item item = inv.getItem(i);
      if (item != null) {
        playerInventorySlots[i].setItem(item, item.amount);
        playerInventoryNameLabels[i].setText(simpleItemName(item.getClass().getName()));
      } else {
        playerInventorySlots[i].clear();
        playerInventoryNameLabels[i].setText("");
      }
    }
  }

  // ── Public API ───────────────────────────────────────────────────────────

  public void setMerchant(String merchantModelId) {
    this.merchantModelId = merchantModelId;
    rebuildUI();
  }

  public Actor getRoot() {
    return rootTable;
  }

  public void dispose() {
    if (backgroundTexture != null) backgroundTexture.dispose();
    if (panelTexture != null) panelTexture.dispose();
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private List<ItemEntry> getShopItems(MerchantModel merchant) {
    List<ItemEntry> result = new ArrayList<>();
    if (merchant == null) return result;
    for (String itemClass : merchant.stock.keySet()) {
      ItemEntry entry = MerchantConfigLoader.get().getEntry(itemClass);
      if (entry != null) result.add(entry);
    }
    return result;
  }

  private MerchantModel getMerchant() {
    Model model = GameContext.get().modelManager.getModel(merchantModelId);
    return model instanceof MerchantModel ? (MerchantModel) model : null;
  }

  private int countGold(PlayerModel player) {
    try {
      Class<?> goldClass = Class.forName("com.noiprocs.gameplay.model.item.GoldCoinItem");
      return player.getInventory().countItems(goldClass);
    } catch (ClassNotFoundException e) {
      return 0;
    }
  }

  private static String simpleItemName(String fullClass) {
    int dot = fullClass.lastIndexOf('.');
    String name = dot >= 0 ? fullClass.substring(dot + 1) : fullClass;
    if (name.endsWith("Item")) name = name.substring(0, name.length() - 4);
    return name.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
  }

  private Label.LabelStyle makeLabelStyle(Color color) {
    Label.LabelStyle style = new Label.LabelStyle();
    style.font = font;
    style.fontColor = color;
    return style;
  }

  private Drawable getPanelDrawable() {
    if (panelTexture == null) {
      Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
      pixmap.setColor(0.18f, 0.18f, 0.18f, 0.9f);
      pixmap.fill();
      panelTexture = new Texture(pixmap);
      pixmap.dispose();
    }
    return new TextureRegionDrawable(panelTexture);
  }

  private Drawable createBackground() {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(0.1f, 0.1f, 0.1f, 0.9f);
    pixmap.fill();
    backgroundTexture = new Texture(pixmap);
    pixmap.dispose();
    return new TextureRegionDrawable(backgroundTexture);
  }

  private void close() {
    hudManager.close();
  }

  private void scheduleRefresh() {
    new Thread(
            () -> {
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
              rebuildUI();
              refresh();
            })
        .start();
  }

  private static boolean isDescendantOf(Actor actor, Actor parent) {
    Actor current = actor;
    while (current != null) {
      if (current == parent) return true;
      current = current.getParent();
    }
    return false;
  }
}
