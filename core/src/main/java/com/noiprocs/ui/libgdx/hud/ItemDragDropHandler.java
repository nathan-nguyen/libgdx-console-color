package com.noiprocs.ui.libgdx.hud;

import com.noiprocs.core.GameContext;
import com.noiprocs.core.control.command.CraftCommand;
import com.noiprocs.core.control.command.EquipCommand;
import com.noiprocs.core.control.command.SwapInventoryCommand;
import com.noiprocs.core.control.command.UnequipCommand;
import com.noiprocs.core.model.item.Item;
import com.noiprocs.core.model.item.ItemCategory;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized drag-drop logic with validation and command execution. Handles validation for
 * equipment slots, inventory slots, crafting materials, and cross-container transfers.
 */
public class ItemDragDropHandler {
  private static final Logger logger = LoggerFactory.getLogger(ItemDragDropHandler.class);
  private final Map<String, Long> slotTypeToCategory;

  public ItemDragDropHandler() {
    this.slotTypeToCategory = new HashMap<>();
    slotTypeToCategory.put("HELMET", ItemCategory.HELMET);
    slotTypeToCategory.put("CHEST PLATE", ItemCategory.CHEST_PLATE);
    slotTypeToCategory.put("LEGGING", ItemCategory.LEGGING);
    slotTypeToCategory.put("BOOT", ItemCategory.BOOT);
  }

  /**
   * Validates if an item can be equipped in a specific equipment slot.
   *
   * @param item Item to equip
   * @param targetSlotType Target equipment slot type (e.g., "HELMET", "CHEST PLATE")
   * @return true if the item can be equipped in the slot
   */
  public boolean canEquip(Object item, String targetSlotType) {
    if (item == null || targetSlotType == null) {
      return false;
    }

    String itemStr = item.toString().toUpperCase();

    switch (targetSlotType) {
      case "HELMET":
        return itemStr.contains("HELMET") || itemStr.contains("HAT") || itemStr.contains("HOOD");
      case "CHEST PLATE":
        return itemStr.contains("CHEST")
            || itemStr.contains("CHESTPLATE")
            || itemStr.contains("ARMOR");
      case "LEGGING":
        return itemStr.contains("LEGS") || itemStr.contains("PANTS") || itemStr.contains("LEGGING");
      case "BOOT":
        return itemStr.contains("BOOT") || itemStr.contains("SHOE") || itemStr.contains("FEET");
      default:
        return false;
    }
  }

  /**
   * Validates if an item can be placed in an inventory slot (always true for inventory).
   *
   * @param item Item to place
   * @return true (inventory accepts any item)
   */
  public boolean canPlaceInInventory(Object item) {
    return item != null;
  }

  /**
   * Executes an equip command for an item from an inventory slot.
   *
   * @param inventorySlot Inventory slot index containing the item to equip
   * @param equipmentSlotType Equipment slot type
   */
  public void executeEquip(int inventorySlot, String equipmentSlotType) {
    executeEquip(GameContext.get().username, inventorySlot, equipmentSlotType);
  }

  /**
   * Executes an equip command for an item from an inventory slot to a target humanoid.
   *
   * @param targetModelId Target humanoid model ID
   * @param inventorySlot Inventory slot index containing the item to equip
   * @param equipmentSlotType Equipment slot type
   */
  public void executeEquip(String targetModelId, int inventorySlot, String equipmentSlotType) {
    if (equipmentSlotType == null) {
      logger.warn("Cannot equip to null slot");
      return;
    }

    GameContext gameContext = GameContext.get();
    EquipCommand command = new EquipCommand(gameContext.username, targetModelId, inventorySlot);
    gameContext.controlManager.processInput(command);
    logger.info(
        "Equipping from inventory slot {} to {} slot on {}",
        inventorySlot,
        equipmentSlotType,
        targetModelId);
  }

  /**
   * Executes an unequip command for an equipment slot.
   *
   * @param equipmentSlotType Equipment slot type to unequip
   */
  public void executeUnequip(String equipmentSlotType) {
    executeUnequip(GameContext.get().username, equipmentSlotType);
  }

  /**
   * Executes an unequip command for an equipment slot on a target humanoid.
   *
   * @param targetModelId Target humanoid model ID
   * @param equipmentSlotType Equipment slot type to unequip
   */
  public void executeUnequip(String targetModelId, String equipmentSlotType) {
    if (equipmentSlotType == null) {
      logger.warn("Cannot unequip from null slot");
      return;
    }

    Long categoryId = slotTypeToCategory.get(equipmentSlotType);
    if (categoryId == null) {
      logger.warn("Unknown equipment slot type: {}", equipmentSlotType);
      return;
    }

    GameContext gameContext = GameContext.get();
    UnequipCommand command = new UnequipCommand(gameContext.username, targetModelId, categoryId);
    gameContext.controlManager.processInput(command);
    logger.info(
        "Unequipping from {} slot (category {}) on {}",
        equipmentSlotType,
        categoryId,
        targetModelId);
  }

  /**
   * Executes a transfer command between containers.
   *
   * @param item Item to transfer
   * @param sourceContainerId Source container ID ("PLAYER" or "CONTAINER")
   * @param targetContainerId Target container ID ("PLAYER" or "CONTAINER")
   * @param sourceSlotIndex Source slot index
   * @param targetSlotIndex Target slot index
   * @param containerModelId The actual container/chest model ID
   */
  public void executeTransfer(
      Object item,
      String sourceContainerId,
      String targetContainerId,
      int sourceSlotIndex,
      int targetSlotIndex,
      String containerModelId) {
    if (item == null) {
      logger.warn("Cannot transfer null item");
      return;
    }

    if (!(item instanceof Item)) {
      logger.warn("Invalid item type for transfer");
      return;
    }

    Item transferItem = (Item) item;

    boolean fromPlayer = "PLAYER".equals(sourceContainerId);

    GameContext.get()
        .controlManager
        .transferItem(containerModelId, sourceSlotIndex, transferItem.amount, fromPlayer);

    logger.info(
        "Transferring {} from {}[{}] to {}[{}]",
        item,
        sourceContainerId,
        sourceSlotIndex,
        targetContainerId,
        targetSlotIndex);
  }

  /**
   * Executes a swap command between two inventory slots.
   *
   * @param slot1Index First slot index
   * @param slot2Index Second slot index
   */
  public void executeSwap(int slot1Index, int slot2Index) {
    GameContext gameContext = GameContext.get();
    SwapInventoryCommand command =
        new SwapInventoryCommand(gameContext.username, slot1Index, slot2Index);
    gameContext.controlManager.processInput(command);
    logger.info("Swapping items at slots {} and {}", slot1Index, slot2Index);
  }

  /**
   * Disposes (permanently deletes) an item from the player's inventory.
   *
   * @param slotIndex Inventory slot index of the item to dispose
   */
  public void executeDispose(int slotIndex) {
    GameContext.get().controlManager.disposeItem(slotIndex);
    logger.info("Disposing item at player inventory slot {}", slotIndex);
  }

  /**
   * Executes a crafting command.
   *
   * @param targetItemClassName Fully qualified class name of the item to craft
   */
  public void executeCraft(String targetItemClassName) {
    if (targetItemClassName == null) {
      logger.warn("Cannot craft with null item class name");
      return;
    }

    GameContext gameContext = GameContext.get();
    CraftCommand command = new CraftCommand(gameContext.username, targetItemClassName);
    gameContext.controlManager.processInput(command);
    logger.info("Crafting item: {}", targetItemClassName);
  }
}
