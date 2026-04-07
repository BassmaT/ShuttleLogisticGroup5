package com.group5.shuttle.controller;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.model.LogisticsOrder;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.OrderStore;
import com.group5.shuttle.service.SensorService;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
import javafx.util.Duration;
import java.util.List;

// Controller für die Logistik- und Bestellverwaltung.
// Jeder Mitarbeiter kann eine Bestellanfrage aufgeben.
// Ein Security Chief muss die Bestellung genehmigen, bevor sie als "bestellt" gilt.
// Nach Genehmigung simuliert eine 10-Sekunden-Pause die Lieferung.
public class LogisticsController extends BaseController {

    // --- FXML-Verknüpfungen ---

    // Eingabefelder für neue Bestellungen
    @FXML private ComboBox<InventoryItem> cmbPartName;
    @FXML private TextField txtQuantity;
    @FXML private ComboBox<Employee> cmbOrderedBy;
    @FXML private TextField txtReason;
    @FXML private Button btnPlaceOrder;

    // Tabelle: ausstehende Genehmigungen
    @FXML private TableView<LogisticsOrder> pendingTable;
    @FXML private TableColumn<LogisticsOrder, String> colPendingNr;
    @FXML private TableColumn<LogisticsOrder, String> colPendingPart;
    @FXML private TableColumn<LogisticsOrder, Integer> colPendingQty;
    @FXML private TableColumn<LogisticsOrder, String> colPendingBy;
    @FXML private TableColumn<LogisticsOrder, String> colPendingReason;
    @FXML private TableColumn<LogisticsOrder, Void>   colPendingAction;

    // Tabelle: alle Bestellungen (gesamte Übersicht)
    @FXML private TableView<LogisticsOrder> allOrdersTable;
    @FXML private TableColumn<LogisticsOrder, String> colAllNr;
    @FXML private TableColumn<LogisticsOrder, String> colAllPart;
    @FXML private TableColumn<LogisticsOrder, Integer> colAllQty;
    @FXML private TableColumn<LogisticsOrder, String> colAllBy;
    @FXML private TableColumn<LogisticsOrder, String> colAllStatus;
    @FXML private TableColumn<LogisticsOrder, String> colAllApprovedBy;
    @FXML private TableColumn<LogisticsOrder, String> colAllDate;

    // Zurück-Button
    @FXML private Button btnBack;

    // Container für die Lagerbestandsanzeige
    @FXML private VBox stockContainer;

    // Observable-Listen für die reaktive Tabellenanzeige
    private final ObservableList<LogisticsOrder> pendingData    = FXCollections.observableArrayList();
    private final ObservableList<LogisticsOrder> allOrdersData  = FXCollections.observableArrayList();

    // SensorService zum Laden des aktuellen Lagerbestands
    private final SensorService sensorService = new SensorService();

    // Wird automatisch beim Laden der FXML aufgerufen
    @FXML
    public void initialize() {
        // Part-ComboBox mit allen Lagerartikeln befüllen; Anzeige: "Name  (Qty: X)"
        List<InventoryItem> inventory = sensorService.loadInventory();
        cmbPartName.setItems(FXCollections.observableArrayList(inventory));
        cmbPartName.setConverter(new javafx.util.StringConverter<InventoryItem>() {
            @Override public String toString(InventoryItem item) {
                return item == null ? "" : item.getName() + "  (Qty: " + item.getQuantity() + ")";
            }
            @Override public InventoryItem fromString(String s) { return null; }
        });

        // ComboBox mit allen Mitarbeitern befüllen
        cmbOrderedBy.setItems(FXCollections.observableArrayList(
            EmployeeService.getInstance().getAllEmployees()));

        // Tabellenspalten konfigurieren
        setupPendingTable();
        setupAllOrdersTable();

        // Bestehende Bestellungen aus dem OrderStore laden
        refreshTables();

        // Bestellen-Button
        btnPlaceOrder.setOnAction(e -> placeOrder());

        // Zurück-Button
        btnBack.setOnAction(e -> loadView("main_view.fxml"));
    }

