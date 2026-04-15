package com.group5.shuttle.service;

import com.group5.shuttle.model.InventoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Verantwortlich für: Lagerhaltung (Inventory laden und speichern).
 * Singleton – inventoryCache bleibt für die gesamte Sitzung erhalten.
 */
public class InventoryService {

    private static InventoryService instance;

    private List<InventoryItem> inventoryCache = null;

    private InventoryService() {}

    public static InventoryService getInstance() {
        if (instance == null) instance = new InventoryService();
        return instance;
    }

    public List<InventoryItem> loadInventory() {
        if (inventoryCache == null) {
            inventoryCache = createDefaultInventory();
        }
        return inventoryCache;
    }

    public void saveInventory(List<InventoryItem> items) {
        inventoryCache = items;
    }

    private List<InventoryItem> createDefaultInventory() {
        List<InventoryItem> list = new ArrayList<>();

        list.add(item("INV-001", "Heat Shield Panel",      "Orbiter",       0, "OUT_OF_STOCK",
                "Thermal protection tile that shields the orbiter hull from extreme heat during atmospheric re-entry."));
        list.add(item("INV-002", "O2 Sensor Module",        "Orbiter",       2, "LOW",
                "Monitors cabin oxygen concentration. Replaced when oxygenLevel sensor falls below safe thresholds."));
        list.add(item("INV-003", "Coolant Pressure Valve",  "Orbiter",       3, "IN_STOCK",
                "Regulates coolant flow through the orbiter's thermal control system."));
        list.add(item("INV-004", "SRB Nozzle Assembly",     "SRB",           3, "IN_STOCK",
                "Directs exhaust from the solid rocket booster to generate thrust."));
        list.add(item("INV-005", "Vibration Damper",         "SRB",           1, "LOW",
                "Absorbs mechanical vibrations from the solid rocket booster during ignition and ascent."));
        list.add(item("INV-006", "Fuel Tank Liner",          "External Tank", 4, "IN_STOCK",
                "Insulating liner inside the external tank that prevents cryogenic fuel from warming."));
        list.add(item("INV-007", "Cryogenic Seal",           "External Tank", 0, "OUT_OF_STOCK",
                "High-performance seal for cryogenic fuel connections."));

        return list;
    }

    private InventoryItem item(String id, String name, String part,
                               int quantity, String status, String description) {
        InventoryItem it = new InventoryItem();
        it.id          = id;
        it.name        = name;
        it.part        = part;
        it.quantity    = quantity;
        it.status      = status;
        it.description = description;
        return it;
    }
}
