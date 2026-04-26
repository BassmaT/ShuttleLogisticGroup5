package com.group5.shuttle.controller;

import com.group5.shuttle.model.ScheduleDay;
import com.group5.shuttle.model.ScheduleEntry;
import com.group5.shuttle.model.TakeoverSchedule;
import com.group5.shuttle.service.IEmployeeService;
import com.group5.shuttle.service.IScheduleService;
import com.group5.shuttle.util.StatusColors;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

// Einzelverantwortung: Aufbau und Aktualisierung der Zeitplan-Vorschau im Dashboard.
public class SchedulePanelController {

    private final VBox             container;
    private final IScheduleService scheduleService;
    private final IEmployeeService employeeService;

    public SchedulePanelController(VBox container,
                                    IScheduleService scheduleService,
                                    IEmployeeService employeeService) {
        this.container       = container;
        this.scheduleService = scheduleService;
        this.employeeService = employeeService;
    }

    public void update() {
        if (container == null) return;
        container.getChildren().clear();

        TakeoverSchedule schedule = scheduleService.loadSchedule();
        if (schedule == null || schedule.getDays() == null) return;

        for (ScheduleDay day : schedule.getDays()) {
            Label dayLabel = new Label(day.getLabel());
            dayLabel.setStyle("-fx-text-fill: #66aaff; -fx-font-size: 13px; "
                + "-fx-font-weight: bold; -fx-padding: 6 0 2 0;");
            container.getChildren().add(dayLabel);

            HBox header = new HBox();
            header.setStyle("-fx-background-color: #333333; -fx-padding: 2 4;");
            Label hTime = new Label("Time");     hTime.setPrefWidth(65);
            Label hTask = new Label("Task");     hTask.setPrefWidth(240);
            Label hEmp  = new Label("Employee"); hEmp.setPrefWidth(150);
            for (Label h : new Label[]{hTime, hTask, hEmp})
                h.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px; -fx-font-weight: bold;");
            header.getChildren().addAll(hTime, hTask, hEmp);
            container.getChildren().add(header);

            for (ScheduleEntry entry : day.getEntries()) {
                String bg  = StatusColors.forScheduleCategoryBg(entry.getCategory());
                String fg  = StatusColors.forScheduleCategoryFg(entry.getCategory());
                String emp = resolveEmployeeName(entry.getAssignedEmployeeId());

                HBox row = new HBox();
                row.setStyle("-fx-background-color: " + bg + "; -fx-padding: 3 4;");
                Label lTime = new Label(entry.getTime()); lTime.setPrefWidth(65);
                Label lTask = new Label(entry.getTask()); lTask.setPrefWidth(240);
                Label lEmp  = new Label(emp);             lEmp.setPrefWidth(150);
                lTime.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
                lTask.setStyle("-fx-text-fill: " + fg + "; -fx-font-size: 12px;");
                lEmp.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");
                row.getChildren().addAll(lTime, lTask, lEmp);
                container.getChildren().add(row);
            }
        }
    }

    private String resolveEmployeeName(String empId) {
        if (empId == null) return "–";
        return employeeService.getById(empId)
            .map(e -> e.getName())
            .orElse(empId);
    }
}
