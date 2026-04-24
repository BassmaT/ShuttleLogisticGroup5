package com.group5.shuttle.controller;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.RoutineTask;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.RoutineTaskStore;
import com.group5.shuttle.service.TakeoverState;
import com.group5.shuttle.util.ColoredTableCell;
import com.group5.shuttle.util.StatusColors;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// Controller für die Staff-Übersicht.
// Links: Liste aller Mitarbeiter.
// Rechts: Reparatur- und Routineaufgaben des ausgewählten Mitarbeiters.
public class StaffController extends BaseController {

    // Zeitformat für den Erledigungszeitstempel
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // --- FXML-Verknüpfungen ---

    // Liste aller Mitarbeiter in der linken Seitenleiste
    @FXML private ListView<Employee> employeeList;

    // Anzeige von Name, Rolle und Team des ausgewählten Mitarbeiters
    @FXML private Label lblEmployeeName;
    @FXML private Label lblEmployeeRole;
    @FXML private Label lblEmployeeTeam;

    // Tabelle der Reparaturaufgaben
    @FXML private TableView<RepairTask> repairTaskTable;
    @FXML private TableColumn<RepairTask, String> colRepairPart;
    @FXML private TableColumn<RepairTask, String> colRepairSensor;
    @FXML private TableColumn<RepairTask, String> colRepairAction;
    @FXML private TableColumn<RepairTask, String> colRepairStatus;

    // Tabelle der Routineaufgaben
    @FXML private TableView<RoutineTask> routineTaskTable;
    @FXML private TableColumn<RoutineTask, String>  colRoutineName;
    @FXML private TableColumn<RoutineTask, String>  colRoutinePart;
    @FXML private TableColumn<RoutineTask, Integer> colRoutineEst;
    @FXML private TableColumn<RoutineTask, Boolean> colRoutineDone;
    @FXML private TableColumn<RoutineTask, String>  colRoutineTime;

    // Zurück-zum-Dashboard-Button (in BaseController mit Navigation verknüpft)
    @FXML private javafx.scene.control.Button btnBack;

    // BaseController benötigt diesen Button, um das Fenster (Stage) zu ermitteln
    @Override
    protected javafx.scene.control.Button getNavigationButton() { return btnBack; }

