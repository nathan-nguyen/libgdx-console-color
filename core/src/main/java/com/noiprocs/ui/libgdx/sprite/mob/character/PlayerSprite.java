package com.noiprocs.ui.libgdx.sprite.mob.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Config;
import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.action.AnimatedAction;
import com.noiprocs.core.model.action.AttackAction;
import com.noiprocs.core.model.action.InteractAction;
import com.noiprocs.core.model.item.Item;
import com.noiprocs.core.model.item.ItemModel;
import com.noiprocs.core.model.item.PlacableItem;
import com.noiprocs.core.model.item.ThrowableItemInterface;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.gameplay.model.environment.BoulderModel;
import com.noiprocs.gameplay.model.environment.ThickVineModel;
import com.noiprocs.gameplay.model.item.AxeItem;
import com.noiprocs.gameplay.model.mob.BombGoblinModel;
import com.noiprocs.gameplay.model.mob.GoblinModel;
import com.noiprocs.gameplay.model.mob.RangeGoblinModel;
import com.noiprocs.gameplay.model.plant.TreeModel;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.ui.libgdx.sprite.LibgdxRenderContext;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class PlayerSprite extends LibgdxSprite {
  private static final String MODEL_CLASS = PlayerModel.class.getName();
  private static final float THROW_ARROW_LENGTH = 10.0f;

  // Texture arrays indexed as [SW=0, SE=1, NE=2, NW=3]
  private static final LibgdxTexture[] STAND;
  private static final LibgdxTexture[] ACTION;
  private static final LibgdxTexture[] CHOP_TREE;
  private static final LibgdxTexture[] PICKUP;

  static {
    LibgdxTexture standSw = loadTexture(MODEL_CLASS, "stand_sw");
    LibgdxTexture standNe = loadTexture(MODEL_CLASS, "stand_ne");
    STAND = new LibgdxTexture[] {standSw, standSw.flipped(), standNe, standNe.flipped()};

    LibgdxTexture actionSw = loadTexture(MODEL_CLASS, "action_sw");
    LibgdxTexture actionNe = loadTexture(MODEL_CLASS, "action_ne");
    ACTION = new LibgdxTexture[] {actionSw, actionSw.flipped(), actionNe, actionNe.flipped()};

    LibgdxTexture chopSw = loadTexture(MODEL_CLASS, "chop_tree_sw");
    LibgdxTexture chopNe = loadTexture(MODEL_CLASS, "chop_tree_ne");
    CHOP_TREE = new LibgdxTexture[] {chopSw, chopSw.flipped(), chopNe, chopNe.flipped()};

    LibgdxTexture pickupSw = loadTexture(MODEL_CLASS, "pickup_sw");
    LibgdxTexture pickupNw = loadTexture(MODEL_CLASS, "pickup_nw");
    PICKUP = new LibgdxTexture[] {pickupSw, pickupSw.flipped(), pickupNw.flipped(), pickupNw};
  }

  public PlayerSprite() {
    super(STAND[0]);
  }

  @Override
  public LibgdxTexture getTexture(Model model) {
    PlayerModel playerModel = (PlayerModel) model;
    Vector3D dir = playerModel.getFacingDirection();

    if (isPickingUp(playerModel)) return selectByDirection(dir, PICKUP);
    if (isChoppingTree(playerModel)) return selectByDirection(dir, CHOP_TREE);
    if (playerModel.getAction() instanceof AnimatedAction) return selectByDirection(dir, ACTION);
    return selectByDirection(dir, STAND);
  }

  private boolean isPickingUp(PlayerModel playerModel) {
    if (!(playerModel.getAction() instanceof InteractAction)) return false;
    InteractAction action = (InteractAction) playerModel.getAction();
    Model target = GameContext.get().modelManager.getModel(action.targetId);
    return target == null || target instanceof ItemModel;
  }

  private boolean isChoppingTree(PlayerModel playerModel) {
    if (!(playerModel.getHoldingItem() instanceof AxeItem)) return false;
    if (playerModel.getAction() instanceof AttackAction) return true;
    if (playerModel.getAction() instanceof InteractAction) {
      InteractAction action = (InteractAction) playerModel.getAction();
      Model target = GameContext.get().modelManager.getModel(action.targetId);
      return target instanceof TreeModel
          || target instanceof GoblinModel
          || target instanceof RangeGoblinModel
          || target instanceof BombGoblinModel
          || target instanceof ThickVineModel
          || target instanceof BoulderModel;
    }
    return false;
  }

  private LibgdxTexture selectByDirection(Vector3D dir, LibgdxTexture[] textures) {
    if (dir.x + dir.y < 0) return textures[dir.y < 0 ? 3 : 2]; // NW=3, NE=2
    return textures[dir.y > 0 ? 1 : 0]; // SE=1, SW=0
  }

  @Override
  public void render(
      SpriteBatch batch,
      Model model,
      Model playerModel,
      float offsetX,
      float offsetY,
      LibgdxRenderContext ctx) {
    super.render(batch, model, playerModel, offsetX, offsetY, ctx);
    Item item = ((PlayerModel) model).getHoldingItem();
    if (item instanceof PlacableItem) renderPlacementPreview(batch, model, offsetX, offsetY, ctx);
    else if (item instanceof ThrowableItemInterface)
      renderThrowArrow(batch, model, offsetX, offsetY, ctx);
  }

  private void renderThrowArrow(
      SpriteBatch batch, Model model, float offsetX, float offsetY, LibgdxRenderContext ctx) {
    PlayerModel playerModel = (PlayerModel) model;
    Vector3D movingDir = playerModel.getMovingDirection();
    if (movingDir.equals(Vector3D.ZERO)) return;

    Vector3D center = GameContext.get().hitboxManager.getHitboxCenter(model);
    float cx = (float) center.x / Config.WORLD_SCALE - offsetX;
    float cy = (float) center.y / Config.WORLD_SCALE - offsetY;
    float dirLen = (float) Math.sqrt(movingDir.x * movingDir.x + movingDir.y * movingDir.y);
    float ex = cx + (movingDir.x / dirLen) * THROW_ARROW_LENGTH;
    float ey = cy + (movingDir.y / dirLen) * THROW_ARROW_LENGTH;

    float startSX = isoScreenX(cx, cy, ctx);
    float startSY = isoScreenY(cx, cy, ctx);
    float endSX = isoScreenX(ex, ey, ctx);
    float endSY = isoScreenY(ex, ey, ctx);

    ShapeRenderer sr = ctx.shapeRenderer;
    beginShapeRendering(batch, ctx);

    sr.begin(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glLineWidth(2f);
    sr.setColor(Color.CYAN);
    sr.line(startSX, startSY, endSX, endSY);

    float arrowDirSX = endSX - startSX;
    float arrowDirSY = endSY - startSY;
    float len = (float) Math.sqrt(arrowDirSX * arrowDirSX + arrowDirSY * arrowDirSY);
    if (len > 0) {
      float nx = arrowDirSX / len;
      float ny = arrowDirSY / len;
      float headLen = 0.4f * UIConfig.CHAR_SIZE;
      sr.line(
          endSX,
          endSY,
          endSX - nx * headLen + ny * headLen * 0.5f,
          endSY - ny * headLen - nx * headLen * 0.5f);
      sr.line(
          endSX,
          endSY,
          endSX - nx * headLen - ny * headLen * 0.5f,
          endSY - ny * headLen + nx * headLen * 0.5f);
    }
    sr.end();

    endShapeRendering(batch);
  }

  private void renderPlacementPreview(
      SpriteBatch batch, Model model, float offsetX, float offsetY, LibgdxRenderContext ctx) {
    PlacableItem placableItem = (PlacableItem) ((PlayerModel) model).getHoldingItem();
    Vector3D dim = GameContext.get().hitboxManager.getHitboxDimension(placableItem);
    Model ghostModel = placableItem.createPlacedModel(model.position);
    boolean valid = GameContext.get().hitboxManager.isValid(ghostModel, ghostModel.position);

    float posX = (float) model.position.x / Config.WORLD_SCALE - offsetX;
    float posY = (float) model.position.y / Config.WORLD_SCALE - offsetY;
    float h = (float) dim.x / Config.WORLD_SCALE;
    float w = (float) dim.y / Config.WORLD_SCALE;

    // 4 corners of the ground footprint in isometric screen space
    float tlsx = isoScreenX(posX, posY, ctx);
    float tlsy = isoScreenY(posX, posY, ctx);
    float trsx = isoScreenX(posX, posY + w, ctx);
    float trsy = isoScreenY(posX, posY + w, ctx);
    float blsx = isoScreenX(posX + h, posY, ctx);
    float blsy = isoScreenY(posX + h, posY, ctx);
    float brsx = isoScreenX(posX + h, posY + w, ctx);
    float brsy = isoScreenY(posX + h, posY + w, ctx);

    Color color = valid ? Color.GREEN : Color.RED;
    ShapeRenderer sr = ctx.shapeRenderer;

    beginShapeRendering(batch, ctx);

    sr.begin(ShapeRenderer.ShapeType.Filled);
    sr.setColor(color.r, color.g, color.b, 0.3f);
    sr.triangle(tlsx, tlsy, trsx, trsy, brsx, brsy);
    sr.triangle(tlsx, tlsy, brsx, brsy, blsx, blsy);
    sr.end();

    sr.begin(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glLineWidth(2f);
    sr.setColor(color.r, color.g, color.b, 0.8f);
    sr.line(tlsx, tlsy, trsx, trsy);
    sr.line(trsx, trsy, brsx, brsy);
    sr.line(brsx, brsy, blsx, blsy);
    sr.line(blsx, blsy, tlsx, tlsy);
    sr.end();

    endShapeRendering(batch);
  }

  private static float isoScreenX(float gridX, float gridY, LibgdxRenderContext ctx) {
    float hw = (ctx.height + ctx.width) / 2f;
    return (gridY - gridX) * UIConfig.CHAR_SIZE / 2f + UIConfig.CHAR_SIZE * hw / 2f;
  }

  private static float isoScreenY(float gridX, float gridY, LibgdxRenderContext ctx) {
    float hw = (ctx.height + ctx.width) / 2f;
    return ctx.virtualHeight / 2f + UIConfig.CHAR_SIZE / 4f * (hw - gridX - gridY);
  }

  private static void beginShapeRendering(SpriteBatch batch, LibgdxRenderContext ctx) {
    batch.end();
    ctx.shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
    Gdx.gl.glEnable(GL20.GL_BLEND);
    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
  }

  private static void endShapeRendering(SpriteBatch batch) {
    Gdx.gl.glDisable(GL20.GL_BLEND);
    batch.begin();
  }
}
