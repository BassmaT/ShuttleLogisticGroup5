package com.group5.shuttle.model;

import com.group5.shuttle.service.InventoryStatusCalculator;

public class InventoryItem {

    // Unique ID of the inventory item, e.g. "INV-001".
    private String id;

    // Name of the component, e.g. "Heat Shield Panel".
    private String name;

    // Which shuttle part does this component belong to, e.g. "Orbiter".
    private String part;

    // How many units are currently in stock.
    private int quantity;

    // Stock status: "IN_STOCK", "LOW" or "OUT_OF_STOCK".
    //private String status;

    // Short description: what the component is and what it is used for
    private String description;
    private StockStatus status;

    // Constructor – fully initializes an inventory item.
    // The status is calculated automatically via setQuantity(),
    // but can be overridden by the explicit status parameter.
    public InventoryItem(String id, String name, String part,
                         int quantity, StockStatus status, String description) {
        this.id          = id;
        this.name        = name;
        this.part        = part;
        this.description = description;
        this.quantity    = quantity;
        this.status      = status;
    }

    // Getter methods – required by PropertyValueFactory for the table.
    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getPart()        { return part; }
    public int    getQuantity()    { return quantity; }
    public StockStatus getStatus()      { return status; }
    public String getDescription() { return description; }

    // Sets the new quantity and automatically recalculates the status.
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.status = InventoryStatusCalculator.calculate(quantity);
    }

    // Allows directly setting the status from outside.
    public void setStatus(StockStatus status) { this.status = status; }
}
