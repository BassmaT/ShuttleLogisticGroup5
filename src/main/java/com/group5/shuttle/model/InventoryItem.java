package com.group5.shuttle.model;

import com.group5.shuttle.service.InventoryStatusCalculator;

public class InventoryItem {

    // Eindeutige ID des Lagerartikels, z. B. "INV-001".
    private String id;

    // Name des Bauteils, z. B. "Heat Shield Panel".
    private String name;

    // Zu welchem Shuttle-Teil gehört dieses Bauteil, z. B. "Orbiter".
    private String part;

    // Wie viele Stück davon auf Lager sind.
    private int quantity;

    // Lagerstatus: "IN_STOCK", "LOW" oder "OUT_OF_STOCK".
    //private String status;

    // Kurze Beschreibung: was das Bauteil ist und wofür es gebraucht wird
    private String description;
    private StockStatus status;

    // Konstruktor – initialisiert einen Lagerartikel vollständig.
    // status wird über setQuantity() automatisch berechnet,
    // kann aber durch den expliziten status-Parameter überschrieben werden.
    public InventoryItem(String id, String name, String part,
                         int quantity, StockStatus status, String description) {
        this.id          = id;
        this.name        = name;
        this.part        = part;
        this.description = description;
        this.quantity    = quantity;
        this.status      = status;
    }

    // Getter-Methoden – werden von PropertyValueFactory für die Tabelle benötigt.
    public String getId()          { return id; }
    public String getName()        { return name; }
    public String getPart()        { return part; }
    public int    getQuantity()    { return quantity; }
    public StockStatus getStatus()      { return status; }
    public String getDescription() { return description; }

    // Setzt die neue Menge und berechnet dabei automatisch den Status neu.
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.status = InventoryStatusCalculator.calculate(quantity);
    }

    // Ermöglicht das direkte Setzen des Status-Strings von außen.
    public void setStatus(StockStatus status) { this.status = status; }
}
