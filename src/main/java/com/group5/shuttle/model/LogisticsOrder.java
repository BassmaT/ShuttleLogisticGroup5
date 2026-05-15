package com.group5.shuttle.model;

// JavaFX property for reactive table display of the order status
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Represents an order request in the logistics system.
// An order passes through the following statuses: PENDING_APPROVAL → APPROVED/REJECTED → ORDERED → DELIVERED
public class LogisticsOrder {

    // --- Fields ---

    // Unique order number, e.g. "ORD-001"
    private String orderNumber;

    // Name of the ordered spare part
    private String partName;

    // Ordered quantity
    private int quantity;

    // ID of the employee who placed the order
    private String orderedById;

    // Display name of the ordering party (denormalized for the table)
    private String orderedByName;

    // Reason for the order, e.g. "Replacement required after sensor anomaly"
    private String reason;

    // Order timestamp (ISO format)
    private String orderDate;

    // Current status of the order
    private OrderStatus status;

    // ID of the approving Security Chief (null until approved)
    private String approvedById;

    // Display name of the approver
    private String approvedByName;

    // Approval timestamp (null until approved)
    private String approvalDate;

    // Delivery timestamp (null until delivered)
    private String deliveryDate;

    // JavaFX property for reactive status display in the TableView
    private final SimpleStringProperty statusProperty;

    // Formatter for a human-readable timestamp
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Constructor – called by OrderStore.createOrder()
    public LogisticsOrder(String orderNumber, String partName, int quantity,
                          Employee orderedBy, String reason) {
        this.orderNumber   = orderNumber;
        this.partName      = partName;
        this.quantity      = quantity;
        this.orderedById   = orderedBy.getId();
        this.orderedByName = orderedBy.getName();
        this.reason        = reason;
        this.orderDate     = LocalDateTime.now().format(FMT);
        this.status        = OrderStatus.PENDING_APPROVAL;
        // JavaFX property is initialized with the initial status
        this.statusProperty = new SimpleStringProperty(OrderStatus.PENDING_APPROVAL.name());
    }

    // --- Status change methods ---

    // Sets the status and simultaneously updates the JavaFX property
    public void setStatus(OrderStatus newStatus) {
        this.status = newStatus;
        statusProperty.set(newStatus.name());
    }

    public void setApprovedById(String id)       { this.approvedById   = id; }
    public void setApprovedByName(String name)   { this.approvedByName = name; }
    public void setApprovalDate(String date)     { this.approvalDate   = date; }
    public void setDeliveryDate(String date)     { this.deliveryDate   = date; }

    // --- Getter methods (for PropertyValueFactory and direct use) ---

    public String getOrderNumber()     { return orderNumber; }
    public String getPartName()        { return partName; }
    public int getQuantity()           { return quantity; }
    public String getOrderedById()     { return orderedById; }
    public String getOrderedByName()   { return orderedByName; }
    public String getReason()          { return reason; }
    public String getOrderDate()       { return orderDate; }
    public String getStatus()          { return status.name(); }
    public OrderStatus getOrderStatus(){ return status; }
    public String getApprovedById()    { return approvedById; }
    public String getApprovedByName()  { return approvedByName; }
    public String getApprovalDate()    { return approvalDate; }
    public String getDeliveryDate()    { return deliveryDate; }

    // Returns the JavaFX property – required for reactive table updates
    public SimpleStringProperty statusProperty() { return statusProperty; }
}
