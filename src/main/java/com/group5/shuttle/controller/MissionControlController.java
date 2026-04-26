package com.group5.shuttle.controller;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.model.MaintenanceTicket;
import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.RoutineTask;
import com.group5.shuttle.model.TakeoverSchedule;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.RoutineTaskStore;
import com.group5.shuttle.service.SensorService;
import com.group5.shuttle.service.TakeoverState;

import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller für Mission Control – steuert den 2-Schritt-Ablauf:
 * erst Teil wählen (Orbiter / SRB / External Tank),
 * dann Rolle (Techniker oder Security Chief) und Name eingeben.
 * Anschließend wird der Arbeitsbereich mit Reparaturtabelle,
 * Lagerstatus und Freigabe-Bereich eingeblendet.
 */
public class MissionControlController extends BaseController {

    // ── Navigation ──────────────────────────────────────────────────────────────

    /** Zurück-Button – navigiert zur Hauptansicht (main_view.fxml). */
    @FXML private Button btnBack;

    // ── Schritt 1: Part-Auswahl ──────────────────────────────────────────────────

    /** Auswahl-Button für das Raumschiff-Hauptmodul (Orbiter). */
    @FXML private Button btnPartOrbiter;

    /** Auswahl-Button für die Feststoffraketen-Booster (SRB). */
    @FXML private Button btnPartSrb;

    /** Auswahl-Button für den externen Treibstofftank (External Tank). */
    @FXML private Button btnPartTank;

    // ── Schritt 2: Rolle + Name eingeben ─────────────────────────────────────────

    /** Panel für die Rollen- und Namenseingabe; initial ausgeblendet. */
    @FXML private VBox rolePanel;

    /** Zeigt an, welches Teil gerade ausgewählt ist (oder Fehlermeldung bei leerem Namen). */
    @FXML private Label lblSelectedPart;

    /** ComboBox zur Auswahl eines Mitarbeiters aus employees.json. */
    @FXML private ComboBox<Employee> cmbEmployee;

    /** Bestätigungs-Button – registriert den gewählten Mitarbeiter im TakeoverState. */
    @FXML private Button btnConfirm;

    // ── Arbeitsbereich ───────────────────────────────────────────────────────────

    /** Hauptpanel des Arbeitsbereichs; wird nach der Anmeldung eingeblendet. */
    @FXML private VBox workPanel;

    /** Kopfzeile der Reparaturtabelle – zeigt Teilname und ggf. Genehmigungsstatus. */
    @FXML private Label lblRepairHeader;

    /** Tabelle mit allen Reparaturaufgaben für das gewählte Teil. */
    @FXML private TableView<RepairTask> repairTable;

    /** Spalte: Name des betroffenen Sensors. */
    @FXML private TableColumn<RepairTask, String>  colSensor;

    /** Spalte: aktueller Fehlerstatus des Sensors (z. B. REPLACE / DEGRADED). */
    @FXML private TableColumn<RepairTask, String>  colStatus;

    /** Spalte: empfohlene Reparaturmaßnahme. */
    @FXML private TableColumn<RepairTask, String>  colAction;

    /** Spalte: Teile-Status mit "Order Part"-Button oder Statusanzeige (IN_STOCK, ORDERED …). */
    @FXML private TableColumn<RepairTask, String>  colParts;

    /** Spalte: Checkbox zum Markieren einer abgeschlossenen Reparatur. */
    @FXML private TableColumn<RepairTask, Boolean> colDone;

    /** Button "Technician Done" – der Techniker signalisiert, dass alle Reparaturen fertig sind. */
    @FXML private Button btnTechDone;

    // ── Routine Tasks ─────────────────────────────────────────────────────────────

    /** Überschrift der Routineaufgaben-Tabelle. */
    @FXML private Label lblRoutineHeader;

    /** Tabelle mit Routineaufgaben für das gewählte Teil. */
    @FXML private TableView<RoutineTask> routineTable;

    /** Spalte: Aufgabenname. */
    @FXML private TableColumn<RoutineTask, String>  colRtName;

    /** Spalte: Geschätzte Dauer in Minuten. */
    @FXML private TableColumn<RoutineTask, Integer> colRtEst;

    /** Spalte: Done-Checkbox. */
    @FXML private TableColumn<RoutineTask, Boolean> colRtDone;

    /** Spalte: Erledigungszeitstempel. */
    @FXML private TableColumn<RoutineTask, String>  colRtTime;

