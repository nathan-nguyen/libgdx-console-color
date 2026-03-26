package com.noiprocs.ui.libgdx.sprite.mob.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Config;
import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.hitbox.HitboxManagerInterface.MovableDistanceResult;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.action.AnimatedAction;
import com.noiprocs.core.model.action.AttackAction;
import com.noiprocs.core.model.action.InteractAction;
import com.noiprocs.core.model.item.Item;
import com.noiprocs.core.model.item.ItemModel;
import com.noiprocs.core.model.item.PlacableItem;
import com.noiprocs.core.model.item.ThrowableItemInterface;
import com.noiprocs.core.model.mob.MobModel;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.gameplay.model.environment.BoulderModel;
import com.noiprocs.gameplay.model.environment.ThickVineModel;
import com.noiprocs.gameplay.model.item.AxeItem;
import com.noiprocs.gameplay.model.mob.BlackSmithModel;
import com.noiprocs.gameplay.model.mob.MerchantModel;
import com.noiprocs.gameplay.model.plant.TreeModel;
import com.noiprocs.input.ThrowAimState;
import com.noiprocs.resources.UIConfig;
import com.noiprocs.ui.libgdx.sprite.LibgdxRenderContext;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class PlayerSprite extends LibgdxSprite {
  private static final String MODEL_CLASS = PlayerModel.class.getName();

  private static final long WALK_FRAME_MS = 300;

  // Texture arrays indexed as [SW=0, SE=1, NE=2, NW=3]
  // WALK_0[0] (stand_sw) is this.texture (set by super())
  private final LibgdxTexture[] WALK_0;
  private final LibgdxTexture[] WALK_1;
  private final LibgdxTexture[] ACTION;
  private final LibgdxTexture[] CHOP_TREE;
  private final LibgdxTexture[] PICKUP;

  {
    LibgdxTexture standNe = loadTexture(MODEL_CLASS, "stand_ne");
    WALK_0 = new LibgdxTexture[] {texture, texture.flipped(), standNe, standNe.flipped()};

    LibgdxTexture walkSw1 = loadTexture(MODEL_CLASS, "walk_sw_1");
    LibgdxTexture walkNe1 = loadTexture(MODEL_CLASS, "walk_ne_1");
    WALK_1 = new LibgdxTexture[] {walkSw1, walkSw1.flipped(), walkNe1, walkNe1.flipped()};

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
    super(loadTexture(MODEL_CLASS, "stand_sw"));
  }

  @Override
  public LibgdxTexture getTexture(Model model) {
    PlayerModel playerModel = (PlayerModel) model;
    Vector3D dir = playerModel.getFacingDirection();

    if (isPickingUp(playerModel)) return selectByDirection(dir, PICKUP);
    if (isChoppingTree(playerModel)) return selectByDirection(dir, CHOP_TREE);
    if (playerModel.getAction() instanceof AnimatedAction) return selectByDirection(dir, ACTION);
    Vector3D movingDir = playerModel.getMovingDirection();
    if (!movingDir.equals(Vector3D.ZERO)) {
      long frame = (System.currentTimeMillis() / WALK_FRAME_MS) % 2;
      return selectByDirection(dir, frame == 0 ? WALK_0 : WALK_1);
    }
    return selectByDirection(dir, WALK_0);
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
      if (action == null) return false;
      Model target = GameContext.get().modelManager.getModel(action.targetId);
      return target instanceof TreeModel
          || (target instanceof MobModel
              && !(target instanceof MerchantModel)
              && !(target instanceof BlackSmithModel))
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
    else if (item instanceof ThrowableItemInterface && ThrowAimState.getAimDirection() != null)
      renderProjectilePath(batch, model, offsetX, offsetY, ctx);
  }

  private void renderProjectilePath(
      SpriteBatch batch, Model model, float offsetX, float offsetY, LibgdxRenderContext ctx) {
    PlayerModel playerModel = (PlayerModel) model;
    ThrowableItemInterface throwable = (ThrowableItemInterface) playerModel.getHoldingItem();

    Vector3D movingDir = ThrowAimState.getAimDirection();

    Vector3D center = GameContext.get().hitboxManager.getHitboxCenter(model);
    float cx = (float) center.x / Config.WORLD_SCALE - offsetX;
    float cy = (float) center.y / Config.WORLD_SCALE - offsetY;

    float dirLen = (float) Math.sqrt(movingDir.x * movingDir.x + movingDir.y * movingDir.y);
    int range = throwable.getProjectileRange();
    Vector3D totalMovement =
        new Vector3D((int) (movingDir.x * range / dirLen), (int) (movingDir.y * range / dirLen), 0);
    MovableDistanceResult sweep =
        GameContext.get()
            .hitboxManager
            .getMovableDistance(throwable.getProjectileModelClass(), center, totalMovement);
    Vector3D actualEnd = center.add(sweep.distance);
    float ex = (float) actualEnd.x / Config.WORLD_SCALE - offsetX;
    float ey = (float) actualEnd.y / Config.WORLD_SCALE - offsetY;

    Vector3D dim = GameContext.get().hitboxManager.getHitboxDimension(throwable);
    float h = (float) dim.x / Config.WORLD_SCALE;
    float w = (float) dim.y / Config.WORLD_SCALE;

    // Start footprint corners (centered on spawn point)
    float sPosX = cx - h / 2f;
    float sPosY = cy - w / 2f;
    float stlsx = isoScreenX(sPosX, sPosY, ctx);
    float stlsy = isoScreenY(sPosX, sPosY, ctx);
    float strsx = isoScreenX(sPosX, sPosY + w, ctx);
    float strsy = isoScreenY(sPosX, sPosY + w, ctx);
    float sblsx = isoScreenX(sPosX + h, sPosY, ctx);
    float sblsy = isoScreenY(sPosX + h, sPosY, ctx);
    float sbrsx = isoScreenX(sPosX + h, sPosY + w, ctx);
    float sbrsy = isoScreenY(sPosX + h, sPosY + w, ctx);

    // End footprint corners (centered on landing point)
    float ePosX = ex - h / 2f;
    float ePosY = ey - w / 2f;
    float etlsx = isoScreenX(ePosX, ePosY, ctx);
    float etlsy = isoScreenY(ePosX, ePosY, ctx);
    float etrsx = isoScreenX(ePosX, ePosY + w, ctx);
    float etrsy = isoScreenY(ePosX, ePosY + w, ctx);
    float eblsx = isoScreenX(ePosX + h, ePosY, ctx);
    float eblsy = isoScreenY(ePosX + h, ePosY, ctx);
    float ebrsx = isoScreenX(ePosX + h, ePosY + w, ctx);
    float ebrsy = isoScreenY(ePosX + h, ePosY + w, ctx);

    ShapeRenderer sr = ctx.shapeRenderer;
    beginShapeRendering(batch, ctx);

    // Fill: start cap + end cap + 4 connecting strips
    sr.begin(ShapeRenderer.ShapeType.Filled);
    sr.setColor(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, 0.3f);
    sr.triangle(stlsx, stlsy, strsx, strsy, sbrsx, sbrsy);
    sr.triangle(stlsx, stlsy, sbrsx, sbrsy, sblsx, sblsy);
    sr.triangle(etlsx, etlsy, etrsx, etrsy, ebrsx, ebrsy);
    sr.triangle(etlsx, etlsy, ebrsx, ebrsy, eblsx, eblsy);
    sr.triangle(stlsx, stlsy, strsx, strsy, etrsx, etrsy);
    sr.triangle(stlsx, stlsy, etrsx, etrsy, etlsx, etlsy);
    sr.triangle(strsx, strsy, sbrsx, sbrsy, ebrsx, ebrsy);
    sr.triangle(strsx, strsy, ebrsx, ebrsy, etrsx, etrsy);
    sr.triangle(sbrsx, sbrsy, sblsx, sblsy, eblsx, eblsy);
    sr.triangle(sbrsx, sbrsy, eblsx, eblsy, ebrsx, ebrsy);
    sr.triangle(sblsx, sblsy, stlsx, stlsy, etlsx, etlsy);
    sr.triangle(sblsx, sblsy, etlsx, etlsy, eblsx, eblsy);
    sr.end();

    // Border: convex hull of swept path. Corners CW: [TL=0, TR=1, BR=2, BL=3].
    // Hull visits sc[b]→sc[b+1]→ec[b+1]→ec[b+2]→ec[b+3]→sc[b+3] where b = back corner index.
    float[] scx = {stlsx, strsx, sbrsx, sblsx};
    float[] scy = {stlsy, strsy, sbrsy, sblsy};
    float[] ecx = {etlsx, etrsx, ebrsx, eblsx};
    float[] ecy = {etlsy, etrsy, ebrsy, eblsy};
    int b;
    if (movingDir.x >= 0 && movingDir.y >= 0) b = 0;
    else if (movingDir.x >= 0) b = 1;
    else if (movingDir.y >= 0) b = 3;
    else b = 2;
    float[] hx = {
      scx[b],
      scx[(b + 1) % 4],
      ecx[(b + 1) % 4],
      ecx[(b + 2) % 4],
      ecx[(b + 3) % 4],
      scx[(b + 3) % 4]
    };
    float[] hy = {
      scy[b],
      scy[(b + 1) % 4],
      ecy[(b + 1) % 4],
      ecy[(b + 2) % 4],
      ecy[(b + 3) % 4],
      scy[(b + 3) % 4]
    };
    sr.begin(ShapeRenderer.ShapeType.Line);
    Gdx.gl.glLineWidth(2f);
    sr.setColor(Color.CYAN.r, Color.CYAN.g, Color.CYAN.b, 0.8f);
    for (int i = 0; i < 6; i++) {
      int n = (i + 1) % 6;
      sr.line(hx[i], hy[i], hx[n], hy[n]);
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
