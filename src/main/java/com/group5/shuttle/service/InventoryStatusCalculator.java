package com.group5.shuttle.service;

import com.group5.shuttle.model.StockStatus;

// Einzel-Verantwortung: berechnet den Lagerstatus aus einer Menge.
// Extrahiert aus InventoryItem (war dort Geschäftslogik im Datenmodell).
public final class InventoryStatusCalculator {

    private static final int STOCK_EMPTY_MAX = 0;
    private static final int STOCK_LOW_MAX   = 2;

    private InventoryStatusCalculator() {}

    public static StockStatus calculate(int quantity) {
        if (quantity <= STOCK_EMPTY_MAX) return StockStatus.OUT_OF_STOCK;
        if (quantity <= STOCK_LOW_MAX)   return StockStatus.LOW;
        return StockStatus.IN_STOCK;
    }
}
