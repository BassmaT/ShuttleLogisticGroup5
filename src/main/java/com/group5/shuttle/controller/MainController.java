package com.group5.shuttle.controller;

import com.group5.shuttle.model.ShuttleData;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.service.SensorService;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.Map;

public class MainController {

    @FXML private Button btnMission;
    @FXML private Button btnTechnician;
    @FXML private Button btnInventory;
    @FXML private Button btnHistory;

    @FXML private ProgressBar progressTakeover;
    @FXML private Label lblWarning;
    @FXML private Label lblStatus;

    private final SensorService sensorService = new SensorService();

    @FXML
    public void initialize() {

        // Navigation
        btnMission.setOnAction(e -> loadView("mission_control.fxml"));
        btnTechnician.setOnAction(e -> loadView("technician.fxml"));
        btnInventory.setOnAction(e -> loadView("inventory.fxml"));
        btnHistory.setOnAction(e -> loadView("history.fxml"));

        // Sensordaten laden FUNKTIONIERT NOCH NICHT
        updateDashboard();
    }

    private void updateDashboard() {

        ShuttleData data = sensorService.loadSensorData();
        Map<String, Map<String, SensorThreshold>> thresholds = sensorService.loadThresholds();

        if (data == null || thresholds == null) {
            lblWarning.setText("Error loading sensor data");
            lblStatus.setText("System offline");
            return;
        }

        int totalSensors = 0;
        int okSensors = 0;
        String worstWarning = "OK";

        // ORBITER
        for (var entry : data.orbiter.sensors.entrySet()) {
            totalSensors++;
            String sensorName = entry.getKey();
            double value = entry.getValue();

            SensorThreshold t = thresholds.get("orbiter").get(sensorName);
            String result = sensorService.evaluate(value, t);

            if (result.equals("OK")) okSensors++;
            if (result.equals("WARNING") && worstWarning.equals("OK")) worstWarning = "WARNING";
            if (result.equals("REPLACE")) worstWarning = "REPLACE";
        }

        // SRB
        for (var entry : data.srb.sensors.entrySet()) {
            totalSensors++;
            String sensorName = entry.getKey();
            double value = entry.getValue();

            SensorThreshold t = thresholds.get("srb").get(sensorName);
            String result = sensorService.evaluate(value, t);

            if (result.equals("OK")) okSensors++;
            if (result.equals("WARNING") && worstWarning.equals("OK")) worstWarning = "WARNING";
            if (result.equals("REPLACE")) worstWarning = "REPLACE";
        }

        // EXTERNAL TANK
        for (var entry : data.externalTank.sensors.entrySet()) {
            totalSensors++;
            String sensorName = entry.getKey();
            double value = entry.getValue();

            SensorThreshold t = thresholds.get("externalTank").get(sensorName);
            String result = sensorService.evaluate(value, t);

            if (result.equals("OK")) okSensors++;
            if (result.equals("WARNING") && worstWarning.equals("OK")) worstWarning = "WARNING";
            if (result.equals("REPLACE")) worstWarning = "REPLACE";
        }

        // Fortschritt berechnen
        double progress = (double) okSensors / totalSensors;
        progressTakeover.setProgress(progress);

        // Warnungen setzen
        switch (worstWarning) {
            case "OK" -> {
                lblWarning.setText("No warnings");
                lblStatus.setText("Systems nominal");
            }
            case "WARNING" -> {
                lblWarning.setText("⚠️ Minor issues detected");
                lblStatus.setText("Technician check recommended");
            }
            case "REPLACE" -> {
                lblWarning.setText("❌ Critical issue detected");
                lblStatus.setText("Immediate replacement required");
            }
        }
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) btnMission.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
