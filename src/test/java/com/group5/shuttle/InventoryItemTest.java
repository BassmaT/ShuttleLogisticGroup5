package com.group5.shuttle;

import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.model.StockStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryItemTest {

    private InventoryItem item(int quantity) {
        return new InventoryItem("INV-TEST", "Test Part", "Orbiter",
                quantity, StockStatus.IN_STOCK, "Testbeschreibung");
    }

    // ── setQuantity() aktualisiert den Status automatisch ────────────────────

    @Test
    void setQuantity_toZero_statusBecomesOutOfStock() {
        InventoryItem i = item(5);
        i.setQuantity(0);
        assertEquals(StockStatus.OUT_OF_STOCK, i.getStatus());
    }

    @Test
    void setQuantity_toOne_statusBecomesLow() {
        InventoryItem i = item(5);
        i.setQuantity(1);
        assertEquals(StockStatus.LOW, i.getStatus());
    }

    @Test
    void setQuantity_toThree_statusBecomesInStock() {
        InventoryItem i = item(0);
        i.setQuantity(3);
        assertEquals(StockStatus.IN_STOCK, i.getStatus());
    }

    // ── Menge wird korrekt gespeichert ───────────────────────────────────────

    @Test
    void setQuantity_storesNewValue() {
        InventoryItem i = item(2);
        i.setQuantity(7);
        assertEquals(7, i.getQuantity());
    }

    // ── Konstruktor übernimmt die übergebenen Werte direkt ───────────────────

    @Test
    void constructor_setsFieldsCorrectly() {
        InventoryItem i = new InventoryItem("INV-001", "Heat Shield Panel",
                "Orbiter", 4, StockStatus.IN_STOCK, "Beschreibung");

        assertEquals("INV-001",           i.getId());
        assertEquals("Heat Shield Panel", i.getName());
        assertEquals("Orbiter",           i.getPart());
        assertEquals(4,                   i.getQuantity());
        assertEquals(StockStatus.IN_STOCK, i.getStatus());
    }
}
