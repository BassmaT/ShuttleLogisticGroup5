package com.group5.shuttle;

import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.SensorStatus;
import com.group5.shuttle.model.StockStatus;
import com.group5.shuttle.service.IInventoryService;
import com.group5.shuttle.service.RepairInventoryService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class RepairInventoryServiceTest {

    // Einfacher Stub – ersetzt echten InventoryService ohne Mockito
    private static IInventoryService stubWith(List<InventoryItem> items) {
        return new IInventoryService() {
            private List<InventoryItem> inv = items;
            @Override public List<InventoryItem> loadInventory()           { return inv; }
            @Override public void saveInventory(List<InventoryItem> saved) { inv = saved; }
        };
    }

    private static InventoryItem part(String name, int quantity) {
        return new InventoryItem("ID", name, "Orbiter", quantity,
                StockStatus.IN_STOCK, "Beschreibung");
    }

    private static RepairTask task(String requiredPart) {
        RepairTask t = new RepairTask("orbiter", "Sensor", SensorStatus.OK, "Check");
        t.setRequiredItemName(requiredPart);
        return t;
    }

    // ── deductInventory ───────────────────────────────────────────────────────

    @Test
    void deductInventory_itemInStock_decrementsQuantityByOne() {
        InventoryItem heatShield = part("Heat Shield Panel", 3);
        IInventoryService svc = stubWith(new ArrayList<>(List.of(heatShield)));
        RepairTask t = task("Heat Shield Panel");

        RepairInventoryService.deductInventory(t, svc, () -> {});

        assertEquals(2, heatShield.getQuantity());
    }

    @Test
    void deductInventory_itemInStock_callsOnDeductedCallback() {
        IInventoryService svc = stubWith(new ArrayList<>(List.of(part("O2 Sensor Module", 2))));
        RepairTask t = task("O2 Sensor Module");
        AtomicBoolean called = new AtomicBoolean(false);

        RepairInventoryService.deductInventory(t, svc, () -> called.set(true));

        assertTrue(called.get());
    }

    @Test
    void deductInventory_itemOutOfStock_doesNotCallCallback() {
        // Menge ist 0 → darf nicht abgebucht werden, Callback soll nicht feuern
        IInventoryService svc = stubWith(new ArrayList<>(List.of(part("Cryogenic Seal", 0))));
        RepairTask t = task("Cryogenic Seal");
        AtomicBoolean called = new AtomicBoolean(false);

        RepairInventoryService.deductInventory(t, svc, () -> called.set(true));

        assertFalse(called.get());
    }

    @Test
    void deductInventory_nullRequiredItem_doesNothing() {
        // Aufgaben ohne RequiredItemName dürfen keinen Fehler werfen
        IInventoryService svc = stubWith(new ArrayList<>());
        RepairTask t = task(null);
        AtomicBoolean called = new AtomicBoolean(false);

        assertDoesNotThrow(() ->
                RepairInventoryService.deductInventory(t, svc, () -> called.set(true)));
        assertFalse(called.get());
    }

    // ── initTaskStatuses ──────────────────────────────────────────────────────

    @Test
    void initTaskStatuses_itemAvailable_setsInStockAndPartAvailable() {
        IInventoryService svc = stubWith(new ArrayList<>(List.of(part("SRB Nozzle Assembly", 3))));
        RepairTask t = task("SRB Nozzle Assembly");

        RepairInventoryService.initTaskStatuses(List.of(t), svc);

        assertEquals(StockStatus.IN_STOCK.name(), t.getPartStatus());
        assertTrue(t.isPartAvailable());
    }

    @Test
    void initTaskStatuses_itemNotFound_setsOutOfStockAndNotAvailable() {
        IInventoryService svc = stubWith(new ArrayList<>());
        RepairTask t = task("Unbekanntes Teil");

        RepairInventoryService.initTaskStatuses(List.of(t), svc);

        assertEquals(StockStatus.OUT_OF_STOCK.name(), t.getPartStatus());
        assertFalse(t.isPartAvailable());
    }

    @Test
    void initTaskStatuses_nullRequiredItem_setsNoneAndAvailable() {
        // Aufgaben ohne benötigtes Teil gelten immer als erfüllbar
        IInventoryService svc = stubWith(new ArrayList<>());
        RepairTask t = task(null);

        RepairInventoryService.initTaskStatuses(List.of(t), svc);

        assertEquals(StockStatus.NONE.name(), t.getPartStatus());
        assertTrue(t.isPartAvailable());
    }

    @Test
    void initTaskStatuses_orderedStatus_skipsUpdate() {
        // Aufgaben mit laufender Bestellung (ORDERED) sollen nicht überschrieben werden
        IInventoryService svc = stubWith(new ArrayList<>(List.of(part("Vibration Damper", 0))));
        RepairTask t = task("Vibration Damper");
        t.setPartStatus(StockStatus.ORDERED);

        RepairInventoryService.initTaskStatuses(List.of(t), svc);

        assertEquals(StockStatus.ORDERED.name(), t.getPartStatus()); // unverändert
    }
}
