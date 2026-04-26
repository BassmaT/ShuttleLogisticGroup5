package com.group5.shuttle.controller;

import com.group5.shuttle.model.RoutineTask;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Einzel-Verantwortung: konfiguriert die Routineaufgaben-Tabelle (Spalten, Listener, Stile).
// Extrahiert aus MissionControlController und StaffController (war dort duplizierte Logik).
final class RoutineTableHelper {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private RoutineTableHelper() {}

    // Overload ohne Estimated-Minutes-Spalte – Callers müssen kein null übergeben.
    static void setup(TableView<RoutineTask>         table,
                      TableColumn<RoutineTask, String>  colName,
                      TableColumn<RoutineTask, Boolean> colDone,
                      TableColumn<RoutineTask, String>  colTime) {
        setup(table, colName, null, colDone, colTime);
    }

    static void setup(TableView<RoutineTask>         table,
                      TableColumn<RoutineTask, String>  colName,
                      TableColumn<RoutineTask, Integer> colEst,
                      TableColumn<RoutineTask, Boolean> colDone,
                      TableColumn<RoutineTask, String>  colTime) {

        colName.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("name"));
        colName.setCellFactory(col -> new TableCell<>() {
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
                task.doneProperty().addListener((obs, o, n) -> table.refresh());
            }
        });

        if (colEst != null) {
            colEst.setCellValueFactory(
                new javafx.scene.control.cell.PropertyValueFactory<>("estimatedMinutes"));
        }

        colDone.setCellValueFactory(data -> data.getValue().doneProperty());
        colDone.setCellFactory(col -> new CheckBoxTableCell<>());
        colDone.setEditable(true);
        table.setEditable(true);

        table.getItems().addListener(
            (javafx.collections.ListChangeListener<RoutineTask>) change -> {
                while (change.next()) {
                    for (RoutineTask task : change.getAddedSubList()) {
                        task.doneProperty().addListener((obs, oldVal, newVal) -> {
                            task.setCompletedAt(newVal ? LocalDateTime.now().format(FMT) : null);
                            table.refresh();
                        });
                    }
                }
            });

        colTime.setCellValueFactory(
            new javafx.scene.control.cell.PropertyValueFactory<>("completedAt"));
        colTime.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String ts, boolean empty) {
                super.updateItem(ts, empty);
                if (empty || ts == null) { setText("–"); setStyle("-fx-text-fill: #666;"); }
                else { setText(ts); setStyle("-fx-text-fill: #66ff66;"); }
            }
        });

        table.setStyle(com.group5.shuttle.util.Styles.TABLE_DARK);
    }
}
