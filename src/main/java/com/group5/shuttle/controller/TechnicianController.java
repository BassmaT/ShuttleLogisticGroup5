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

// Controller für das Techniker-Panel.
// Zeigt alle aktuellen Sensorwerte aller Shuttle-Teile in einer Tabelle an.
// Jeder Sensor wird farblich nach seinem Status markiert.
public class TechnicianController extends BaseController {

    // Der Zurück-Button, der zum Haupt-Dashboard führt.
    @FXML private Button btnBack;

    // Die Tabelle, die alle Sensorwerte anzeigt.
    @FXML private TableView<SensorRow> sensorTable;

    // Spalte für den Namen des Shuttle-Teils, z. B. "Orbiter".
    @FXML private TableColumn<SensorRow, String> colPart;

    // Spalte für den Sensornamen, z. B. "hullTemperature".
    @FXML private TableColumn<SensorRow, String> colSensor;

    // Spalte für den aktuellen Messwert, z. B. "520.00".
    @FXML private TableColumn<SensorRow, String> colValue;

    // Spalte für den Bewertungsstatus: "OK", "WARNING" oder "REPLACE".
    @FXML private TableColumn<SensorRow, String> colStatus;

    private final ISensorDataService sensorService = SensorDataService.getInstance();

    // initialize() wird automatisch aufgerufen, sobald die FXML-Datei geladen ist.
    @FXML
    public void initialize() {
        // Zurück-Button navigiert zum Haupt-Dashboard.
        setupBackButton(btnBack);

        // Tabelle einrichten und mit Daten befüllen.
        setupTable();
        loadSensorData();
    }

    // Richtet die Tabellenspalten ein und definiert, welche Daten wo angezeigt werden.
    private void setupTable() {
        // PropertyValueFactory verknüpft eine Spalte mit einem Getter in SensorRow.
        // "part" bedeutet: Spalte zeigt den Rückgabewert von getPart() an.
        colPart.setCellValueFactory(new PropertyValueFactory<>("part"));
        colSensor.setCellValueFactory(new PropertyValueFactory<>("sensor"));
        colValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Benutzerdefinierte Zell-Darstellung für die Status-Spalte: farbige Texte.
        colStatus.setCellFactory(col -> new ColoredTableCell<>(StatusColors::forSensorStatus));
    }

    // Lädt die Sensordaten und Grenzwerte, bewertet jeden Sensor
    // und fügt das Ergebnis als Zeile in die Tabelle ein.
    private void loadSensorData() {
        ShuttleData data = sensorService.loadSensorData();
        Map<String, Map<String, SensorThreshold>> thresholds = sensorService.loadThresholds();

        // Wenn die Daten nicht geladen werden konnten, Methode beenden.
        if (data == null || thresholds == null) return;

        // ObservableList ist eine JavaFX-Liste – die Tabelle reagiert automatisch auf Änderungen.
        ObservableList<SensorRow> rows = FXCollections.observableArrayList();

        // OCP: Shuttle-Teile kommen aus data.getAllParts(); neuer Part → nur ShuttleData ändern.
        for (var partEntry : data.getAllParts().entrySet()) {
            String jKey        = partEntry.getKey();
            String displayName = TakeoverState.getDisplayName(jKey);
            ShuttlePart part   = partEntry.getValue();

            // Überspringen, wenn keine Sensordaten vorhanden sind.
            if (part == null || part.getSensors() == null) continue;

            // Grenzwerte für diesen Shuttle-Teil holen.
            Map<String, SensorThreshold> partThresholds = thresholds.get(jKey);

            // Jeden einzelnen Sensor dieses Teils durchgehen.
            for (var sensorEntry : part.getSensors().entrySet()) {
                String sensorName = sensorEntry.getKey();   // z. B. "hullTemperature"
                double value      = sensorEntry.getValue(); // z. B. 520.0

                SensorStatus result = ShuttleDataHelper.evaluateSensor(sensorName, value, partThresholds, sensorService);

                // Neue Zeile mit allen Informationen erstellen und zur Liste hinzufügen.
                rows.add(new SensorRow(displayName, sensorName, String.format("%.2f", value), result.name()));
            }
        }

        // Alle gesammelten Zeilen in die Tabelle laden.
        sensorTable.setItems(rows);
    }

    // Gibt den Zurück-Button zurück – wird von BaseController.loadView() benötigt.
    @Override
    protected Button getNavigationButton() {
        return btnBack;
    }
}
