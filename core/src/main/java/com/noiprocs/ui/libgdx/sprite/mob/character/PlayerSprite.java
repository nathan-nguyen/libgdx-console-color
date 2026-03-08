package com.noiprocs.ui.libgdx.sprite.mob.character;

import com.noiprocs.core.GameContext;
import com.noiprocs.core.common.Vector3D;
import com.noiprocs.core.model.Model;
import com.noiprocs.core.model.action.AnimatedAction;
import com.noiprocs.core.model.action.AttackAction;
import com.noiprocs.core.model.action.InteractAction;
import com.noiprocs.core.model.item.ItemModel;
import com.noiprocs.core.model.mob.character.PlayerModel;
import com.noiprocs.gameplay.model.item.AxeItem;
import com.noiprocs.gameplay.model.mob.GoblinModel;
import com.noiprocs.gameplay.model.plant.TreeModel;
import com.noiprocs.ui.libgdx.sprite.LibgdxSprite;
import com.noiprocs.ui.libgdx.sprite.LibgdxTexture;

public class PlayerSprite extends LibgdxSprite {
  private static final String MODEL_CLASS = PlayerModel.class.getName();

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
      return target instanceof TreeModel || target instanceof GoblinModel;
    }
    return false;
  }

  private LibgdxTexture selectByDirection(Vector3D dir, LibgdxTexture[] textures) {
    if (dir.x + dir.y < 0) return textures[dir.y < 0 ? 3 : 2]; // NW=3, NE=2
    return textures[dir.y > 0 ? 1 : 0]; // SE=1, SW=0
  }
}
