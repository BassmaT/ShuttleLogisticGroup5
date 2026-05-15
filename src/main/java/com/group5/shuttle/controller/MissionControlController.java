package com.group5.shuttle.controller;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.RepairTask;
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
 * Controller for Mission Control – manages the 2-step workflow:
 * first select a part (Orbiter / SRB / External Tank),
 * then enter role (Technician or Security Chief) and name.
 * Afterwards the workspace with the repair table,
 * inventory status and approval section is shown.
 */
public class MissionControlController extends BaseController {

    // ── Navigation ──────────────────────────────────────────────────────────────

    /** Back button – navigates to the main view (main_view.fxml). */
    @FXML private Button btnBack;

    // ── Step 1: Part selection ──────────────────────────────────────────────────

    /** Selection button for the spacecraft main module (Orbiter). */
    @FXML private Button btnPartOrbiter;

    /** Selection button for the solid rocket boosters (SRB). */
    @FXML private Button btnPartSrb;

    /** Selection button for the external fuel tank (External Tank). */
    @FXML private Button btnPartTank;

    // ── Step 2: Enter role + name ─────────────────────────────────────────────────

    /** Panel for role and name input; hidden initially. */
    @FXML private VBox rolePanel;

    /** Shows which part is currently selected (or an error message when name is empty). */
    @FXML private Label lblSelectedPart;

    /** ComboBox for selecting an employee  */
    @FXML private ComboBox<Employee> cmbEmployee;

    /** Confirm button – registers the selected employee in TakeoverState. */
    @FXML private Button btnConfirm;

    // ── Workspace ───────────────────────────────────────────────────────────────

    /** Main panel of the workspace; shown after login. */
    @FXML private VBox workPanel;

    /** Header of the repair table – shows part name and approval status if applicable. */
    @FXML private Label lblRepairHeader;

    /** Table with all repair tasks for the selected part. */
    @FXML private TableView<RepairTask> repairTable;

    /** Column: name of the affected sensor. */
    @FXML private TableColumn<RepairTask, String>  colSensor;

    /** Column: current fault status of the sensor (e.g. REPLACE / DEGRADED). */
    @FXML private TableColumn<RepairTask, String>  colStatus;

    /** Column: recommended repair action. */
    @FXML private TableColumn<RepairTask, String>  colAction;

    /** Column: parts status with "Order Part" button or status display (IN_STOCK, ORDERED …). */
    @FXML private TableColumn<RepairTask, String>  colParts;

    /** Column: checkbox for marking a completed repair. */
    @FXML private TableColumn<RepairTask, Boolean> colDone;

    /** "Technician Done" button – the technician signals that all repairs are complete. */
    @FXML private Button btnTechDone;

    // ── Routine Tasks ─────────────────────────────────────────────────────────────

    /** Heading of the routine tasks table. */
    @FXML private Label lblRoutineHeader;

    /** Table with routine tasks for the selected part. */
    @FXML private TableView<RoutineTask> routineTable;

    /** Column: task name. */
    @FXML private TableColumn<RoutineTask, String>  colRtName;

    /** Column: estimated duration in minutes. */
    @FXML private TableColumn<RoutineTask, Integer> colRtEst;

    /** Column: done checkbox. */
    @FXML private TableColumn<RoutineTask, Boolean> colRtDone;

    /** Column: completion timestamp. */
    @FXML private TableColumn<RoutineTask, String>  colRtTime;

    // ── Inventory ─────────────────────────────────────────────────────────────

    /** Heading above the inventory display; shows the selected part. */
    @FXML private Label lblInventoryHeader;

    /** VBox container that holds dynamically created inventory labels. */
    @FXML private VBox inventoryStatus;

    // ── Security Chief approval ──────────────────────────────────────────────────

    /** Section for Security Chief approval; only visible when a Chief is logged in. */
    @FXML private VBox approvalSection;

    /** Status information about the approval (e.g. "Waiting for technician" or "Ready for approval"). */
    @FXML private Label lblApprovalInfo;

    /** Approval button – only enabled once the technician has marked work as done. */
    @FXML private Button btnApprove;

    // ── State and services ─────────────────────────────────────────────────────────────────

    private final IWorkerRegistry    workerRegistry   = TakeoverState.getInstance();
    private final IRepairAccess      repairAccess     = TakeoverState.getInstance();
    private final IPartApproval      partApproval     = TakeoverState.getInstance();
    private final IInventoryService  inventoryService = InventoryService.getInstance();
    private final IScheduleService   scheduleService  = ScheduleService.getInstance();

