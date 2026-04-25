package com.group5.shuttle.service;

import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.StockStatus;

import java.util.List;

// Einzelverantwortung: Verwaltet die Lagerlogik für Reparaturaufgaben.
// Prüft Verfügbarkeit, bucht Teile ein/aus und plant Lieferungen.
// Extrahiert aus MissionControlController (war dort Geschäftslogik in einem UI-Controller).
public final class RepairInventoryService {

    private RepairInventoryService() {}

    // Setzt partStatus und partAvailable für jede Aufgabe anhand des aktuellen Lagerbestands.
    // Aufgaben mit laufender Bestellung (ORDERED/ARRIVED) werden übersprungen.
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

    // Setzt den Status auf ORDERED und plant die automatische Lieferung nach 10 Sekunden.
    // onArrived wird nach der Lieferung aufgerufen – UI-Refresh und Dialog obliegen dem Controller.
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

    // Bucht das benötigte Teil aus dem Lager ab, wenn eine Reparatur als erledigt markiert wird.
    // onDeducted wird nach der Abbuchung aufgerufen – UI-Refresh obliegt dem Controller.
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
