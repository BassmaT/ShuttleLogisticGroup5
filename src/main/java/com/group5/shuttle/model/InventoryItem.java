package com.group5.shuttle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// @JsonIgnoreProperties(ignoreUnknown = true) bedeutet:
// Falls die JSON-Datei Felder hat, die hier nicht als Variablen definiert sind,
// werden diese einfach ignoriert – kein Fehler wird geworfen.
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryItem {

    // Eindeutige ID des Lagerartikels, z. B. "INV-001".
    public String id;

    // Name des Bauteils, z. B. "Heat Shield Panel".
    public String name;

    // Zu welchem Shuttle-Teil gehört dieses Bauteil, z. B. "Orbiter".
    public String part;

    // Wie viele Stück davon auf Lager sind.
    public int quantity;

    // Lagerstatus: "IN_STOCK", "LOW" oder "OUT_OF_STOCK".
    public String status;

    // Kurze Beschreibung: was das Bauteil ist und wofür es gebraucht wird
    public String description;

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
