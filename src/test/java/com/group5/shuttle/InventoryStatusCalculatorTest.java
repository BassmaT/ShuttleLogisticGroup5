package com.group5.shuttle;

import com.group5.shuttle.model.StockStatus;
import com.group5.shuttle.service.InventoryStatusCalculator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryStatusCalculatorTest {

    // ── OUT_OF_STOCK (Menge <= 0) ─────────────────────────────────────────────

    @Test
    void calculate_negativeQuantity_returnsOutOfStock() {
        assertEquals(StockStatus.OUT_OF_STOCK, InventoryStatusCalculator.calculate(-5));
    }

    @Test
    void calculate_zero_returnsOutOfStock() {
        assertEquals(StockStatus.OUT_OF_STOCK, InventoryStatusCalculator.calculate(0));
    }

    // ── LOW (Menge 1 oder 2) ──────────────────────────────────────────────────

    @Test
    void calculate_one_returnsLow() {
        assertEquals(StockStatus.LOW, InventoryStatusCalculator.calculate(1));
    }

    @Test
    void calculate_two_returnsLow() {
        assertEquals(StockStatus.LOW, InventoryStatusCalculator.calculate(2));
    }

    // ── IN_STOCK (Menge >= 3) ─────────────────────────────────────────────────

    @Test
    void calculate_three_returnsInStock() {
        assertEquals(StockStatus.IN_STOCK, InventoryStatusCalculator.calculate(3));
    }

    @Test
    void calculate_largeQuantity_returnsInStock() {
        assertEquals(StockStatus.IN_STOCK, InventoryStatusCalculator.calculate(100));
    }
}
