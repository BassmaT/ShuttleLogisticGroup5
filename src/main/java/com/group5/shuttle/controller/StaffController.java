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

// Controller for the Staff overview.
// Left: list of all employees.
// Right: repair and routine tasks for the selected employee.
public class StaffController extends BaseController {

    // --- FXML bindings ---

    // List of all employees in the left sidebar
    @FXML private ListView<Employee> employeeList;

    // Displays name, role and team of the selected employee
    @FXML private Label lblEmployeeName;
    @FXML private Label lblEmployeeRole;
    @FXML private Label lblEmployeeTeam;

    // Table of repair tasks
    @FXML private TableView<RepairTask> repairTaskTable;
    @FXML private TableColumn<RepairTask, String> colRepairPart;
    @FXML private TableColumn<RepairTask, String> colRepairSensor;
    @FXML private TableColumn<RepairTask, String> colRepairAction;
    @FXML private TableColumn<RepairTask, String> colRepairStatus;

    // Table of routine tasks
    @FXML private TableView<RoutineTask> routineTaskTable;
    @FXML private TableColumn<RoutineTask, String>  colRoutineName;
    @FXML private TableColumn<RoutineTask, String>  colRoutinePart;
    @FXML private TableColumn<RoutineTask, Integer> colRoutineEst;
    @FXML private TableColumn<RoutineTask, Boolean> colRoutineDone;
    @FXML private TableColumn<RoutineTask, String>  colRoutineTime;

    // Back-to-dashboard button (linked to navigation in BaseController)
    @FXML private javafx.scene.control.Button btnBack;

    private final IWorkerRegistry workerRegistry = TakeoverState.getInstance();
    private final IRepairAccess   repairAccess   = TakeoverState.getInstance();

    // BaseController needs this button to retrieve the window (Stage)
    @Override
    protected javafx.scene.control.Button getNavigationButton() { return btnBack; }

    // Called when the FXML is loaded – initializes all tables and the employee list
    @FXML
    public void initialize() {
        setupRepairTable();     // Configure columns of the repair table
        setupRoutineTable();    // Configure columns of the routine table
        loadEmployees();        // Populate the employee list

        // When an employee is selected in the list, show their details on the right
        employeeList.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> {
                if (newVal != null) showEmployee(newVal);
            });

        // Link the back button
        setupBackButton(btnBack);
    }

    // Populates the ListView with all employees from EmployeeService
    private void loadEmployees() {
        List<Employee> employees = EmployeeService.getInstance().getAllEmployees();
        employeeList.setItems(FXCollections.observableArrayList(employees));

        // Custom cell content: "Name (Role) – Team"
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

    // Displays name, role, team and tasks of the selected employee
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

        // Routine tasks: all tasks assigned to this employee
        List<RoutineTask> assignedRoutine =
            RoutineTaskStore.getInstance().getTasksForEmployee(emp.getId());
        routineTaskTable.setItems(FXCollections.observableArrayList(assignedRoutine));
    }

    // Configures the columns of the repair table
    private void setupRepairTable() {
        // Part column: shows the display name of the shuttle part
        colRepairPart.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                TakeoverState.getDisplayName(data.getValue().getPartKey())));

        // Sensor column
        colRepairSensor.setCellValueFactory(new PropertyValueFactory<>("sensorName"));

        // Action column
        colRepairAction.setCellValueFactory(new PropertyValueFactory<>("action"));

        // Status column with color coding (yellow = WARNING, red = REPLACE, green = done)
        colRepairStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colRepairStatus.setCellFactory(col -> new ColoredTableCell<>(StatusColors::forSensorStatus));

        // Set background color of the table and text color for all cells
        repairTaskTable.setStyle(Styles.TABLE_DARK);
    }

    private void setupRoutineTable() {
        RoutineTableHelper.setup(routineTaskTable, colRoutineName, colRoutineEst, colRoutineDone, colRoutineTime);
        colRoutinePart.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("shuttlePart"));
    }
}
