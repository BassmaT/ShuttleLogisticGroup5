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

// Controller für die Lagerübersicht.
// Zeigt alle Lagerpositionen in einer Tabelle an: ID, Bauteilname, Shuttle-Teil, Menge und Status.
// Der Status wird farbig hervorgehoben (grün = vorhanden, gelb = fast leer, rot = nicht vorrätig).
public class InventoryController extends BaseController {

    // Zurück-Button zum Haupt-Dashboard.
    @FXML private Button btnBack;

    // Die Tabelle, die alle Lagerpositionen anzeigt.
    @FXML private TableView<InventoryItem> inventoryTable;

    // Spalte für die Artikel-ID, z. B. "INV-001".
    @FXML private TableColumn<InventoryItem, String>  colId;

    // Spalte für den Bauteilnamen, z. B. "Heat Shield Panel".
    @FXML private TableColumn<InventoryItem, String>  colName;

    // Spalte für das zugehörige Shuttle-Teil, z. B. "Orbiter".
    @FXML private TableColumn<InventoryItem, String>  colPart;

    // Spalte für die verfügbare Menge, z. B. 5.
    @FXML private TableColumn<InventoryItem, Integer> colQuantity;

    // Spalte für den Lagerstatus: "IN_STOCK", "LOW" oder "OUT_OF_STOCK".
    @FXML private TableColumn<InventoryItem, String>  colStatus;

    // Spalte für die Kurzbeschreibung des Bauteils.
    @FXML private TableColumn<InventoryItem, String>  colDescription;

    private final InventoryService inventoryService = InventoryService.getInstance();

    // Wird automatisch aufgerufen, sobald die FXML-Datei vollständig geladen ist.
    @FXML
    public void initialize() {
        // Zurück-Button: Navigiert zum Haupt-Dashboard.
        setupBackButton(btnBack);

        // Tabelle aufbauen und mit Daten füllen.
        setupTable();
        loadInventory();
    }

    // Bindet jede Spalte an das entsprechende Feld in InventoryItem
    // und definiert die farbige Darstellung der Status-Spalte.
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPart.setCellValueFactory(new PropertyValueFactory<>("part"));
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Benutzerdefinierte Darstellung: Status-Zelle bekommt je nach Wert eine andere Farbe.
        colStatus.setCellFactory(col -> new ColoredTableCell<>(StatusColors::forStockStatus));
    }

    // Lädt alle Lagerpositionen aus der Datei und gibt sie an die Tabelle weiter.
    private void loadInventory() {
        List<InventoryItem> items = inventoryService.loadInventory();
        // FXCollections.observableArrayList wandelt die normale Liste in eine JavaFX-Observable-Liste um,
        // damit die Tabelle auf Änderungen reagieren kann.
        inventoryTable.setItems(FXCollections.observableArrayList(items));
    }

    // Gibt den Zurück-Button zurück – wird von BaseController.loadView() benötigt.
    @Override
    protected Button getNavigationButton() {
        return btnBack;
    }
}
