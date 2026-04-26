package com.group5.shuttle.controller;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.RoutineTask;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.IRepairAccess;
import com.group5.shuttle.service.IWorkerRegistry;
import com.group5.shuttle.service.RoutineTaskStore;
import com.group5.shuttle.service.TakeoverState;
import com.group5.shuttle.util.ColoredTableCell;
import com.group5.shuttle.util.StatusColors;
import com.group5.shuttle.util.Styles;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.ArrayList;
import java.util.List;

// Controller für die Staff-Übersicht.
// Links: Liste aller Mitarbeiter.
// Rechts: Reparatur- und Routineaufgaben des ausgewählten Mitarbeiters.
public class StaffController extends BaseController {

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

    private final IWorkerRegistry workerRegistry = TakeoverState.getInstance();
    private final IRepairAccess   repairAccess   = TakeoverState.getInstance();

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
        setupBackButton(btnBack);
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
        lblEmployeeName.setText(emp.getName());
        lblEmployeeRole.setText(emp.getRole());
        lblEmployeeTeam.setText(emp.getTeam());

        List<RepairTask> assignedRepairs = new ArrayList<>();
        for (String partKey : TakeoverState.PART_KEYS) {
            String techName = workerRegistry.getTechnicianName(partKey);
            if (emp.getName().equals(techName)) {
                assignedRepairs.addAll(repairAccess.getRepairs(partKey));
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
        repairTaskTable.setStyle(Styles.TABLE_DARK);
    }

    private void setupRoutineTable() {
        RoutineTableHelper.setup(routineTaskTable, colRoutineName, colRoutineEst, colRoutineDone, colRoutineTime);
        colRoutinePart.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("shuttlePart"));
    }
}
