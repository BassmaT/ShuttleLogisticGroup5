package com.group5.shuttle.controller;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.StockStatus;
import com.group5.shuttle.service.MaintenanceHistoryService;
import com.group5.shuttle.service.RepairInventoryService;
import com.group5.shuttle.model.RoutineTask;
import com.group5.shuttle.model.TakeoverSchedule;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.IInventoryService;
import com.group5.shuttle.service.IPartApproval;
import com.group5.shuttle.service.IRepairAccess;
import com.group5.shuttle.service.IScheduleService;
import com.group5.shuttle.service.IWorkerRegistry;
import com.group5.shuttle.service.InventoryService;
import com.group5.shuttle.service.RoutineTaskStore;
import com.group5.shuttle.service.ScheduleService;
import com.group5.shuttle.service.SessionState;
import com.group5.shuttle.service.TakeoverState;
import com.group5.shuttle.util.Dialogs;
import com.group5.shuttle.util.EmployeeComboHelper;
import com.group5.shuttle.util.EmployeeStringConverter;
import com.group5.shuttle.util.StatusColors;
import com.group5.shuttle.util.Styles;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
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

    /** ComboBox zur Auswahl eines Mitarbeiters  */
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

    private final IWorkerRegistry    workerRegistry   = TakeoverState.getInstance();
    private final IRepairAccess      repairAccess     = TakeoverState.getInstance();
    private final IPartApproval      partApproval     = TakeoverState.getInstance();
    private final IInventoryService  inventoryService = InventoryService.getInstance();
    private final IScheduleService   scheduleService  = ScheduleService.getInstance();

    /** Schlüssel des aktuell ausgewählten Teils (z. B. "orbiter", "srb", "externalTank"). */
    private String selectedPartKey = null;
    // OCP: neuer Part → hier einen Eintrag ergänzen; highlightPartButton() bleibt unverändert.
    private Map<String, Button> partButtons;
    private InventoryStatusPanelController inventoryPanel;

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
        partButtons = Map.of("orbiter", btnPartOrbiter, "srb", btnPartSrb, "externalTank", btnPartTank);

        // ComboBox mit allen Mitarbeitern befüllen (Techniker und Security Chiefs)
        EmployeeComboHelper.setup(cmbEmployee);

        // Bestätigungs-Button mit confirmEntry() verknüpfen
        btnConfirm.setOnAction(e -> confirmEntry());

        // Reparaturtabelle mit allen Spalten und Zell-Factories einrichten
        setupRepairTable();

        // Routineaufgaben-Tabelle einrichten
        setupRoutineTable();
        inventoryPanel = new InventoryStatusPanelController(inventoryStatus, lblInventoryHeader, inventoryService);

        // "Technician Done"-Button: Techniker meldet alle Reparaturen als abgeschlossen
        btnTechDone.setOnAction(e -> {
            if (selectedPartKey != null) {
                // Namen des Technikers aus dem Zustand laden (Fallback: "Technician")
                String name = workerRegistry.getTechnicianName(selectedPartKey);
                repairAccess.markTechnicianDone(selectedPartKey, name != null ? name : "Technician");
                // Abgeschlossene Reparaturen in der Wartungshistorie protokollieren
                MaintenanceHistoryService.logRepairs(selectedPartKey, repairAccess, workerRegistry);
                // Arbeitsbereich aktualisieren
                refreshWorkPanel();
            }
        });

        // Freigabe-Button: Security Chief erteilt die endgültige Genehmigung
        btnApprove.setOnAction(e -> {
            if (selectedPartKey != null && partApproval.canApprove(selectedPartKey)) {
                // Namen des Security Chiefs aus dem Zustand laden (Fallback: "Security Chief")
                String name = workerRegistry.getSecurityChiefName(selectedPartKey);
                partApproval.approve(selectedPartKey, name != null ? name : "Security Chief");
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
                Dialogs.showError("Navigationsfehler", "Ansicht konnte nicht geladen werden", ex.getMessage());
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
    private void selectPart(String partKey) {
        selectedPartKey = partKey;
        highlightPartButton();

        String displayName = TakeoverState.getDisplayName(partKey);
        Employee user = SessionState.getInstance().getCurrentUser();

        if (user != null) {
            // Auto-register current user by role; keeps existing registration of the other role
            workerRegistry.registerWorker(partKey, user.getName(), user.getRole());
            lblSelectedPart.setText(displayName + " — " + EmployeeStringConverter.INSTANCE.toString(user));
            lblSelectedPart.setStyle(Styles.label13("#66aaff"));
            rolePanel.setVisible(false);
            rolePanel.setManaged(false);
            showWorkPanel();
        } else {
            // Fallback: manual selection (only reachable if no user is logged in)
            lblSelectedPart.setText("Part: " + displayName);
            List<Employee> assignedEmps = getAssignedEmployeesForPart(partKey);
            cmbEmployee.setItems(FXCollections.observableArrayList(assignedEmps));
            cmbEmployee.getSelectionModel().clearSelection();
            rolePanel.setVisible(true);
            rolePanel.setManaged(true);
            workPanel.setVisible(false);
            workPanel.setManaged(false);
        }
    }

    /**
     * Hebt den aktuell ausgewählten Teil-Button blau hervor und
     * setzt alle anderen Buttons auf den Standardstil zurück.
     */
    private void highlightPartButton() {
        partButtons.forEach((key, btn) ->
            btn.setStyle(key.equals(selectedPartKey) ? Styles.PART_BTN_SELECTED : Styles.PART_BTN_NORMAL));
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
            lblSelectedPart.setStyle(Styles.label13("#ff4444"));
            return;
        }

        // Name und Rolle direkt aus dem Employee-Objekt lesen
        String name = emp.getName();
        String role = emp.getRole();

        // Mitarbeiter mit Name und Rolle im zentralen Zustand registrieren
        workerRegistry.registerWorker(selectedPartKey, name, role);

        // Label zurück auf den normalen Teile-Namen setzen (grau, keine Fehlermeldung)
        lblSelectedPart.setText("Part: " + TakeoverState.getDisplayName(selectedPartKey));
        lblSelectedPart.setStyle(Styles.label13("#aaaaaa"));

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
        colStatus.setCellFactory(col -> new SensorStatusCell());

        // Parts-Spalte – zeigt entweder einen "Order Part"-Button oder eine Statusanzeige
        colParts.setCellValueFactory(c -> c.getValue().partStatusProperty());
        colParts.setCellFactory(col -> new PartStatusCell(this::placeOrder));

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
        RepairInventoryService.initTaskStatuses(repairAccess.getRepairs(selectedPartKey), inventoryService);
        repairTable.refresh();
    }

    /**
     * Wird aufgerufen, wenn der Techniker auf "Order Part" klickt.
     * Setzt den Status sofort auf ORDERED, zeigt eine Bestätigungsmeldung
     * und bucht das Teil nach 10 Sekunden automatisch ins Lager ein.
     *
     * @param task die Reparaturaufgabe, für die das Teil bestellt werden soll
     */
    private void placeOrder(RepairTask task) {
        RepairInventoryService.placeOrder(task, inventoryService, () -> {
            repairTable.refresh();
            refreshInventory();
            Dialogs.showInfo("Part Arrived",
                    "\"" + task.getRequiredItemName() + "\" has arrived in the warehouse!\n" +
                    "You can now mark the repair as done.");
        });
        repairTable.refresh();
        Dialogs.showInfo("Part Ordered",
                "\"" + task.getRequiredItemName() + "\" is out of stock.\n" +
                "An order has been placed – expected arrival in ~10 seconds.");
    }

    /**
     * Bucht das für eine Reparaturaufgabe benötigte Teil aus dem Lager ab,
     * sobald die Aufgabe als erledigt markiert wird.
     *
     * @param task die abgeschlossene Reparaturaufgabe
     */
    private void deductInventory(RepairTask task) {
        RepairInventoryService.deductInventory(task, inventoryService, this::refreshInventory);
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
        boolean approved   = partApproval.isPartApproved(selectedPartKey);
        boolean techDone   = repairAccess.isTechnicianDone(selectedPartKey);
        String displayName = TakeoverState.getDisplayName(selectedPartKey);
        String techName    = workerRegistry.getTechnicianName(selectedPartKey);
        String chiefName   = workerRegistry.getSecurityChiefName(selectedPartKey);

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
        List<RepairTask> tasks = repairAccess.getRepairs(selectedPartKey);
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
    if (selectedPartKey == null) return;
    inventoryPanel.update(TakeoverState.getDisplayName(selectedPartKey));
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
        boolean approved   = partApproval.isPartApproved(selectedPartKey);
        boolean canApprove = partApproval.canApprove(selectedPartKey);
        boolean techDone   = repairAccess.isTechnicianDone(selectedPartKey);

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

    // ── Hilfsmethoden ─────────────────────────────────────────────────────────────

    /**
     * Richtet die Routineaufgaben-Tabelle ein:
     * Aufgabenname, Dauer, Done-Checkbox (setzt completedAt) und Zeitstempel.
     */
    private void setupRoutineTable() {
        RoutineTableHelper.setup(routineTable, colRtName, colRtEst, colRtDone, colRtTime);
    }

    /**
     * Gibt die Liste der Mitarbeiter zurück, die laut  für diesen Part eingeplant sind.
     * Fallback: alle Mitarbeiter, falls kein Schedule geladen werden kann.
     */
    private List<Employee> getAssignedEmployeesForPart(String partKey) {
        String displayName = TakeoverState.getDisplayName(partKey);
        TakeoverSchedule schedule = scheduleService.loadSchedule();
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

    /** Gibt den Zurück-Button zurück, der für die Navigation in der Basisklasse genutzt wird. */
    @Override
    protected Button getNavigationButton() { return btnBack; }
}
