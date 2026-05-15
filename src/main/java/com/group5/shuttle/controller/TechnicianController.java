package com.group5.shuttle.controller;

import com.group5.shuttle.model.SensorRow;
import com.group5.shuttle.model.SensorStatus;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;
import com.group5.shuttle.model.ShuttlePart;
import com.group5.shuttle.service.ISensorDataService;
import com.group5.shuttle.service.SensorDataService;
import com.group5.shuttle.service.TakeoverState;
import com.group5.shuttle.util.ColoredTableCell;
import com.group5.shuttle.util.ShuttleDataHelper;
import com.group5.shuttle.util.StatusColors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Map;

// Controller for the technician panel.
// Displays all current sensor values for all shuttle parts in a table.
// Each sensor is color-coded according to its status.
public class TechnicianController extends BaseController {

    // The back button that leads to the main dashboard.
    @FXML private Button btnBack;

    // The table that displays all sensor values.
    @FXML private TableView<SensorRow> sensorTable;

    // Column for the shuttle part name, e.g. "Orbiter".
    @FXML private TableColumn<SensorRow, String> colPart;

    // Column for the sensor name, e.g. "hullTemperature".
    @FXML private TableColumn<SensorRow, String> colSensor;

    // Column for the current measured value, e.g. "520.00".
    @FXML private TableColumn<SensorRow, String> colValue;

    // Column for the evaluation status: "OK", "WARNING" or "REPLACE".
    @FXML private TableColumn<SensorRow, String> colStatus;

    private final ISensorDataService sensorService = SensorDataService.getInstance();

    // initialize() is called automatically as soon as the FXML file is loaded.
    @FXML
    public void initialize() {
        // Back button navigates to the main dashboard.
        setupBackButton(btnBack);

        // Set up the table and populate it with data.
        setupTable();
        loadSensorData();
    }

    // Sets up the table columns and defines which data is displayed where.
    private void setupTable() {
        // PropertyValueFactory links a column to a getter in SensorRow.
        // "part" means: the column shows the return value of getPart().
        colPart.setCellValueFactory(new PropertyValueFactory<>("part"));
        colSensor.setCellValueFactory(new PropertyValueFactory<>("sensor"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Custom cell rendering for the status column: colored text.
        colStatus.setCellFactory(col -> new ColoredTableCell<>(StatusColors::forSensorStatus));
    }

    // Loads sensor data and thresholds, evaluates each sensor
    // and adds the result as a row in the table.
    private void loadSensorData() {
        ShuttleData data = sensorService.loadSensorData();
        Map<String, Map<String, SensorThreshold>> thresholds = sensorService.loadThresholds();

        // If the data could not be loaded, exit the method.
        if (data == null || thresholds == null) return;

        // ObservableList is a JavaFX list – the table reacts automatically to changes.
        ObservableList<SensorRow> rows = FXCollections.observableArrayList();

        // OCP: Shuttle parts come from data.getAllParts(); new part → only change ShuttleData.
        for (var partEntry : data.getAllParts().entrySet()) {
            String jKey        = partEntry.getKey();
            String displayName = TakeoverState.getDisplayName(jKey);
            ShuttlePart part   = partEntry.getValue();

            // Skip if no sensor data is available.
            if (part == null || part.getSensors() == null) continue;

            // Get the thresholds for this shuttle part.
            Map<String, SensorThreshold> partThresholds = thresholds.get(jKey);

            // Iterate over each individual sensor of this part.
            for (var sensorEntry : part.getSensors().entrySet()) {
                String sensorName = sensorEntry.getKey();   // e.g. "hullTemperature"
                double value      = sensorEntry.getValue(); // e.g. 520.0

                SensorStatus result = ShuttleDataHelper.evaluateSensor(sensorName, value, partThresholds, sensorService);

                // Create a new row with all information and add it to the list.
                rows.add(new SensorRow(displayName, sensorName, String.format("%.2f", value), result.name()));
            }
        }

        // Load all collected rows into the table.
        sensorTable.setItems(rows);
    }

    // Returns the back button – required by BaseController.loadView().
    @Override
    protected Button getNavigationButton() {
        return btnBack;
    }
}
