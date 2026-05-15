package com.group5.shuttle.controller;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.ScheduleDay;
import com.group5.shuttle.model.ScheduleEntry;
import com.group5.shuttle.model.TakeoverSchedule;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.IScheduleService;
import com.group5.shuttle.service.ScheduleService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import com.group5.shuttle.util.EmployeeComboHelper;
import com.group5.shuttle.util.StatusColors;
import com.group5.shuttle.util.Styles;



// Controller for the interactive 3-day schedule.
// Displays all schedule entries by day in editable tables.
// Employees can be reassigned via ComboBox (e.g. in case of illness).
public class ScheduleController extends BaseController {

    // Main container: dynamically populated with one table per day
    @FXML private VBox scheduleContent;

    // Back button to the main dashboard
    @FXML private Button btnBack;

    private final IScheduleService scheduleService = ScheduleService.getInstance();
    private static ScheduleController instance;

    // Called automatically when the FXML is loaded
    @FXML
    public void initialize() {
        setupBackButton(btnBack);
        buildSchedule();
        instance = this;
        System.out.println("ScheduleController initialized: " + this.hashCode());
    }

    public static ScheduleController getInstance() {
        return instance;
    }

    // Loads the schedule and builds a separate table for each day
    private void buildSchedule() {
        TakeoverSchedule schedule = ScheduleService.getInstance().loadSchedule();
        if (schedule == null || schedule.getDays() == null) {
            Label err = new Label("Schedule could not be loaded.");
            err.setStyle(Styles.label13("#ff4444"));
            scheduleContent.getChildren().add(err);
            return;
        }

        for (ScheduleDay day : schedule.getDays()) {
            // Day heading
            Label dayLabel = new Label(day.getLabel());
            dayLabel.setStyle(
                "-fx-text-fill: #66aaff; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 0 0 4 0;");

            // Create table for this day
            TableView<ScheduleEntry> table = buildDayTable(day.getEntries());

            // Wrapper VBox per day
            VBox dayBox = new VBox(4, dayLabel, table);
            dayBox.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-background-radius: 6;");
            scheduleContent.getChildren().add(dayBox);
        }
    }

    // Creates a TableView for a single day with all columns
    private TableView<ScheduleEntry> buildDayTable(ObservableList<ScheduleEntry> entries) {
        TableView<ScheduleEntry> table = new TableView<>();
        table.setStyle(Styles.TABLE_DARK);
        table.setEditable(true);

        table.setItems(entries);

        table.setFixedCellSize(35);
        table.prefHeightProperty().bind(
                javafx.beans.binding.Bindings.size(table.getItems())
                        .multiply(table.getFixedCellSize())
                        .add(30)
        );

        // Column: Time
        TableColumn<ScheduleEntry, String> colTime = new TableColumn<>("Time");
        colTime.setPrefWidth(65);
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        styleTextCol(colTime, "#cccccc");

        // Column: Task
        TableColumn<ScheduleEntry, String> colTask = new TableColumn<>("Task");
        colTask.setPrefWidth(230);
        colTask.setCellValueFactory(new PropertyValueFactory<>("task"));
        // Color by category
        colTask.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle(""); return;
                }
                String cat = getTableRow().getItem().getCategory();
                String color = StatusColors.forScheduleCategoryFg(cat);
                setText(task);
                setStyle(Styles.label12(color));
            }
        });

        // Column: Category
        TableColumn<ScheduleEntry, String> colCat = new TableColumn<>("Category");
        colCat.setPrefWidth(90);
        colCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        styleTextCol(colCat, "#888888");

        // Column: Shuttle part
        TableColumn<ScheduleEntry, String> colPart = new TableColumn<>("Part");
        colPart.setPrefWidth(110);
        colPart.setCellValueFactory(new PropertyValueFactory<>("shuttlePart"));
        styleTextCol(colPart, "#aaaaaa");

        // Column: Assigned employee (ComboBox – editable)
        TableColumn<ScheduleEntry, String> colEmp = new TableColumn<>("Assigned Employee");
        colEmp.setPrefWidth(190);
        // Shows the name of the currently assigned employee
        colEmp.setCellValueFactory(cellData -> {
            String empId = cellData.getValue().getAssignedEmployeeId();
            if (empId == null) return new SimpleStringProperty("–");
            Employee emp = EmployeeService.getInstance().getById(empId).orElse(null);
            return new SimpleStringProperty(emp != null ? emp.getName() : empId);
        });
        // ComboBox cell: allows reassignment via dropdown
        colEmp.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<Employee> combo = new ComboBox<>();
            {
                // Offer all employees for selection
                EmployeeComboHelper.setup(combo);
                combo.setStyle("-fx-font-size: 12px;");
                // On selection: update assignedEmployeeId in the entry
                combo.setOnAction(e -> {
                    ScheduleEntry entry = getTableRow() != null ? getTableRow().getItem() : null;
                    Employee selected = combo.getValue();
                    if (entry != null && selected != null) {
                        entry.setAssignedEmployeeId(selected.getId());
                        getTableView().refresh();
                    }
                });
            }
            @Override
            protected void updateItem(String empName, boolean empty) {
                super.updateItem(empName, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null); return;
                }
                // Pre-select the currently assigned employee
                String empId = getTableRow().getItem().getAssignedEmployeeId();
                if (empId != null) {
                    Employee current = EmployeeService.getInstance().getById(empId).orElse(null);
                    combo.getSelectionModel().select(current);
                } else {
                    combo.getSelectionModel().clearSelection();
                }
                setGraphic(combo);
            }
        });

        table.getColumns().addAll(colTime, colTask, colCat, colPart, colEmp);
        return table;
    }

    // Helper method: sets text color for simple text columns
    private void styleTextCol(TableColumn<ScheduleEntry, String> col, String color) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                setStyle(Styles.label12(color));
            }
        });
    }

    public void refreshSchedule() {
        for (javafx.scene.Node node : scheduleContent.getChildren()) {
            if (node instanceof VBox dayBox) {
                for (javafx.scene.Node subNode : dayBox.getChildren()) {
                    if (subNode instanceof TableView<?> table) {
                        // Trick: temporarily detach and reattach the list to force a refresh
                        var items = table.getItems();
                        table.setItems(null);
                        table.layout();
                        table.setItems((ObservableList) items);

                        table.refresh();
                    }
                }
            }
        }
        scheduleContent.requestLayout();
    }


    // Returns the back button – required by BaseController.loadView()
    @Override
    protected Button getNavigationButton() { return btnBack; }
}
