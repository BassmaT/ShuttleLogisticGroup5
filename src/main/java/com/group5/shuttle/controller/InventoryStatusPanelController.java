package com.group5.shuttle.controller;

import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.service.IInventoryService;
import com.group5.shuttle.util.StatusColors;
import com.group5.shuttle.util.Styles;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

class InventoryStatusPanelController {

    private final VBox container;
    private final Label header;
    private final IInventoryService inventoryService;

    InventoryStatusPanelController(VBox container, Label header, IInventoryService inventoryService) {
        this.container = container;
        this.header = header;
        this.inventoryService = inventoryService;
    }

    void update(String displayName) {
        container.getChildren().clear();
        header.setText("Required Parts – " + displayName);

        List<InventoryItem> items = inventoryService.loadInventory();
        boolean found = false;
        for (InventoryItem item : items) {
            if (item.getPart().equalsIgnoreCase(displayName)) {
                found = true;
                String color = StatusColors.forStockStatus(item.getStatus());
                Label lbl = new Label(String.format("  %s  (Qty: %d)  [%s]",
                        item.getName(), item.getQuantity(), item.getStatus()));
                lbl.setStyle(Styles.label13(color));
                container.getChildren().add(lbl);
            }
        }
        if (!found) {
            Label lbl = new Label("  No parts tracked for this section");
            lbl.setStyle(Styles.label13("#888888"));
            container.getChildren().add(lbl);
        }
    }
}
