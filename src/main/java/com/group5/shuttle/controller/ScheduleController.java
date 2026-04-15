package com.group5.shuttle.controller;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.ScheduleDay;
import com.group5.shuttle.model.ScheduleEntry;
import com.group5.shuttle.model.TakeoverSchedule;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.ScheduleService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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
import javafx.util.StringConverter;

import java.util.List;

// Controller für den interaktiven 3-Tage-Zeitplan.
// Zeigt alle Schedule-Einträge tagesweise in editierbaren Tabellen an.
// Mitarbeiter können per ComboBox umgeplant werden (z. B. bei Krankheit).
public class ScheduleController extends BaseController {

    // Hauptcontainer: wird dynamisch mit einer Tabelle pro Tag befüllt
    @FXML private VBox scheduleContent;

    // Zurück-Button zum Haupt-Dashboard
    @FXML private Button btnBack;

    private final ScheduleService sensorService = ScheduleService.getInstance();

    // Wird automatisch beim Laden der FXML aufgerufen
    @FXML
    public void initialize() {
        btnBack.setOnAction(e -> loadView("main_view.fxml"));
        buildSchedule();
    }

    // Lädt den Schedule und baut für jeden Tag eine eigene Tabelle auf
    private void buildSchedule() {
        TakeoverSchedule schedule = sensorService.loadSchedule();
        if (schedule == null || schedule.getDays() == null) {
            Label err = new Label("Schedule could not be loaded.");
            err.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 13px;");
            scheduleContent.getChildren().add(err);
            return;
        }

        for (ScheduleDay day : schedule.getDays()) {
            // Tagesüberschrift
            Label dayLabel = new Label(day.getLabel());
            dayLabel.setStyle(
                "-fx-text-fill: #66aaff; -fx-font-size: 15px; -fx-font-weight: bold; -fx-padding: 0 0 4 0;");

            // Tabelle für diesen Tag erstellen
            TableView<ScheduleEntry> table = buildDayTable(day.getEntries());

            // Wrapper-VBox pro Tag
            VBox dayBox = new VBox(4, dayLabel, table);
            dayBox.setStyle("-fx-background-color: #2a2a2a; -fx-padding: 10; -fx-background-radius: 6;");
            scheduleContent.getChildren().add(dayBox);
        }
    }

    // Erstellt eine TableView für einen einzelnen Tag mit allen Spalten
    private TableView<ScheduleEntry> buildDayTable(List<ScheduleEntry> entries) {
        TableView<ScheduleEntry> table = new TableView<>();
        table.setStyle("-fx-background: #2a2a2a; -fx-background-color: #2a2a2a;");
        table.setPrefHeight(entries.size() * 36.0 + 30);
        table.setEditable(true);

        ObservableList<ScheduleEntry> data = FXCollections.observableArrayList(entries);
        table.setItems(data);

        // Spalte: Zeit
        TableColumn<ScheduleEntry, String> colTime = new TableColumn<>("Time");
        colTime.setPrefWidth(65);
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        styleTextCol(colTime, "#cccccc");

        // Spalte: Aufgabe
        TableColumn<ScheduleEntry, String> colTask = new TableColumn<>("Task");
        colTask.setPrefWidth(230);
        colTask.setCellValueFactory(new PropertyValueFactory<>("task"));
        // Farbe je nach Kategorie
        colTask.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle(""); return;
                }
                String cat = getTableRow().getItem().getCategory();
                String color = switch (cat != null ? cat : "") {
                    case "repair"     -> "#ff6666";
                    case "approval"   -> "#66ff66";
                    case "routine"    -> "#ffcc00";
                    case "diagnostic" -> "#66aaff";
                    default           -> "#aaaaaa";
                };
                setText(task);
                setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
            }
        });

        // Spalte: Kategorie
        TableColumn<ScheduleEntry, String> colCat = new TableColumn<>("Category");
        colCat.setPrefWidth(90);
        colCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        styleTextCol(colCat, "#888888");

        // Spalte: Shuttle-Teil
        TableColumn<ScheduleEntry, String> colPart = new TableColumn<>("Part");
        colPart.setPrefWidth(110);
        colPart.setCellValueFactory(new PropertyValueFactory<>("shuttlePart"));
        styleTextCol(colPart, "#aaaaaa");

        // Spalte: Zugewiesener Mitarbeiter (ComboBox – editierbar)
        TableColumn<ScheduleEntry, String> colEmp = new TableColumn<>("Assigned Employee");
        colEmp.setPrefWidth(190);
        // Zeigt den Namen des aktuell zugewiesenen Mitarbeiters
        colEmp.setCellValueFactory(cellData -> {
            String empId = cellData.getValue().getAssignedEmployeeId();
            if (empId == null) return new SimpleStringProperty("–");
            Employee emp = EmployeeService.getInstance().getById(empId);
            return new SimpleStringProperty(emp != null ? emp.getName() : empId);
        });
        // ComboBox-Zelle: ermöglicht Umplanung per Dropdown
        colEmp.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<Employee> combo = new ComboBox<>();
            {
                // Alle Mitarbeiter zur Auswahl anbieten
                combo.setItems(FXCollections.observableArrayList(
                    EmployeeService.getInstance().getAllEmployees()));
                // Anzeige: "Name (Rolle)"
                combo.setConverter(new StringConverter<>() {
                    @Override public String toString(Employee e) {
                        return e == null ? "–" : e.getName() + " (" + e.getRole() + ")";
                    }
                    @Override public Employee fromString(String s) { return null; }
                });
                combo.setStyle("-fx-font-size: 12px;");
                // Bei Auswahl: assignedEmployeeId im Eintrag aktualisieren
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
                // Aktuell zugewiesenen Mitarbeiter vorauswählen
                String empId = getTableRow().getItem().getAssignedEmployeeId();
                if (empId != null) {
                    Employee current = EmployeeService.getInstance().getById(empId);
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

    // Hilfsmethode: setzt Textfarbe für einfache Text-Spalten
    private void styleTextCol(TableColumn<ScheduleEntry, String> col, String color) {
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(val);
                setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
            }
        });
    }

    // Gibt den Zurück-Button zurück – wird von BaseController.loadView() benötigt
    @Override
    protected Button getNavigationButton() { return btnBack; }
}
