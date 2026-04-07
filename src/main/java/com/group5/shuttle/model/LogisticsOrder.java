package com.group5.shuttle.model;

// JavaFX-Property für die reaktive Tabellenanzeige des Bestellstatus
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Repräsentiert eine Bestellanfrage im Logistiksystem
// Eine Bestellung durchläuft folgende Status: PENDING_APPROVAL → APPROVED/REJECTED → ORDERED → DELIVERED
public class LogisticsOrder {

    // --- Status-Konstanten ---

    // Warte auf Genehmigung durch Security Chief
    public static final String PENDING_APPROVAL = "PENDING_APPROVAL";

    // Vom Security Chief genehmigt
    public static final String APPROVED = "APPROVED";

    // Vom Security Chief abgelehnt
    public static final String REJECTED = "REJECTED";

    // Bestellt beim Lieferanten (nach Genehmigung)
    public static final String ORDERED = "ORDERED";

    // Im Lager eingetroffen
    public static final String DELIVERED = "DELIVERED";

    // --- Felder ---

    // Eindeutige Bestellnummer, z. B. "ORD-001"
    private String orderNumber;

    // Name des bestellten Ersatzteils
    private String partName;

    // Bestellte Menge
    private int quantity;

    // ID des Mitarbeiters, der bestellt hat
    private String orderedById;

    // Anzeigename des Bestellers (denormalisiert für die Tabelle)
    private String orderedByName;

    // Begründung der Bestellung, z. B. "Replacement required after sensor anomaly"
    private String reason;

    // Zeitstempel der Bestellung (ISO-Format)
    private String orderDate;

    // Aktueller Status der Bestellung
    private String status;

    // ID des genehmigenden Security Chiefs (null bis genehmigt)
    private String approvedById;

    // Anzeigename des Genehmigers
    private String approvedByName;

    // Zeitstempel der Genehmigung (null bis genehmigt)
    private String approvalDate;

    // Zeitstempel der Lieferung (null bis geliefert)
    private String deliveryDate;

    // JavaFX-Property für die reaktive Statusanzeige in der TableView
    private final SimpleStringProperty statusProperty;

    // Formatierer für lesbaren Zeitstempel
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Konstruktor – wird von OrderStore.createOrder() aufgerufen
    public LogisticsOrder(String orderNumber, String partName, int quantity,
                          Employee orderedBy, String reason) {
        this.orderNumber   = orderNumber;
        this.partName      = partName;
        this.quantity      = quantity;
        this.orderedById   = orderedBy.getId();
        this.orderedByName = orderedBy.getName();
        this.reason        = reason;
        this.orderDate     = LocalDateTime.now().format(FMT);
        this.status        = PENDING_APPROVAL;
        // JavaFX-Property wird mit dem Anfangsstatus initialisiert
        this.statusProperty = new SimpleStringProperty(PENDING_APPROVAL);
    }

    // --- Methoden zur Statusänderung ---

    // Genehmigt die Bestellung durch einen Security Chief
    public void approve(Employee chief) {
        this.approvedById   = chief.getId();
        this.approvedByName = chief.getName();
        this.approvalDate   = LocalDateTime.now().format(FMT);
        setStatus(APPROVED);
    }

    // Lehnt die Bestellung ab
    public void reject(Employee chief) {
        this.approvedById   = chief.getId();
        this.approvedByName = chief.getName();
        this.approvalDate   = LocalDateTime.now().format(FMT);
        setStatus(REJECTED);
    }

    // Markiert die Bestellung als "bestellt beim Lieferanten"
    public void markOrdered() {
        setStatus(ORDERED);
    }

    // Markiert die Bestellung als "im Lager eingetroffen"
    public void markDelivered() {
        this.deliveryDate = LocalDateTime.now().format(FMT);
        setStatus(DELIVERED);
    }

    // Setzt den Status und aktualisiert gleichzeitig die JavaFX-Property
    private void setStatus(String newStatus) {
        this.status = newStatus;
        statusProperty.set(newStatus);
    }

    // --- Getter-Methoden (für PropertyValueFactory und direkte Verwendung) ---

    public String getOrderNumber()   { return orderNumber; }
    public String getPartName()      { return partName; }
    public int getQuantity()         { return quantity; }
    public String getOrderedById()   { return orderedById; }
    public String getOrderedByName() { return orderedByName; }
    public String getReason()        { return reason; }
    public String getOrderDate()     { return orderDate; }
    public String getStatus()        { return status; }
    public String getApprovedById()  { return approvedById; }
    public String getApprovedByName(){ return approvedByName; }
    public String getApprovalDate()  { return approvalDate; }
    public String getDeliveryDate()  { return deliveryDate; }

    // Gibt die JavaFX-Property zurück – wird für reaktive Tabellenaktualisierung benötigt
    public SimpleStringProperty statusProperty() { return statusProperty; }
}