    /** Key of the currently selected part (e.g. "orbiter", "srb", "externalTank"). */
    private String selectedPartKey = null;
    // OCP: new part → add an entry here; highlightPartButton() stays unchanged.
    private Map<String, Button> partButtons;
    private InventoryStatusPanelController inventoryPanel;

    // ── Initialisation ──────────────────────────────────────────────────────────

    /**
     * Called automatically after the FXML file has been loaded.
     * All buttons are wired to actions and panels are hidden initially.
     */
    @FXML
    public void initialize() {
        // Wire part-selection buttons to the selectPart() method
        btnPartOrbiter.setOnAction(e -> selectPart("orbiter"));
        btnPartSrb.setOnAction(e -> selectPart("srb"));
        btnPartTank.setOnAction(e -> selectPart("externalTank"));
        partButtons = Map.of("orbiter", btnPartOrbiter, "srb", btnPartSrb, "externalTank", btnPartTank);

        // Populate the ComboBox with all employees (technicians and security chiefs)
        EmployeeComboHelper.setup(cmbEmployee);

        // Wire the confirm button to confirmEntry()
        btnConfirm.setOnAction(e -> confirmEntry());

        // Set up the repair table with all columns and cell factories
        setupRepairTable();

        // Set up the routine tasks table
        setupRoutineTable();
        inventoryPanel = new InventoryStatusPanelController(inventoryStatus, lblInventoryHeader, inventoryService);

        // "Technician Done" button: technician reports all repairs as complete
        btnTechDone.setOnAction(e -> {
            if (selectedPartKey != null) {
                // Load the technician's name from state (fallback: "Technician")
                String name = workerRegistry.getTechnicianName(selectedPartKey);
                repairAccess.markTechnicianDone(selectedPartKey, name != null ? name : "Technician");
                // Log completed repairs in the maintenance history
                MaintenanceHistoryService.logRepairs(selectedPartKey, repairAccess, workerRegistry);
                // Refresh the workspace
                refreshWorkPanel();
            }
        });

        // Approval button: Security Chief grants final approval
        btnApprove.setOnAction(e -> {
            if (selectedPartKey != null && partApproval.canApprove(selectedPartKey)) {
                // Load the Security Chief's name from state (fallback: "Security Chief")
                String name = workerRegistry.getSecurityChiefName(selectedPartKey);
                partApproval.approve(selectedPartKey, name != null ? name : "Security Chief");
                // Refresh the workspace and highlight the selected button in blue
                refreshWorkPanel();
                highlightPartButton();
            }
        });

        // Hide the role panel and workspace at startup
        rolePanel.setVisible(false);
        rolePanel.setManaged(false);
        workPanel.setVisible(false);
        workPanel.setManaged(false);

        // Back button: navigate to the main view and refresh the dashboard
        btnBack.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml"));
                Parent root = loader.load();
                MainController mc = loader.getController();
                mc.updateDashboard();
                Stage stage = (Stage) btnBack.getScene().getWindow();
                stage.getScene().setRoot(root);
            } catch (Exception ex) {
                Dialogs.showError("Navigation Error", "View could not be loaded", ex.getMessage());
            }
        });
    }

    // ── Part selection ─────────────────────────────────────────────────────────────────────

    /**
     * Called when the user clicks a part-selection button.
     * Stores the chosen part key, pre-fills the name field with the
     * already registered employee, and shows the panels.
     *
     * @param partKey internal key of the part ("orbiter", "srb", "externalTank")
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
     * Highlights the currently selected part button in blue and
     * resets all other buttons to their default style.
     */
    private void highlightPartButton() {
        partButtons.forEach((key, btn) ->
            btn.setStyle(key.equals(selectedPartKey) ? Styles.PART_BTN_SELECTED : Styles.PART_BTN_NORMAL));
    }

    /**
     * Confirms the name and role input and registers the employee
     * in TakeoverState. Shows an error message if the name field is empty.
     */
    private void confirmEntry() {
        // No action possible without a selected part
        if (selectedPartKey == null) return;

        // Read employee from the ComboBox – must be selected
        Employee emp = cmbEmployee.getValue();
        if (emp == null) {
            // Show error message
            lblSelectedPart.setText("Please select an employee!");
            lblSelectedPart.setStyle(Styles.label13("#ff4444"));
            return;
        }

        // Read name and role directly from the Employee object
        String name = emp.getName();
        String role = emp.getRole();

        // Register employee with name and role in the central state
        workerRegistry.registerWorker(selectedPartKey, name, role);

        // Reset label back to the normal part name (grey, no error message)
        lblSelectedPart.setText("Part: " + TakeoverState.getDisplayName(selectedPartKey));
        lblSelectedPart.setStyle(Styles.label13("#aaaaaa"));

        // Show the workspace after successful registration
        showWorkPanel();
    }

    // ── Table setup ─────────────────────────────────────────────────────────────────────

    /**
     * Sets up all table columns:
     * – Status column with colour-coded display (REPLACE = red, otherwise yellow),
     * – Parts column with "Order Part" button or status display,
     * – Done checkbox, disabled as long as the required part is not in stock.
     */
    private void setupRepairTable() {
        // Mark the table as editable (for the done checkboxes)
        repairTable.setEditable(true);

        // Bind sensor name and action directly from the data model
        colSensor.setCellValueFactory(new PropertyValueFactory<>("sensorName"));
        colAction.setCellValueFactory(new PropertyValueFactory<>("action"));

        // Status column – colour-coded text display based on status
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new SensorStatusCell());

        // Parts column – shows either an "Order Part" button or a status display
        colParts.setCellValueFactory(c -> c.getValue().partStatusProperty());
        colParts.setCellFactory(col -> new PartStatusCell(this::placeOrder));

        // Done column – checkbox for ticking off a repair,
        // disabled when the required part is not yet available
        colDone.setCellValueFactory(c -> c.getValue().doneProperty());
        colDone.setEditable(true);
        colDone.setCellFactory(col -> new TableCell<>() {
            private final CheckBox cb = new CheckBox();
            {
                cb.setOnAction(e -> {
                    RepairTask task = getTableRow() != null ? getTableRow().getItem() : null;
                    if (task == null) return;
                    // Update the completion status of the task
                    task.setDone(cb.isSelected());
                    // When the checkbox is ticked, deduct the required part from inventory
                    if (cb.isSelected()) deductInventory(task);
                    // Re-evaluate the approval button (enable/disable)
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
                // Reflect the current completion status in the checkbox
                cb.setSelected(task.isDone());
                // Disable the checkbox as long as the required part is not available
                cb.setDisable(!task.isPartAvailable());
                // Render the row semi-transparent when the part is not yet available
                setStyle(task.isPartAvailable() ? "" : "-fx-opacity: 0.4;");
                setGraphic(cb);
            }
        });
    }

    // ── Inventory and order logic ──────────────────────────────────────────────────────────

    /**
     * Initialises the parts status (partStatus / partAvailable) for every task
     * of the currently selected part.
     * Tasks that already have the status ORDERED or ARRIVED are skipped
     * so that a running timer is not overwritten.
     */
    private void initTaskPartStatus() {
        if (selectedPartKey == null) return;
        RepairInventoryService.initTaskStatuses(repairAccess.getRepairs(selectedPartKey), inventoryService);
        repairTable.refresh();
    }

    /**
     * Called when the technician clicks "Order Part".
     * Sets the status to ORDERED immediately, shows a confirmation message,
     * and automatically books the part into the warehouse after 10 seconds.
     *
     * @param task the repair task for which the part should be ordered
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
     * Deducts the part required for a repair task from inventory
     * as soon as the task is marked as done.
     *
     * @param task the completed repair task
     */
    private void deductInventory(RepairTask task) {
        RepairInventoryService.deductInventory(task, inventoryService, this::refreshInventory);
    }

    // ── Workspace ────────────────────────────────────────────────────────────────────────

    /**
     * Shows the workspace and refreshes all sub-components.
     */
    private void showWorkPanel() {
        workPanel.setVisible(true);
        workPanel.setManaged(true);
        refreshWorkPanel();
    }

    /**
     * Refreshes the entire workspace for the currently selected part:
     * – header with part name, employee names and approval status,
     * – repair table with current parts status,
     * – visibility of the "Technician Done" button,
     * – inventory display,
     * – approval section for the Security Chief.
     */
    private void refreshWorkPanel() {
        if (selectedPartKey == null) return;

        // Retrieve the current state for the selected part
        boolean approved   = partApproval.isPartApproved(selectedPartKey);
        boolean techDone   = repairAccess.isTechnicianDone(selectedPartKey);
        String displayName = TakeoverState.getDisplayName(selectedPartKey);
        String techName    = workerRegistry.getTechnicianName(selectedPartKey);
        String chiefName   = workerRegistry.getSecurityChiefName(selectedPartKey);

        // Colour and label the header based on approval status
        if (approved) {
            // Part fully approved – green success display
            lblRepairHeader.setText(displayName + " – APPROVED ✓");
            lblRepairHeader.setStyle("-fx-text-fill: #66ff66; -fx-font-size: 15px; -fx-font-weight: bold;");
        } else {
            // Not yet approved – show names of the logged-in employees
            String worker = "";
            if (techName != null) worker += techName + " (Technician)";
            if (chiefName != null) { if (!worker.isEmpty()) worker += " / "; worker += chiefName + " (Security Chief)"; }
            lblRepairHeader.setText("Repair Tasks – " + displayName
                    + (worker.isEmpty() ? "" : "\n" + worker));
            lblRepairHeader.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        }

        // Display repair tasks in the table
        List<RepairTask> tasks = repairAccess.getRepairs(selectedPartKey);
        repairTable.setItems(FXCollections.observableArrayList(tasks));

        // Load and display routine tasks for this part
        routineTable.setItems(FXCollections.observableArrayList(
            RoutineTaskStore.getInstance().getTasksForPart(displayName)));

        // Initialise parts status for each task (ORDERED/ARRIVED states are preserved)
        initTaskPartStatus();

        // Show "Technician Done" button only when: technician logged in, not yet done, not approved
        boolean isTechnician = techName != null && chiefName == null;
        boolean showTechDone = isTechnician && !techDone && !approved;
        btnTechDone.setVisible(showTechDone);
        btnTechDone.setManaged(showTechDone);

        // Refresh the inventory display for the selected part
        refreshInventory();

        // Show the approval section only when a Security Chief is logged in
        boolean isSecurityChief = chiefName != null;
        approvalSection.setVisible(isSecurityChief);
        approvalSection.setManaged(isSecurityChief);
        if (isSecurityChief) refreshApproveState();
    }

    /**
     * Displays the inventory status of all required parts for the currently selected part.
     * Each part is shown as a colour-coded label:
     * red = out of stock, yellow = low, green = sufficient.
     */
   private void refreshInventory() {
    if (selectedPartKey == null) return;
    inventoryPanel.update(TakeoverState.getDisplayName(selectedPartKey));
}


    /**
     * Updates the approval button and the Security Chief's information label.
     * – Already approved: button disabled, green text.
     * – Technician done, approval possible: button enabled.
     * – Technician not yet done: button disabled, yellow waiting info.
     */
    private void refreshApproveState() {
        if (selectedPartKey == null) return;

        // Retrieve current approval and completion status
        boolean approved   = partApproval.isPartApproved(selectedPartKey);
        boolean canApprove = partApproval.canApprove(selectedPartKey);
        boolean techDone   = repairAccess.isTechnicianDone(selectedPartKey);

        if (approved) {
            // Part already approved – disable button and show success message
            lblApprovalInfo.setText("This part has been approved.");
            lblApprovalInfo.setStyle("-fx-text-fill: #66ff66;");
            btnApprove.setDisable(true);
            btnApprove.setText("Already Approved ✓");
        } else if (canApprove) {
            // Technician is done – Security Chief approval can be granted
            lblApprovalInfo.setText("Technician marked done. Ready for approval.");
            lblApprovalInfo.setStyle("-fx-text-fill: #66ff66;");
            btnApprove.setDisable(false);
            btnApprove.setText("Give Security Chief OK");
        } else if (!techDone) {
            // Technician not yet done – approval button locked, yellow waiting info
            lblApprovalInfo.setText("Waiting for Technician to finish repairs.");
            lblApprovalInfo.setStyle("-fx-text-fill: #ffcc00;");
            btnApprove.setDisable(true);
            btnApprove.setText("Give Security Chief OK");
        }
    }

    // ── Helper methods ─────────────────────────────────────────────────────────────────────

    /**
     * Sets up the routine tasks table:
     * task name, duration, done checkbox (sets completedAt) and timestamp.
     */
    private void setupRoutineTable() {
        RoutineTableHelper.setup(routineTable, colRtName, colRtEst, colRtDone, colRtTime);
    }

    /**
     * Returns the list of employees scheduled for this part according to the schedule.
     * Fallback: all employees if no schedule can be loaded.
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

    /** Returns the back button used for navigation in the base class. */
    @Override
    protected Button getNavigationButton() { return btnBack; }
}