    // ── Lagerbestand ─────────────────────────────────────────────────────────────

    /** Überschrift über der Lagerbestandsanzeige; zeigt das ausgewählte Teil. */
    @FXML private Label lblInventoryHeader;

    /** VBox-Container, der dynamisch erzeugte Lager-Labels aufnimmt. */
    @FXML private VBox inventoryStatus;

    // ── Security-Chief-Freigabe ──────────────────────────────────────────────────

    /** Bereich für die Freigabe durch den Security Chief; nur sichtbar wenn Chief angemeldet. */
    @FXML private VBox approvalSection;

    /** Statusinformation zur Freigabe (z. B. "Warte auf Techniker" oder "Bereit zur Genehmigung"). */
    @FXML private Label lblApprovalInfo;

    /** Freigabe-Button – wird nur aktiviert, wenn der Techniker als fertig markiert hat. */
    @FXML private Button btnApprove;

    // ── Zustand und Services ─────────────────────────────────────────────────────

    /** Singleton-Instanz des gemeinsamen Übergabe-Zustands (TakeoverState). */
    private final TakeoverState state = TakeoverState.getInstance();

    /** Service zum Laden und Speichern von Inventar, Tickets und Sensordaten. */
    private final SensorService sensorService = new SensorService();

    /** Schlüssel des aktuell ausgewählten Teils (z. B. "orbiter", "srb", "externalTank"). */
    private String selectedPartKey = null;

    // ── Initialisierung ──────────────────────────────────────────────────────────