    // Legt eine neue Bestellanfrage an und fügt sie dem OrderStore hinzu
    private void placeOrder() {
        // Eingaben validieren: alle Felder müssen ausgefüllt sein
        InventoryItem selectedPart = cmbPartName.getValue();
        String partName = selectedPart != null ? selectedPart.getName() : "";
        String qtyText  = txtQuantity.getText().trim();
        Employee orderedBy = cmbOrderedBy.getValue();
        String reason   = txtReason.getText().trim();

        if (partName.isEmpty() || qtyText.isEmpty() || orderedBy == null || reason.isEmpty()) {
            showAlert("Bitte alle Felder ausfüllen.", Alert.AlertType.WARNING);
            return;
        }

        // Menge als Zahl parsen
        int quantity;
        try {
            quantity = Integer.parseInt(qtyText);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showAlert("Menge muss eine positive ganze Zahl sein.", Alert.AlertType.WARNING);
            return;
        }

        // Bestellung im OrderStore anlegen (Status: PENDING_APPROVAL)
        OrderStore.getInstance().createOrder(partName, quantity, orderedBy, reason);

        // Tabellen und Eingabefelder aktualisieren
        refreshTables();
        cmbPartName.getSelectionModel().clearSelection();
        txtQuantity.clear();
        cmbOrderedBy.getSelectionModel().clearSelection();
        txtReason.clear();
    }

