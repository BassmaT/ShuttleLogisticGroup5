package com.group5.shuttle.controller;

import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.service.InventoryService;
import com.group5.shuttle.util.ColoredTableCell;
import com.group5.shuttle.util.StatusColors;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

// Controller for the inventory overview.
// Displays all inventory items in a table: ID, part name, shuttle part, quantity and status.
// The status is color-highlighted (green = in stock, yellow = almost empty, red = out of stock).
public class InventoryController extends BaseController {

    // Back button to the main dashboard.
    @FXML private Button btnBack;

    // The table that displays all inventory items.
    @FXML private TableView<InventoryItem> inventoryTable;

    // Column for the item ID, e.g. "INV-001".
    @FXML private TableColumn<InventoryItem, String>  colId;

    // Column for the part name, e.g. "Heat Shield Panel".
    @FXML private TableColumn<InventoryItem, String>  colName;

    // Column for the associated shuttle part, e.g. "Orbiter".
    @FXML private TableColumn<InventoryItem, String>  colPart;

    // Column for the available quantity, e.g. 5.
    @FXML private TableColumn<InventoryItem, Integer> colQuantity;

    // Column for the stock status: "IN_STOCK", "LOW" or "OUT_OF_STOCK".
    @FXML private TableColumn<InventoryItem, String>  colStatus;

    // Column for the short description of the part.
    @FXML private TableColumn<InventoryItem, String>  colDescription;

    private final InventoryService inventoryService = InventoryService.getInstance();

    // Called automatically as soon as the FXML file is fully loaded.
    @FXML
    public void initialize() {
        // Back button: navigates to the main dashboard.
        setupBackButton(btnBack);

        // Build the table and fill it with data.
        setupTable();
        loadInventory();
    }

    // Binds each column to the corresponding field in InventoryItem
    // and defines the colored rendering of the status column.
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPart.setCellValueFactory(new PropertyValueFactory<>("part"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colStatus.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getStatus().getLabel()));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Custom rendering: the status cell gets a different color depending on its value.
        colStatus.setCellFactory(col -> new ColoredTableCell<>(StatusColors::forStockStatus));
    }

    // Loads all inventory items from the file and passes them to the table.
    private void loadInventory() {
        List<InventoryItem> items = inventoryService.loadInventory();
        // FXCollections.observableArrayList converts the regular list into a JavaFX observable list
        // so that the table can react to changes.
        inventoryTable.setItems(FXCollections.observableArrayList(items));
    }

    // Returns the back button – required by BaseController.loadView().
    @Override
    protected Button getNavigationButton() {
        return btnBack;
    }
}