    // Wird beim Laden der FXML aufgerufen – initialisiert alle Tabellen und die Mitarbeiterliste
    @FXML
    public void initialize() {
        setupRepairTable();     // Spalten der Reparaturtabelle konfigurieren
        setupRoutineTable();    // Spalten der Routinetabelle konfigurieren
        loadEmployees();        // Mitarbeiterliste befüllen

        // Wenn ein Mitarbeiter in der Liste ausgewählt wird, Details rechts anzeigen
        employeeList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) showEmployee(newVal);
            });

        // Zurück-Button verknüpfen
        btnBack.setOnAction(e -> loadView("main_view.fxml"));
    }

    // Befüllt die ListView mit allen Mitarbeitern aus EmployeeService
    private void loadEmployees() {
        List<Employee> employees = EmployeeService.getInstance().getAllEmployees();
        employeeList.setItems(FXCollections.observableArrayList(employees));

        // Benutzerdefinierten Zellinhalt: "Name (Rolle) – Team"
        employeeList.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Employee e, boolean empty) {
                super.updateItem(e, empty);
                if (empty || e == null) {
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    setText(e.getName() + "\n" + e.getRole() + " – " + e.getTeam());
                    setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-background-color: transparent;");
                }
            }
        });
    }

    // Zeigt Name, Rolle, Team und Tasks des ausgewählten Mitarbeiters an
    private void showEmployee(Employee emp) {
        // Kopfzeile aktualisieren
        lblEmployeeName.setText(emp.getName());
        lblEmployeeRole.setText(emp.getRole());
        lblEmployeeTeam.setText(emp.getTeam());

        // Reparaturaufgaben: alle Parts durchsuchen, Aufgaben mit diesem Mitarbeiter zusammensammeln
        List<RepairTask> assignedRepairs = new ArrayList<>();
        TakeoverState state = TakeoverState.getInstance();
        for (String partKey : TakeoverState.PART_KEYS) {
            String techName = state.getTechnicianName(partKey);
            // Wenn dieser Mitarbeiter als Techniker für diesen Part eingetragen ist
            if (emp.getName().equals(techName)) {
                assignedRepairs.addAll(state.getRepairs(partKey));
            }
        }
        repairTaskTable.setItems(FXCollections.observableArrayList(assignedRepairs));

        // Routineaufgaben: alle Aufgaben, die diesem Mitarbeiter zugewiesen sind
        List<RoutineTask> assignedRoutine =
            RoutineTaskStore.getInstance().getTasksForEmployee(emp.getId());
        routineTaskTable.setItems(FXCollections.observableArrayList(assignedRoutine));
    }

    // Konfiguriert die Spalten der Reparaturtabelle
    private void setupRepairTable() {
        // Part-Spalte: zeigt den Anzeigenamen des Shuttle-Teils
        colRepairPart.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                TakeoverState.getDisplayName(data.getValue().getPartKey())));

        // Sensor-Spalte
        colRepairSensor.setCellValueFactory(new PropertyValueFactory<>("sensorName"));

        // Action-Spalte
        colRepairAction.setCellValueFactory(new PropertyValueFactory<>("action"));

        // Status-Spalte mit Farbcodierung (gelb = WARNING, rot = REPLACE, grün = erledigt)
        colRepairStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRepairStatus.setCellFactory(col -> new ColoredTableCell<>(StatusColors::forSensorStatus));

        // Hintergrundfarbe der Tabelle und Textfarbe für alle Zellen setzen
        repairTaskTable.setStyle("-fx-background: #2a2a2a; -fx-background-color: #2a2a2a;");
    }

    // Konfiguriert die Spalten der Routineaufgaben-Tabelle
    private void setupRoutineTable() {
        // Aufgabenname – durchgestrichen und grau, wenn die Aufgabe erledigt ist
        colRoutineName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colRoutineName.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle(""); return;
                }
                RoutineTask task = getTableRow().getItem();
                setText(name);
                boolean done = task.isDone();
                setStyle(done
                    ? "-fx-strikethrough: true; -fx-text-fill: #666666;"
                    : "-fx-strikethrough: false; -fx-text-fill: white;");
                // Tabelle neu rendern, wenn done-Status sich ändert
                task.doneProperty().addListener((obs, o, n) -> routineTaskTable.refresh());
            }
        });

        // Shuttle-Teil
        colRoutinePart.setCellValueFactory(new PropertyValueFactory<>("shuttlePart"));

        // Geschätzte Dauer in Minuten
        colRoutineEst.setCellValueFactory(new PropertyValueFactory<>("estimatedMinutes"));

        // Done-Checkbox: beim Abhaken wird completedAt gesetzt
        colRoutineDone.setCellValueFactory(data -> data.getValue().doneProperty());
        colRoutineDone.setCellFactory(col -> {
            CheckBoxTableCell<RoutineTask, Boolean> cell = new CheckBoxTableCell<>();
            return cell;
        });
        // Wenn eine Checkbox-Zelle geklickt wird, completedAt aktualisieren
        colRoutineDone.setEditable(true);
        routineTaskTable.setEditable(true);

        // Listener auf das doneProperty jeder Aufgabe, damit completedAt gesetzt wird
        routineTaskTable.getItems().addListener(
            (javafx.collections.ListChangeListener<RoutineTask>) change -> {
                while (change.next()) {
                    for (RoutineTask task : change.getAddedSubList()) {
                        task.doneProperty().addListener((obs, oldVal, newVal) -> {
                            if (newVal) {
                                // Erledigt: Zeitstempel setzen
                                task.setCompletedAt(LocalDateTime.now().format(FMT));
                            } else {
                                // Rückgängig gemacht: Zeitstempel löschen
                                task.setCompletedAt(null);
                            }
                            // Tabelle neu zeichnen, damit completedAt-Spalte aktualisiert wird
                            routineTaskTable.refresh();
                        });
                    }
                }
            });

        // Erledigungszeitstempel (leer wenn noch nicht erledigt)
        colRoutineTime.setCellValueFactory(new PropertyValueFactory<>("completedAt"));
        colRoutineTime.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String ts, boolean empty) {
                super.updateItem(ts, empty);
                if (empty || ts == null) { setText("–"); setStyle("-fx-text-fill: #666;"); }
                else { setText(ts); setStyle("-fx-text-fill: #66ff66;"); }
            }
        });

        routineTaskTable.setStyle("-fx-background: #2a2a2a; -fx-background-color: #2a2a2a;");
    }
}