    // Konfiguriert die Tabelle "Pending Approval"
    private void setupPendingTable() {
        colPendingNr.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colPendingPart.setCellValueFactory(new PropertyValueFactory<>("partName"));
        colPendingQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colPendingBy.setCellValueFactory(new PropertyValueFactory<>("orderedByName"));
        colPendingReason.setCellValueFactory(new PropertyValueFactory<>("reason"));

        // Action-Spalte: zwei Buttons (Approve / Reject) pro Zeile
        colPendingAction.setCellFactory(col -> new TableCell<>() {
            // "Approve"-Button: grün, nur für Security Chiefs sinnvoll
            private final Button btnApprove = new Button("Approve");
            // "Reject"-Button: rot
            private final Button btnReject  = new Button("Reject");
            private final HBox box = new HBox(6, btnApprove, btnReject);

            {
                // Buttons stylen
                btnApprove.setStyle(
                    "-fx-background-color: #66ff66; -fx-text-fill: #1e1e1e; -fx-font-size: 11px;");
                btnReject.setStyle(
                    "-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-size: 11px;");

                // Approve: Bestellung genehmigen → 10s Liefersimulation starten
                btnApprove.setOnAction(e -> {
                    LogisticsOrder order = getTableView().getItems().get(getIndex());
                    // Ersten Security Chief als Genehmiger verwenden (Simulation)
                    List<Employee> chiefs = EmployeeService.getInstance().getByRole("Security Chief");
                    Employee approver = chiefs.isEmpty() ? null : chiefs.get(0);
                    if (approver == null) return;

                    // Bestellung genehmigen
                    order.approve(approver);
                    order.markOrdered();
                    refreshTables();

                    // 10-Sekunden-Simulation: danach wird Bestellung auf DELIVERED gesetzt
                    PauseTransition pause = new PauseTransition(Duration.seconds(10));
                    pause.setOnFinished(ev -> {
                        order.markDelivered();

                        // Lagerbestand erhöhen: bestellte Menge zum vorhandenen Bestand addieren
                        List<com.group5.shuttle.model.InventoryItem> inventory =
                            sensorService.loadInventory();
                        inventory.stream()
                            .filter(i -> i.getName().equals(order.getPartName()))
                            .findFirst()
                            .ifPresent(item -> {
                                item.setQuantity(item.getQuantity() + order.getQuantity());
                                sensorService.saveInventory(inventory);
                            });

                        refreshTables(); // aktualisiert auch refreshStockDisplay()
                        // ComboBox neu befüllen, damit neue Mengen sichtbar sind
                        cmbPartName.setItems(FXCollections.observableArrayList(
                            sensorService.loadInventory()));

                        // Pop-up-Benachrichtigung: Teil eingetroffen
                        showAlert("✓ Bestellung " + order.getOrderNumber() + " eingetroffen!\n"
                            + "Teil: " + order.getPartName()
                            + " (Menge: " + order.getQuantity() + ")",
                            Alert.AlertType.INFORMATION);
                    });
                    pause.play();
                });

                // Reject: Bestellung ablehnen
                btnReject.setOnAction(e -> {
                    LogisticsOrder order = getTableView().getItems().get(getIndex());
                    List<Employee> chiefs = EmployeeService.getInstance().getByRole("Security Chief");
                    Employee approver = chiefs.isEmpty() ? null : chiefs.get(0);
                    if (approver == null) return;
                    order.reject(approver);
                    refreshTables();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                // Buttons nur anzeigen, wenn die Zeile nicht leer ist
                setGraphic(empty ? null : box);
            }
        });

        pendingTable.setItems(pendingData);
        pendingTable.setStyle("-fx-background: #2a2a2a; -fx-background-color: #2a2a2a;");
    }

    // Konfiguriert die Gesamtübersicht aller Bestellungen
    private void setupAllOrdersTable() {
        colAllNr.setCellValueFactory(new PropertyValueFactory<>("orderNumber"));
        colAllPart.setCellValueFactory(new PropertyValueFactory<>("partName"));
        colAllQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colAllBy.setCellValueFactory(new PropertyValueFactory<>("orderedByName"));
        colAllApprovedBy.setCellValueFactory(new PropertyValueFactory<>("approvedByName"));
        colAllDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));

        // Status-Spalte: farblich markiert (grün = geliefert, gelb = bestellt, orange = genehmigt,
        //                                   rot = abgelehnt, grau = ausstehend)
        colAllStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAllStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                String color = switch (status) {
                    case LogisticsOrder.DELIVERED        -> "#66ff66"; // grün
                    case LogisticsOrder.ORDERED          -> "#66aaff"; // blau
                    case LogisticsOrder.APPROVED         -> "#ffcc00"; // gelb
                    case LogisticsOrder.REJECTED         -> "#ff4444"; // rot
                    default /* PENDING_APPROVAL */       -> "#aaaaaa"; // grau
                };
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });

        allOrdersTable.setItems(allOrdersData);
        allOrdersTable.setStyle("-fx-background: #2a2a2a; -fx-background-color: #2a2a2a;");
    }

    // Aktualisiert beide Tabellen aus dem OrderStore
    private void refreshTables() {
        List<LogisticsOrder> all = OrderStore.getInstance().getOrders();

        // "Pending"-Tabelle: nur Bestellungen mit Status PENDING_APPROVAL
        pendingData.setAll(
            all.stream()
               .filter(o -> LogisticsOrder.PENDING_APPROVAL.equals(o.getStatus()))
               .toList());

        // Gesamtübersicht: alle Bestellungen
        allOrdersData.setAll(all);

        // Lagerbestandsanzeige aktualisieren
        refreshStockDisplay();
    }

    // Befüllt stockContainer mit farbigen Labels für jeden Lagerartikel
    private void refreshStockDisplay() {
        if (stockContainer == null) return;
        stockContainer.getChildren().clear();
        List<InventoryItem> items = sensorService.loadInventory();
        for (InventoryItem item : items) {
            String color = switch (item.getStatus()) {
                case "OUT_OF_STOCK" -> "#ff4444";
                case "LOW"          -> "#ffcc00";
                default             -> "#66ff66";
            };
            Label lbl = new Label(String.format("  %s  (Qty: %d)  [%s]",
                    item.getName(), item.getQuantity(), item.getStatus()));
            lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
            stockContainer.getChildren().add(lbl);
        }
    }

    // Zeigt einen einfachen Alert-Dialog mit einer Nachricht
    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // Gibt den Zurück-Button für BaseController zurück
    @Override
    protected Button getNavigationButton() { return btnBack; }
}
