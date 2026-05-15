package com.group5.shuttle.controller;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.model.LogisticsOrder;
import com.group5.shuttle.model.OrderStatus;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.IEmployeeService;
import com.group5.shuttle.service.IInventoryService;
import com.group5.shuttle.service.InventoryService;
import com.group5.shuttle.service.OrderApprovalService;
import com.group5.shuttle.service.OrderStore;
import com.group5.shuttle.util.ColoredTableCell;
import com.group5.shuttle.util.Dialogs;
import com.group5.shuttle.util.EmployeeComboHelper;
import com.group5.shuttle.util.StatusColors;
import com.group5.shuttle.util.Styles;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.List;

// Controller for logistics and order management.
// Any employee can place an order request.
// A Security Chief must approve the order before it is considered "ordered".
// After approval, a 10-second pause simulates the delivery.
public class LogisticsController extends BaseController {

    // --- FXML bindings ---

    // Input fields for new orders
    @FXML private ComboBox<InventoryItem> cmbPartName;
    @FXML private TextField txtQuantity;
    @FXML private ComboBox<Employee> cmbOrderedBy;
    @FXML private TextField txtReason;
    @FXML private Button btnPlaceOrder;

    // Table: pending approvals
    @FXML private TableView<LogisticsOrder> pendingTable;
    @FXML private TableColumn<LogisticsOrder, String> colPendingNr;
    @FXML private TableColumn<LogisticsOrder, String> colPendingPart;
    @FXML private TableColumn<LogisticsOrder, Integer> colPendingQty;
    @FXML private TableColumn<LogisticsOrder, String> colPendingBy;
    @FXML private TableColumn<LogisticsOrder, String> colPendingReason;
    @FXML private TableColumn<LogisticsOrder, Void>   colPendingAction;

    // Table: all orders (complete overview)
    @FXML private TableView<LogisticsOrder> allOrdersTable;
    @FXML private TableColumn<LogisticsOrder, String> colAllNr;
    @FXML private TableColumn<LogisticsOrder, String> colAllPart;
    @FXML private TableColumn<LogisticsOrder, Integer> colAllQty;
    @FXML private TableColumn<LogisticsOrder, String> colAllBy;
    @FXML private TableColumn<LogisticsOrder, String> colAllStatus;
    @FXML private TableColumn<LogisticsOrder, String> colAllApprovedBy;
    @FXML private TableColumn<LogisticsOrder, String> colAllDate;

    // Back button
    @FXML private Button btnBack;

    // Container for the stock inventory display
    @FXML private VBox stockContainer;

    // Observable lists for reactive table display
    private final ObservableList<LogisticsOrder> pendingData    = FXCollections.observableArrayList();
    private final ObservableList<LogisticsOrder> allOrdersData  = FXCollections.observableArrayList();

    private final IInventoryService inventoryService = InventoryService.getInstance();
    private final IEmployeeService  employeeService  = EmployeeService.getInstance();

    // Called automatically when the FXML is loaded
    @FXML
    public void initialize() {
        // Populate the Part ComboBox with all inventory items; display: "Name  (Qty: X)"
        List<InventoryItem> inventory = inventoryService.loadInventory();
        cmbPartName.setItems(FXCollections.observableArrayList(inventory));
        cmbPartName.setConverter(new javafx.util.StringConverter<InventoryItem>() {
            @Override public String toString(InventoryItem item) {
                return item == null ? "" : item.getName() + "  (Qty: " + item.getQuantity() + ")";
            }
            @Override public InventoryItem fromString(String s) {
                if (s == null) return null;
                return inventory.stream().filter(i -> toString(i).equals(s)).findFirst().orElse(null);
            }
        });

        // Populate the ComboBox with all employees
        EmployeeComboHelper.setup(cmbOrderedBy);

        // Configure table columns
        setupPendingTable();
        setupAllOrdersTable();

        // Load existing orders from the OrderStore
        refreshTables();

        // Order button
        btnPlaceOrder.setOnAction(e -> placeOrder());

        // Back button
        setupBackButton(btnBack);
    }