    /**
     * Wird automatisch nach dem Laden der FXML-Datei aufgerufen.
     * Alle Buttons werden mit Aktionen verknüpft, Panels initial ausgeblendet.
     */
    @FXML
    public void initialize() {
        // Teil-Auswahl-Buttons mit der selectPart()-Methode verknüpfen
        btnPartOrbiter.setOnAction(e -> selectPart("orbiter"));
        btnPartSrb.setOnAction(e -> selectPart("srb"));
        btnPartTank.setOnAction(e -> selectPart("externalTank"));

        // ComboBox mit allen Mitarbeitern befüllen (Techniker und Security Chiefs)
        cmbEmployee.setItems(FXCollections.observableArrayList(
            EmployeeService.getInstance().getAllEmployees()));
        // Anzeige in der ComboBox: "Name (Rolle)" – z. B. "Max Müller (Technician)"
        cmbEmployee.setConverter(new javafx.util.StringConverter<Employee>() {
            @Override public String toString(Employee e) {
                return e == null ? "" : e.getName() + " (" + e.getRole() + ")";
            }
            @Override public Employee fromString(String s) { return null; }
        });

        // Bestätigungs-Button mit confirmEntry() verknüpfen
        btnConfirm.setOnAction(e -> confirmEntry());

        // Reparaturtabelle mit allen Spalten und Zell-Factories einrichten
        setupRepairTable();

        // Routineaufgaben-Tabelle einrichten
        setupRoutineTable();

        // "Technician Done"-Button: Techniker meldet alle Reparaturen als abgeschlossen
        btnTechDone.setOnAction(e -> {
            if (selectedPartKey != null) {
                // Namen des Technikers aus dem Zustand laden (Fallback: "Technician")
                String name = state.getTechnicianName(selectedPartKey);
                state.markTechnicianDone(selectedPartKey, name != null ? name : "Technician");
                // Abgeschlossene Reparaturen in der Wartungshistorie protokollieren
                logRepairsToHistory();
                // Arbeitsbereich aktualisieren
                refreshWorkPanel();
            }
        });

        // Freigabe-Button: Security Chief erteilt die endgültige Genehmigung
        btnApprove.setOnAction(e -> {
            if (selectedPartKey != null && state.canApprove(selectedPartKey)) {
                // Namen des Security Chiefs aus dem Zustand laden (Fallback: "Security Chief")
                String name = state.getSecurityChiefName(selectedPartKey);
                state.approve(selectedPartKey, name != null ? name : "Security Chief");
                // Arbeitsbereich aktualisieren und ausgewählten Button blau hervorheben
                refreshWorkPanel();
                highlightPartButton();
            }
        });

        // Rollen-Panel und Arbeitsbereich zu Beginn ausblenden
        rolePanel.setVisible(false);
        rolePanel.setManaged(false);
        workPanel.setVisible(false);
        workPanel.setManaged(false);

        // Zurück-Button: zur Hauptansicht navigieren und Dashboard aktualisieren
        btnBack.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml"));
                Parent root = loader.load();
                MainController mc = loader.getController();
                mc.updateDashboard();
                Stage stage = (Stage) btnBack.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // ── Teil-Auswahl ─────────────────────────────────────────────────────────────

    /**
     * Wird aufgerufen, wenn der Benutzer einen Teil-Auswahl-Button klickt.
     * Speichert den gewählten Teil-Schlüssel, befüllt das Namensfeld mit dem
     * bereits registrierten Mitarbeiter und blendet die Panels ein.
     *
     * @param partKey interner Schlüssel des Teils ("orbiter", "srb", "externalTank")
     */
    private void selectPart(String partKey) { //KI
        // Ausgewählten Teil-Schlüssel global speichern
        selectedPartKey = partKey;
        // Ausgewählten Button blau hervorheben, alle anderen zurücksetzen
        highlightPartButton();

        // Anzeigename des Teils ermitteln und im Label anzeigen
        String displayName = TakeoverState.getDisplayName(partKey);
        lblSelectedPart.setText("Part: " + displayName);

        // ComboBox mit gefilterten Mitarbeitern befüllen (nur die für diesen Part eingeplanten)
        List<Employee> assignedEmps = getAssignedEmployeesForPart(partKey);
        cmbEmployee.setItems(FXCollections.observableArrayList(assignedEmps));
        String techName  = state.getTechnicianName(partKey);
        String chiefName = state.getSecurityChiefName(partKey);
        String existingName = techName != null ? techName : chiefName;
        if (existingName != null) {
            // Passenden Mitarbeiter in der gefilterten Liste vorauswählen
            assignedEmps.stream()
                .filter(e -> e.getName().equals(existingName))
                .findFirst()
                .ifPresent(e -> cmbEmployee.getSelectionModel().select(e));
        } else {
            cmbEmployee.getSelectionModel().clearSelection();
        }

        // Rollen-Panel einblenden, damit Name und Rolle eingegeben oder bestätigt werden können
        rolePanel.setVisible(true);
        rolePanel.setManaged(true);

        // Arbeitsbereich nur anzeigen, wenn bereits ein Mitarbeiter für dieses Teil angemeldet ist
        if (techName != null || chiefName != null) {
            showWorkPanel();
        } else {
            // Noch kein Mitarbeiter angemeldet – Arbeitsbereich ausblenden
            workPanel.setVisible(false);
            workPanel.setManaged(false);
        }
    }

    /**
     * Hebt den aktuell ausgewählten Teil-Button blau hervor und
     * setzt alle anderen Buttons auf den Standardstil zurück.
     */
    private void highlightPartButton() {
        String normal   = "-fx-font-size: 18px; -fx-background-color: #21262d; -fx-text-fill: #8b949e; -fx-padding: 22 0; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #30363d; -fx-border-radius: 8; -fx-font-weight: bold;";
        String selected = "-fx-font-size: 18px; -fx-background-color: rgba(75,156,255,0.15); -fx-text-fill: #4B9CFF; -fx-padding: 22 0; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #4B9CFF; -fx-border-radius: 8; -fx-font-weight: bold;";
        // Jeden Button abhängig vom aktuellen selectedPartKey einfärben
        btnPartOrbiter.setStyle("orbiter".equals(selectedPartKey)     ? selected : normal);
        btnPartSrb.setStyle(    "srb".equals(selectedPartKey)          ? selected : normal);
        btnPartTank.setStyle(   "externalTank".equals(selectedPartKey) ? selected : normal);
    }

    /**
     * Bestätigt die Eingabe von Name und Rolle und registriert den Mitarbeiter
     * im TakeoverState. Zeigt eine Fehlermeldung, wenn das Namensfeld leer ist.
     */
    private void confirmEntry() {
        // Ohne ausgewähltes Teil keine Aktion möglich
        if (selectedPartKey == null) return;

        // Mitarbeiter aus der ComboBox lesen – muss ausgewählt sein
        Employee emp = cmbEmployee.getValue();
        if (emp == null) {
            // Fehlermeldung anzeigen
            lblSelectedPart.setText("Please select an employee!");
            lblSelectedPart.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 13px;");
            return;
        }

        // Name und Rolle direkt aus dem Employee-Objekt lesen
        String name = emp.getName();
        String role = emp.getRole();

        // Mitarbeiter mit Name und Rolle im zentralen Zustand registrieren
        state.registerWorker(selectedPartKey, name, role);

        // Label zurück auf den normalen Teile-Namen setzen (grau, keine Fehlermeldung)
        lblSelectedPart.setText("Part: " + TakeoverState.getDisplayName(selectedPartKey));
        lblSelectedPart.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 13px;");

        // Arbeitsbereich nach erfolgreicher Registrierung einblenden
        showWorkPanel();
    }

    // ── Tabellen-Einrichtung ─────────────────────────────────────────────────────

    /**
     * Richtet alle Tabellenspalten ein:
     * – Status-Spalte mit farbiger Darstellung (REPLACE = rot, sonst gelb),
     * – Parts-Spalte mit "Order Part"-Button oder Statusanzeige,
     * – Done-Checkbox, die deaktiviert ist, solange das benötigte Teil nicht vorrätig ist.
     */
    private void setupRepairTable() {
        // Tabelle als editierbar markieren (für die Done-Checkboxen)
        repairTable.setEditable(true);

        // Sensor-Name und Aktion direkt aus dem Datenmodell binden
        colSensor.setCellValueFactory(new PropertyValueFactory<>("sensorName"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));

        // Status-Spalte – farbige Textdarstellung je nach Status
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                // REPLACE wird rot dargestellt, alle anderen Status gelb
                setStyle(item.equals("REPLACE")
                        ? "-fx-text-fill: #ff4444; -fx-font-weight: bold;"
                        : "-fx-text-fill: #ffcc00; -fx-font-weight: bold;");
            }
        });

        // Parts-Spalte – zeigt entweder einen "Order Part"-Button oder eine Statusanzeige
        colParts.setCellValueFactory(c -> c.getValue().partStatusProperty());
        colParts.setCellFactory(col -> new TableCell<>() {
            // "Order Part"-Button zum Bestellen eines nicht vorrätigen Teils
            private final Button btnOrder = new Button("Order Part");
            // Label für alle anderen Zustände (IN_STOCK, ORDERED, ARRIVED, NONE)
            private final Label  lblPart  = new Label();
            {
                // Stil des Bestellbuttons: orange, fett
                btnOrder.setStyle(
                    "-fx-font-size: 11px; -fx-background-color: #ff8800; " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 8;");
                // Klick auf den Button löst die placeOrder()-Methode für diese Aufgabe aus
                btnOrder.setOnAction(e -> {
                    RepairTask task = getTableRow() != null ? getTableRow().getItem() : null;
                    if (task != null) placeOrder(task);
                });
            }
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                // Je nach Teile-Status das passende Grafikelement anzeigen
                switch (status) {
                    case "IN_STOCK" -> {
                        // Teil ist vorrätig – grünes Häkchen mit Mengenangabe
                        lblPart.setText("✓ In Stock");
                        lblPart.setStyle("-fx-text-fill: #66ff66; -fx-font-size: 12px;");
                        setGraphic(lblPart);
                    }
                    case "OUT_OF_STOCK" -> setGraphic(btnOrder); // Nicht vorrätig – Bestell-Button anzeigen
                    case "ORDERED" -> {
                        // Bestellung läuft – gelbe Warteanzeige
                        lblPart.setText("⏳ Ordered...");
                        lblPart.setStyle("-fx-text-fill: #ffcc00; -fx-font-size: 12px;");
                        setGraphic(lblPart);
                    }
                    case "ARRIVED" -> {
                        // Teil eingetroffen – grüne Bestätigung
                        lblPart.setText("✓ Arrived");
                        lblPart.setStyle("-fx-text-fill: #66ff66; -fx-font-size: 12px;");
                        setGraphic(lblPart);
                    }
                    default -> {
                        // "NONE": kein Teil benötigt – neutrales Gedankenstrich-Label
                        lblPart.setText("–");
                        lblPart.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");
                        setGraphic(lblPart);
                    }
                }
            }
        });

        // Done-Spalte – Checkbox zum Abhaken einer Reparatur,
        // deaktiviert wenn das benötigte Teil noch nicht verfügbar ist
        colDone.setCellValueFactory(c -> c.getValue().doneProperty());
        colDone.setEditable(true);
        colDone.setCellFactory(col -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setOnAction(e -> {
                    RepairTask task = getTableRow() != null ? getTableRow().getItem() : null;
                    if (task == null) return;
                    // Erledigungsstatus der Aufgabe aktualisieren
                    task.setDone(cb.isSelected());
                    // Wird die Checkbox angehakt, das benötigte Teil aus dem Lager abbuchen
                    if (cb.isSelected()) deductInventory(task);
                    // Freigabe-Button neu bewerten (aktivieren/deaktivieren)
                    refreshApproveState();
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                RepairTask task = getTableRow().getItem();
                // Aktuellen Erledigungsstatus in der Checkbox spiegeln
                cb.setSelected(task.isDone());
                // Checkbox deaktivieren, solange das benötigte Teil nicht verfügbar ist
                cb.setDisable(!task.isPartAvailable());
                // Zeile halbtransparent darstellen, wenn Teil noch nicht verfügbar
                setStyle(task.isPartAvailable() ? "" : "-fx-opacity: 0.4;");
                setGraphic(cb);
            }
        });
    }

    // ── Lager- und Bestell-Logik ──────────────────────────────────────────────────

    /**
     * Initialisiert den Teile-Status (partStatus / partAvailable) für jede Aufgabe
     * des aktuell gewählten Teils.
     * Aufgaben, die bereits den Status ORDERED oder ARRIVED haben, werden übersprungen,
     * damit ein laufender Timer nicht überschrieben wird.
     */
    private void initTaskPartStatus() {
        if (selectedPartKey == null) return;
        // Aktuellen Lagerbestand aus dem persistenten Speicher laden
        List<InventoryItem> inventory = sensorService.loadInventory();

        for (RepairTask task : state.getRepairs(selectedPartKey)) {
            // Bereits gestartete Bestellungen nicht zurücksetzen
            String existing = task.getPartStatus();
            if ("ORDERED".equals(existing) || "ARRIVED".equals(existing)) continue;

            if (task.getRequiredItemName() == null) {
                // Kein Teil benötigt – Aufgabe sofort freigeben
                task.setPartStatus("NONE");
                task.setPartAvailable(true);
            } else {
                // Prüfen ob das benötigte Teil im Lager vorhanden und Menge > 0
                InventoryItem item = inventory.stream()
                        .filter(i -> i.getName().equals(task.getRequiredItemName()))
                        .findFirst().orElse(null);
                if (item != null && item.getQuantity() > 0) {
                    // Teil vorrätig – Aufgabe freigeben
                    task.setPartStatus("IN_STOCK");
                    task.setPartAvailable(true);
                } else {
                    // Teil nicht vorrätig – Checkbox sperren, Bestell-Button anzeigen
                    task.setPartStatus("OUT_OF_STOCK");
                    task.setPartAvailable(false);
                }
            }
        }
        // Tabelle neu rendern, damit die aktualisierten Status sichtbar werden
        repairTable.refresh();
    }

    /**
     * Wird aufgerufen, wenn der Techniker auf "Order Part" klickt.
     * Setzt den Status sofort auf ORDERED, zeigt eine Bestätigungsmeldung
     * und bucht das Teil nach 10 Sekunden automatisch ins Lager ein.
     *
     * @param task die Reparaturaufgabe, für die das Teil bestellt werden soll
     */
    private void placeOrder(RepairTask task) { //KI
        // Status sofort auf "bestellt" setzen und Tabelle aktualisieren
        task.setPartStatus("ORDERED");
        repairTable.refresh();

        // Bestätigungs-Dialog anzeigen
        showAlert(Alert.AlertType.INFORMATION, "Part Ordered",
                "\"" + task.getRequiredItemName() + "\" is out of stock.\n" +
                "An order has been placed – expected arrival in ~10 seconds.");

        // Timer: nach 10 Sekunden das Teil einbuchen und Benachrichtigung anzeigen
        PauseTransition pause = new PauseTransition(Duration.seconds(10));
        pause.setOnFinished(evt -> {
            // Lagerbestand laden und Menge um 1 erhöhen (Wareneingang simulieren)
            List<InventoryItem> inventory = sensorService.loadInventory();
            inventory.stream()
                    .filter(i -> i.getName().equals(task.getRequiredItemName()))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setQuantity(item.getQuantity() + 1);
                        // Aktualisierten Bestand dauerhaft speichern
                        sensorService.saveInventory(inventory);
                    });

            // Status des Teils auf "eingetroffen" setzen und Checkbox freigeben
            task.setPartStatus("ARRIVED");
            task.setPartAvailable(true);
            repairTable.refresh();
            // Lageranzeige aktualisieren, damit neue Menge sichtbar wird
            refreshInventory();

            // Eingangsbenachrichtigung anzeigen
            showAlert(Alert.AlertType.INFORMATION, "Part Arrived",
                    "\"" + task.getRequiredItemName() + "\" has arrived in the warehouse!\n" +
                    "You can now mark the repair as done.");
        });
        pause.play();
    }

    /**
     * Bucht das für eine Reparaturaufgabe benötigte Teil aus dem Lager ab,
     * sobald die Aufgabe als erledigt markiert wird.
     *
     * @param task die abgeschlossene Reparaturaufgabe
     */
    private void deductInventory(RepairTask task) { //KI
        // Keine Abbuchung nötig, wenn kein Teil benötigt wird
        if (task.getRequiredItemName() == null) return;
        List<InventoryItem> inventory = sensorService.loadInventory();
        inventory.stream()
                .filter(i -> i.getName().equals(task.getRequiredItemName()))
                .findFirst()
                .ifPresent(item -> {
                    if (item.getQuantity() > 0) {
                        // Menge um 1 reduzieren und dauerhaft speichern
                        item.setQuantity(item.getQuantity() - 1);
                        sensorService.saveInventory(inventory);
                        // Lageranzeige nach der Abbuchung aktualisieren
                        refreshInventory();
                    }
                });
    }

    // ── Arbeitsbereich ────────────────────────────────────────────────────────────

    /**
     * Blendet den Arbeitsbereich ein und aktualisiert alle Unterkomponenten.
     */
    private void showWorkPanel() {
        workPanel.setVisible(true);
        workPanel.setManaged(true);
        refreshWorkPanel();
    }

    /**
     * Aktualisiert den gesamten Arbeitsbereich für das aktuell gewählte Teil:
     * – Kopfzeile mit Teilname, Mitarbeiternamen und Genehmigungsstatus,
     * – Reparaturtabelle mit aktuellem Teile-Status,
     * – Sichtbarkeit des "Technician Done"-Buttons,
     * – Lageranzeige,
     * – Freigabebereich für den Security Chief.
     */
    private void refreshWorkPanel() {
        if (selectedPartKey == null) return;

        // Aktuellen Zustand für das gewählte Teil abrufen
        boolean approved   = state.isPartApproved(selectedPartKey);
        boolean techDone   = state.isTechnicianDone(selectedPartKey);
        String displayName = TakeoverState.getDisplayName(selectedPartKey);
        String techName    = state.getTechnicianName(selectedPartKey);
        String chiefName   = state.getSecurityChiefName(selectedPartKey);

        // Kopfzeile je nach Genehmigungsstatus einfärben und beschriften
        if (approved) {
            // Teil vollständig genehmigt – grüne Erfolgsanzeige
            lblRepairHeader.setText(displayName + " – APPROVED ✓");
            lblRepairHeader.setStyle("-fx-text-fill: #66ff66; -fx-font-size: 15px; -fx-font-weight: bold;");
        } else {
            // Noch nicht genehmigt – Namen der angemeldeten Mitarbeiter anzeigen
            String worker = "";
            if (techName != null) worker += techName + " (Technician)";
            if (chiefName != null) { if (!worker.isEmpty()) worker += " / "; worker += chiefName + " (Security Chief)"; }
            lblRepairHeader.setText("Repair Tasks – " + displayName
                    + (worker.isEmpty() ? "" : "\n" + worker));
            lblRepairHeader.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        }

        // Reparaturaufgaben in der Tabelle anzeigen
        List<RepairTask> tasks = state.getRepairs(selectedPartKey);
        repairTable.setItems(FXCollections.observableArrayList(tasks));

        // Routineaufgaben für diesen Part laden und anzeigen
        routineTable.setItems(FXCollections.observableArrayList(
            RoutineTaskStore.getInstance().getTasksForPart(displayName)));

        // Teile-Status für jede Aufgabe initialisieren (ORDERED/ARRIVED-Zustände bleiben erhalten)
        initTaskPartStatus();

        // "Technician Done"-Button nur anzeigen wenn: Techniker angemeldet, noch nicht fertig, nicht genehmigt
        boolean isTechnician = techName != null && chiefName == null;
        boolean showTechDone = isTechnician && !techDone && !approved;
        btnTechDone.setVisible(showTechDone);
        btnTechDone.setManaged(showTechDone);

        // Lageranzeige für das gewählte Teil aktualisieren
        refreshInventory();

        // Freigabebereich nur anzeigen wenn ein Security Chief angemeldet ist
        boolean isSecurityChief = chiefName != null;
        approvalSection.setVisible(isSecurityChief);
        approvalSection.setManaged(isSecurityChief);
        if (isSecurityChief) refreshApproveState();
    }

    /**
     * Zeigt den Lagerstatus aller benötigten Teile für das aktuell gewählte Teil an.
     * Jedes Teil wird als farbiges Label dargestellt:
     * rot = nicht vorrätig, gelb = niedrig, grün = ausreichend.
     */
    private void refreshInventory() {
        // Bisherige Einträge entfernen
        inventoryStatus.getChildren().clear();
        if (selectedPartKey == null) return;

        // Überschrift mit dem ausgewählten Teilenamen aktualisieren
        String displayName = TakeoverState.getDisplayName(selectedPartKey);
        lblInventoryHeader.setText("Required Parts – " + displayName);

        // Alle Lagerartikel laden und die zum gewählten Teil passenden anzeigen
        List<InventoryItem> items = sensorService.loadInventory();
        boolean found = false;
        for (InventoryItem item : items) {
            if (item.getPart().equalsIgnoreCase(displayName)) {
                found = true;
                // Farbe je nach Lagerstatus bestimmen
                String color = switch (item.getStatus()) {
                    case "OUT_OF_STOCK" -> "#ff4444"; // Rot: nicht vorrätig
                    case "LOW"          -> "#ffcc00"; // Gelb: niedrig
                    default             -> "#66ff66"; // Grün: ausreichend
                };
                // Label mit Artikelname, Menge und Status erstellen
                Label lbl = new Label(String.format("  %s  (Qty: %d)  [%s]",
                        item.getName(), item.getQuantity(), item.getStatus()));
                lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px;");
                inventoryStatus.getChildren().add(lbl);
            }
        }
        // Hinweis anzeigen, wenn keine Artikel für dieses Teil vorhanden sind
        if (!found) {
            Label lbl = new Label("  No parts tracked for this section");
            lbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 13px;");
            inventoryStatus.getChildren().add(lbl);
        }
    }

    /**
     * Aktualisiert den Freigabe-Button und das Informations-Label des Security Chiefs.
     * – Bereits genehmigt: Button deaktiviert, grüner Text.
     * – Techniker fertig, Freigabe möglich: Button aktiviert.
     * – Techniker noch nicht fertig: Button deaktiviert, gelbe Warteinfo.
     */
    private void refreshApproveState() {
        if (selectedPartKey == null) return;

        // Aktuellen Genehmigungs- und Fertigstellungsstatus abrufen
        boolean approved   = state.isPartApproved(selectedPartKey);
        boolean canApprove = state.canApprove(selectedPartKey);
        boolean techDone   = state.isTechnicianDone(selectedPartKey);

        if (approved) {
            // Teil bereits genehmigt – Button deaktivieren und Erfolgsmeldung anzeigen
            lblApprovalInfo.setText("This part has been approved.");
            lblApprovalInfo.setStyle("-fx-text-fill: #66ff66;");
            btnApprove.setDisable(true);
            btnApprove.setText("Already Approved ✓");
        } else if (canApprove) {
            // Techniker ist fertig – Security-Chief-Freigabe kann erteilt werden
            lblApprovalInfo.setText("Technician marked done. Ready for approval.");
            lblApprovalInfo.setStyle("-fx-text-fill: #66ff66;");
            btnApprove.setDisable(false);
            btnApprove.setText("Give Security Chief OK");
        } else if (!techDone) {
            // Techniker noch nicht fertig – Freigabe-Button gesperrt, gelbe Warteinfo
            lblApprovalInfo.setText("Waiting for Technician to finish repairs.");
            lblApprovalInfo.setStyle("-fx-text-fill: #ffcc00;");
            btnApprove.setDisable(true);
            btnApprove.setText("Give Security Chief OK");
        }
    }

    // ── Wartungshistorie ──────────────────────────────────────────────────────────

    /**
     * Schreibt alle abgeschlossenen Reparaturaufgaben des aktuellen Teils
     * als Wartungstickets in die Wartungshistorie.
     * Nur Aufgaben, die als erledigt markiert sind (isDone() == true), werden gespeichert.
     */
    private void logRepairsToHistory() {
        if (selectedPartKey == null) return;

        // Namen des Technikers ermitteln (Fallback: "Unknown")
        String techName = state.getTechnicianName(selectedPartKey);
        if (techName == null) techName = "Unknown";

        // Anzeigename des Teils und aktuellen Zeitstempel erzeugen
        String displayName = TakeoverState.getDisplayName(selectedPartKey);
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Bestehende Tickets laden, um fortlaufende IDs zu vergeben
        List<MaintenanceTicket> tickets = sensorService.loadTickets();
        int nextId = tickets.size() + 1;

        // Für jede abgeschlossene Aufgabe ein neues Wartungsticket anlegen
        for (RepairTask task : state.getRepairs(selectedPartKey)) {
            // Nicht erledigte Aufgaben überspringen
            if (!task.isDone()) continue;

            MaintenanceTicket ticket = new MaintenanceTicket();
            // Ticket-ID im Format "TKT-001", "TKT-002" usw. vergeben
            ticket.id        = String.format("TKT-%03d", nextId++);
            ticket.date      = timestamp;
            ticket.part      = displayName;
            ticket.sensor    = task.getSensorName();
            ticket.oldStatus = task.getStatus();
            // Verwendetes Teil (falls vorhanden) an die Aktionsbeschreibung anhängen
            ticket.action    = task.getAction()
                    + (task.getRequiredItemName() != null
                       ? " – used: " + task.getRequiredItemName() : "");
            ticket.technician = techName;
            tickets.add(ticket);
        }
        // Alle Tickets dauerhaft speichern
        sensorService.saveTickets(tickets);
    }

    // ── Hilfsmethoden ─────────────────────────────────────────────────────────────

    /**
     * Richtet die Routineaufgaben-Tabelle ein:
     * Aufgabenname, Dauer, Done-Checkbox (setzt completedAt) und Zeitstempel.
     */
    private void setupRoutineTable() {
        colRtName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRtEst.setCellValueFactory(new PropertyValueFactory<>("estimatedMinutes"));

        // Done-Checkbox
        colRtDone.setCellValueFactory(data -> data.getValue().doneProperty());
        colRtDone.setCellFactory(col -> new CheckBoxTableCell<>());
        colRtDone.setEditable(true);
        routineTable.setEditable(true);

        // Listener: completedAt setzen wenn Checkbox angehakt wird
        routineTable.getItems().addListener(
            (javafx.collections.ListChangeListener<RoutineTask>) change -> {
                while (change.next()) {
                    for (RoutineTask task : change.getAddedSubList()) {
                        task.doneProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal) {
                                task.setCompletedAt(LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                            } else {
                                task.setCompletedAt(null);
                            }
                            routineTable.refresh();
                        });
                    }
                }
            });

        // Erledigungszeitstempel
        colRtTime.setCellValueFactory(new PropertyValueFactory<>("completedAt"));
        colRtTime.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String ts, boolean empty) {
                super.updateItem(ts, empty);
                if (empty || ts == null) { setText("–"); setStyle("-fx-text-fill: #666;"); }
                else { setText(ts); setStyle("-fx-text-fill: #66ff66;"); }
            }
        });

        routineTable.setStyle("-fx-background: #0D1117; -fx-background-color: #0D1117;");
    }

    /**
     * Gibt die Liste der Mitarbeiter zurück, die laut schedule.json für diesen Part eingeplant sind.
     * Fallback: alle Mitarbeiter, falls kein Schedule geladen werden kann.
     */
    private List<Employee> getAssignedEmployeesForPart(String partKey) {
        String displayName = TakeoverState.getDisplayName(partKey);
        TakeoverSchedule schedule = sensorService.loadSchedule();
        if (schedule == null || schedule.getDays() == null) {
            return EmployeeService.getInstance().getAllEmployees();
        }
        Set<String> ids = schedule.getDays().stream()
            .flatMap(d -> d.getEntries().stream())
            .filter(e -> displayName.equals(e.getShuttlePart()))
            .map(e -> e.getAssignedEmployeeId())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (ids.isEmpty()) return EmployeeService.getInstance().getAllEmployees();
        return EmployeeService.getInstance().getAllEmployees().stream()
            .filter(e -> ids.contains(e.getId()))
            .collect(Collectors.toList());
    }

    /**
     * Zeigt einen modalen Alert-Dialog mit dem angegebenen Typ, Titel und Text an.
     *
     * @param type    Art des Dialogs (INFORMATION, WARNING, ERROR …)
     * @param title   Fenstertitel des Dialogs
     * @param message Anzuzeigender Nachrichtentext
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    /** Gibt den Zurück-Button zurück, der für die Navigation in der Basisklasse genutzt wird. */
    @Override
    protected Button getNavigationButton() { return btnBack; }
}
