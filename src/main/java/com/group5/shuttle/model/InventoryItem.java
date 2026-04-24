package com.group5.shuttle.model;

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
    private String status;

    // Kurze Beschreibung: was das Bauteil ist und wofür es gebraucht wird
    private String description;

    // Konstruktor – initialisiert einen Lagerartikel vollständig.
    // status wird über setQuantity() automatisch berechnet,
    // kann aber durch den expliziten status-Parameter überschrieben werden.
    public InventoryItem(String id, String name, String part,
                         int quantity, String status, String description) {
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
    public String getStatus()      { return status; }
    public String getDescription() { return description; }

    // Setzt die neue Menge und berechnet dabei automatisch den Status neu.
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        if (quantity <= 0)      this.status = "OUT_OF_STOCK"; // nichts mehr auf Lager
        else if (quantity <= 2) this.status = "LOW";          // fast leer
        else                    this.status = "IN_STOCK";     // ausreichend vorhanden
    }

    // Ermöglicht das direkte Setzen des Status-Strings von außen.
    public void setStatus(String status) { this.status = status; }
}