    // Creates a new order request and adds it to the OrderStore
    private void placeOrder() {
        // Validate input: all fields must be filled in
        InventoryItem selectedPart = cmbPartName.getValue();
        String partName = selectedPart != null ? selectedPart.getName() : "";
        String qtyText  = txtQuantity.getText().trim();
        Employee orderedBy = cmbOrderedBy.getValue();
        String reason   = txtReason.getText().trim();

        if (partName.isEmpty() || qtyText.isEmpty() || orderedBy == null || reason.isEmpty()) {
            Dialogs.showWarning("Please fill in all fields.");
            return;
        }

        // Parse quantity as a number
        int quantity;
        try {
            quantity = Integer.parseInt(qtyText);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            Dialogs.showWarning("Quantity must be a positive integer.");
            return;
        }

        // Create order in the OrderStore (status: PENDING_APPROVAL)
        OrderStore.getInstance().createOrder(partName, quantity, orderedBy, reason);

        // Refresh tables and input fields
        refreshTables();
        cmbPartName.getSelectionModel().clearSelection();
        txtQuantity.clear();
        cmbOrderedBy.getSelectionModel().clearSelection();
        txtReason.clear();
    }

    // Configures the "Pending Approval" table
    private void setupPendingTable() {
        colPendingNr.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colPendingPart.setCellValueFactory(new PropertyValueFactory<>("partName"));
        colPendingQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPendingBy.setCellValueFactory(new PropertyValueFactory<>("orderedByName"));
        colPendingReason.setCellValueFactory(new PropertyValueFactory<>("reason"));

        // Action column: two buttons (Approve / Reject) per row
        colPendingAction.setCellFactory(col -> new TableCell<>() {
            // "Approve" button: green, intended for Security Chiefs only
            private final Button btnApprove = new Button("Approve");
            // "Reject" button: red
            private final Button btnReject  = new Button("Reject");
            private final HBox box = new HBox(6, btnApprove, btnReject);

            {
                // Style buttons
                btnApprove.setStyle(
                    "-fx-background-color: #66ff66; -fx-text-fill: #1e1e1e; -fx-font-size: 11px;");
                btnReject.setStyle(
                    "-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-size: 11px;");

                btnApprove.setOnAction(e -> {
                    LogisticsOrder order = getTableView().getItems().get(getIndex());
                    OrderApprovalService.approve(order, inventoryService, employeeService, () -> {
                        refreshTables();
                        cmbPartName.setItems(FXCollections.observableArrayList(
                            inventoryService.loadInventory()));
                        Dialogs.showInfo("Delivery",
                            "✓ Order " + order.getOrderNumber() + " has arrived!\n"
                            + "Part: " + order.getPartName()
                            + " (Quantity: " + order.getQuantity() + ")");
                    });
                    refreshTables();
                });

                btnReject.setOnAction(e -> {
                    LogisticsOrder order = getTableView().getItems().get(getIndex());
                    OrderApprovalService.reject(order, employeeService);
                    refreshTables();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // Only show buttons when the row is not empty
                setGraphic(empty ? null : box);
            }
        });

        pendingTable.setItems(pendingData);
        pendingTable.setStyle(Styles.TABLE_DARK);
    }

    // Configures the complete overview of all orders
    private void setupAllOrdersTable() {
        colAllNr.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colAllPart.setCellValueFactory(new PropertyValueFactory<>("partName"));
        colAllQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAllBy.setCellValueFactory(new PropertyValueFactory<>("orderedByName"));
        colAllApprovedBy.setCellValueFactory(new PropertyValueFactory<>("approvedByName"));
        colAllDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        // Status column: colour-coded (green = delivered, blue = ordered, yellow = approved,
        //                               red = rejected, grey = pending)
        colAllStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAllStatus.setCellFactory(col -> new ColoredTableCell<>(StatusColors::forOrderStatus));

        allOrdersTable.setItems(allOrdersData);
        allOrdersTable.setStyle(Styles.TABLE_DARK);
    }

    // Refreshes both tables from the OrderStore
    private void refreshTables() {
        List<LogisticsOrder> all = OrderStore.getInstance().getOrders();

        // "Pending" table: only orders with status PENDING_APPROVAL
        pendingData.setAll(
            all.stream()
               .filter(o -> o.getOrderStatus() == OrderStatus.PENDING_APPROVAL)
               .toList());

        // Complete overview: all orders
        allOrdersData.setAll(all);

        // Refresh stock display
        refreshStockDisplay();
    }

    // Populates stockContainer with colour-coded labels for each inventory item
    private void refreshStockDisplay() {
        if (stockContainer == null) return;
        stockContainer.getChildren().clear();
        List<InventoryItem> items = inventoryService.loadInventory();
        for (InventoryItem item : items) {
            String color = StatusColors.forStockStatus(item.getStatus());
            Label lbl = new Label(String.format("  %s  (Qty: %d)  [%s]",
                    item.getName(), item.getQuantity(), item.getStatus()));
            lbl.setStyle(Styles.label12(color));
            stockContainer.getChildren().add(lbl);
        }
    }

    // Returns the back button for BaseController
    @Override
    protected Button getNavigationButton() { return btnBack; }
}
