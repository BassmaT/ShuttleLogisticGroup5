package com.group5.shuttle.service;

import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.StockStatus;

import java.util.List;

// Single responsibility: manages the inventory logic for repair tasks.
// Checks availability, books parts in/out, and schedules deliveries.
// Extracted from MissionControlController (business logic was previously in a UI controller).
public final class RepairInventoryService {

    private RepairInventoryService() {}

    // Sets partStatus and partAvailable for each task based on the current inventory.
    // Tasks with an ongoing order (ORDERED/ARRIVED) are skipped.
    public static void initTaskStatuses(List<RepairTask> tasks, IInventoryService inventoryService) {
        List<InventoryItem> inventory = inventoryService.loadInventory();
        for (RepairTask task : tasks) {
            StockStatus existing = StockStatus.valueOf(task.getPartStatus());
            if (existing.skipsStatusInit()) continue;

            if (task.getRequiredItemName() == null) {
                task.setPartStatus(StockStatus.NONE);
                task.setPartAvailable(true);
            } else {
                InventoryItem item = inventory.stream()
                        .filter(i -> i.getName().equals(task.getRequiredItemName()))
                        .findFirst().orElse(null);
                if (item != null && item.getQuantity() > 0) {
                    task.setPartStatus(StockStatus.IN_STOCK);
                    task.setPartAvailable(true);
                } else {
                    task.setPartStatus(StockStatus.OUT_OF_STOCK);
                    task.setPartAvailable(false);
                }
            }
        }
    }

    // Sets the status to ORDERED and schedules automatic delivery after 10 seconds.
    // onArrived is called after delivery – UI refresh and dialog are the controller's responsibility.
    public static void placeOrder(RepairTask task, IInventoryService inventoryService, Runnable onArrived) {
        task.setPartStatus(StockStatus.ORDERED);
        OrderDeliveryService.scheduleDelivery(() -> {
            List<InventoryItem> inventory = inventoryService.loadInventory();
            inventory.stream()
                    .filter(i -> i.getName().equals(task.getRequiredItemName()))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setQuantity(item.getQuantity() + 1);
                        inventoryService.saveInventory(inventory);
                    });
            task.setPartStatus(StockStatus.ARRIVED);
            task.setPartAvailable(true);
            onArrived.run();
        });
    }

    // Deducts the required part from inventory when a repair is marked as done.
    // onDeducted is called after the deduction – UI refresh is the controller's responsibility.
    public static void deductInventory(RepairTask task, IInventoryService inventoryService, Runnable onDeducted) {
        if (task.getRequiredItemName() == null) return;
        List<InventoryItem> inventory = inventoryService.loadInventory();
        inventory.stream()
                .filter(i -> i.getName().equals(task.getRequiredItemName()))
                .findFirst()
                .ifPresent(item -> {
                    if (item.getQuantity() > 0) {
                        item.setQuantity(item.getQuantity() - 1);
                        inventoryService.saveInventory(inventory);
                        onDeducted.run();
                    }
                });
    }
}
