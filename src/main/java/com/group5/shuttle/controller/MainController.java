package com.group5.shuttle.controller;

import com.group5.shuttle.model.ShuttleData;
import com.group5.shuttle.model.ShuttlePart;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.service.SensorService;
import com.group5.shuttle.service.TakeoverState;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

// Controller für das Haupt-Dashboard (main_view.fxml).
// Zeigt den Übernahme-Fortschritt, aktive Arbeiter, Sensorwarnungen und Sensor-Details.
// Diese Klasse wird als erstes geladen, wenn die App startet.
public class MainController {

    // Navigations-Buttons in der linken Seitenleiste.
    @FXML private Button btnMission;    // öffnet Mission Control
    @FXML private Button btnTechnician; // öffnet das Techniker-Panel
    @FXML private Button btnInventory;  // öffnet die Lagerübersicht
    @FXML private Button btnHistory;    // öffnet die Wartungshistorie

    // Button, der nach vollständiger Übernahme (100%) erscheint.
    @FXML private Button btnFinish;

    // Fortschrittsbalken – zeigt 0% bis 100% Übernahme-Fortschritt.
    @FXML private ProgressBar progressTakeover;

    // Zeigt die aktuelle Warnstufe an, z. B. "Critical issue detected".
    @FXML private Label lblWarning;

    // Zeigt den allgemeinen Systemstatus an, z. B. "Systems nominal".
    @FXML private Label lblStatus;

    // Zeigt den Freigabe-Status aller drei Teile an, z. B. "Orbiter ✓  SRB ✗  External Tank ✗".
    @FXML private Label lblApprovalStatus;

    // Container, der dynamisch befüllt wird mit den aktiven Arbeitern pro Shuttle-Teil.
    @FXML private VBox sensorContainer;

    // Container für die "Aktive Arbeit"-Sektion – wer arbeitet gerade wo.
    @FXML private VBox activeWorkContainer;

    // Zeigt die zuletzt durchgeführte Aktion an, z. B. "J. Miller completed repairs on Orbiter".
    @FXML private Label lblLastActivity;

    // SensorService lädt die Sensordaten und Grenzwerte aus den JSON-Dateien.
    private final SensorService sensorService = new SensorService();

    // TakeoverState ist der zentrale Zustandsspeicher der Übernahme – Singleton.
    private final TakeoverState takeoverState = TakeoverState.getInstance();

    // initialize() wird automatisch aufgerufen, wenn das FXML geladen ist.
    @FXML
    public void initialize() {
        // Navigations-Buttons mit Aktionen verknüpfen – jeder lädt eine andere FXML-Seite.
        btnMission.setOnAction(e -> loadView("mission_control.fxml"));
        btnTechnician.setOnAction(e -> loadView("technician.fxml"));
        btnInventory.setOnAction(e -> loadView("inventory.fxml"));
        btnHistory.setOnAction(e -> loadView("history.fxml"));

        // "Takeover Complete"-Button: setzt alles zurück und generiert neue Reparaturen.
        btnFinish.setOnAction(e -> {
            takeoverState.reset(); // alle Daten der Übernahme zurücksetzen

            // Nach dem Reset die Reparaturaufgaben neu berechnen.
            ShuttleData data = sensorService.loadSensorData();
            Map<String, Map<String, SensorThreshold>> thresholds = sensorService.loadThresholds();
            takeoverState.generateRepairs(data, thresholds, sensorService);

            updateDashboard(); // Dashboard neu laden
        });

        // Beim ersten Start: Reparaturaufgaben aus den aktuellen Sensordaten berechnen.
        ShuttleData data = sensorService.loadSensorData();
        Map<String, Map<String, SensorThreshold>> thresholds = sensorService.loadThresholds();
        if (data != null && thresholds != null) {
            takeoverState.generateRepairs(data, thresholds, sensorService);
        }

        // Dashboard mit aktuellen Werten befüllen.
        updateDashboard();
    }

    // Aktualisiert alle sichtbaren Informationen auf dem Dashboard.
    // Wird auch von anderen Controllern aufgerufen (z. B. nach dem Zurück-Navigieren).
    public void updateDashboard() {
        ShuttleData data = sensorService.loadSensorData();
        Map<String, Map<String, SensorThreshold>> thresholds = sensorService.loadThresholds();

        // Übernahme-Fortschritt berechnen und im Balken anzeigen (0.0 bis 1.0).
        double progress = takeoverState.getProgress();
        progressTakeover.setProgress(progress);

        // Tooltip am Fortschrittsbalken anzeigen – zeigt die Prozentzahl beim Hovern.
        int pct = (int) Math.round(progress * 100);
        progressTakeover.setTooltip(new Tooltip(pct + "% approved"));

        // Freigabe-Übersicht: ✓ = freigegeben, ✗ = noch nicht freigegeben.
        String orbiterStatus = takeoverState.isPartApproved("orbiter")      ? "Orbiter ✓"       : "Orbiter ✗";
        String srbStatus     = takeoverState.isPartApproved("srb")           ? "SRB ✓"            : "SRB ✗";
        String tankStatus    = takeoverState.isPartApproved("externalTank")  ? "External Tank ✓"  : "External Tank ✗";
        lblApprovalStatus.setText(orbiterStatus + "   " + srbStatus + "   " + tankStatus);

        // "Aktive Arbeit"-Sektion: zeigt für jeden Teil, wer gerade daran arbeitet.
        activeWorkContainer.getChildren().clear(); // zuerst alten Inhalt löschen
        for (String partKey : TakeoverState.PART_KEYS) {
            String worker = takeoverState.getWorkerInfo(partKey);
            if (worker == null) continue; // kein Arbeiter eingetragen → überspringen

            boolean approved = takeoverState.isPartApproved(partKey);
            String suffix = approved ? " – Approved ✓" : " – In Progress";
            String color  = approved ? "#66ff66" : "#ffcc00"; // grün = fertig, gelb = läuft noch

            Label lbl = new Label(TakeoverState.getDisplayName(partKey) + ": " + worker + suffix);
            lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px;");
            activeWorkContainer.getChildren().add(lbl);
        }
        // Falls niemand arbeitet, einen Hinweistext anzeigen.
        if (activeWorkContainer.getChildren().isEmpty()) {
            Label none = new Label("No active workers");
            none.setStyle("-fx-text-fill: #555555; -fx-font-size: 13px;");
            activeWorkContainer.getChildren().add(none);
        }

        // Letzte durchgeführte Aktion anzeigen oder "–" wenn noch nichts passiert ist.
        String lastAct = takeoverState.getLastActivity();
        lblLastActivity.setText(lastAct.isEmpty() ? "–" : lastAct);

        // "Takeover Complete"-Button und Statusmeldungen aktualisieren.
        if (takeoverState.isTakeoverComplete()) {
            btnFinish.setVisible(true); // Button einblenden, wenn alle Teile freigegeben sind
            lblWarning.setText("All parts approved – Takeover complete!");
            lblWarning.setStyle("-fx-text-fill: #66ff66; -fx-font-size: 16px;");
            lblStatus.setText("Press 'Takeover Complete' to reset");
        } else {
            btnFinish.setVisible(false); // Button ausblenden, solange noch nicht fertig
            if (data == null || thresholds == null) {
                lblWarning.setText("Error loading sensor data");
                lblStatus.setText("System offline");
            } else {
                updateWarningLabel(data, thresholds); // Sensorwarnungen berechnen und anzeigen
            }
        }

        // Sensor-Details-Sektion: listet alle Teile mit ihren Sensoren auf.
        if (data == null) return; // ohne Daten nichts anzeigen

        sensorContainer.getChildren().clear(); // alten Inhalt löschen

        // Alle drei Shuttle-Teile in der gewünschten Reihenfolge verarbeiten.
        Map<String, ShuttlePart> parts = new LinkedHashMap<>();
        parts.put("orbiter",      data.orbiter);
        parts.put("srb",          data.srb);
        parts.put("externalTank", data.externalTank);

        for (var partEntry : parts.entrySet()) {
            String partName  = partEntry.getKey();
            ShuttlePart part = partEntry.getValue();

            if (part == null || part.getSensors() == null) continue;

            // Kopfzeile für diesen Teil erstellen – grün wenn freigegeben, sonst grau.
            boolean approved    = takeoverState.isPartApproved(partName);
            String headerText   = TakeoverState.getDisplayName(partName).toUpperCase()
                    + (approved ? "  [APPROVED]" : "");
            Label partHeader    = new Label(headerText);
            String headerColor  = approved ? "#66ff66" : "#aaaaaa";
            partHeader.setStyle("-fx-text-fill: " + headerColor
                    + "; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 0 2 0;");
            sensorContainer.getChildren().add(partHeader);

            Map<String, SensorThreshold> partThresholds =
                    thresholds != null ? thresholds.get(partName) : null;

            // Jeden Sensor dieses Teils als farbige Zeile anzeigen.
            for (var sensorEntry : part.getSensors().entrySet()) {
                String sensorName = sensorEntry.getKey();
                double value      = sensorEntry.getValue();

                // Sensor bewerten – OK wenn keine Grenzwerte vorhanden.
                String result = "OK";
                if (partThresholds != null) {
                    SensorThreshold t = partThresholds.get(sensorName);
                    if (t != null) result = sensorService.evaluate(value, t);
                }

                // Farbe je nach Bewertung: gelb = Warnung, rot = Ersatz nötig, grün = OK.
                String color = switch (result) {
                    case "WARNING" -> "#ffcc00";
                    case "REPLACE" -> "#ff4444";
                    default        -> "#66ff66";
                };
                Label sensorLabel = new Label(
                        String.format("  %s: %.2f  [%s]", sensorName, value, result));
                sensorLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 13px;");
                sensorContainer.getChildren().add(sensorLabel);
            }
        }
    }

    // Berechnet die schlimmste aktuelle Warnstufe aller Sensoren
    // und aktualisiert die Warn- und Status-Labels entsprechend.
    private void updateWarningLabel(ShuttleData data,
                                    Map<String, Map<String, SensorThreshold>> thresholds) {
        String worstWarning = "OK"; // Startwert – wird nach oben angepasst

        Map<String, ShuttlePart> parts = new LinkedHashMap<>();
        parts.put("orbiter",      data.orbiter);
        parts.put("srb",          data.srb);
        parts.put("externalTank", data.externalTank);

        // Jeden Teil und jeden Sensor durchgehen und schlimmstes Ergebnis merken.
        for (var partEntry : parts.entrySet()) {
            ShuttlePart part = partEntry.getValue();
            if (part == null || part.getSensors() == null) continue;

            Map<String, SensorThreshold> partThresholds = thresholds.get(partEntry.getKey());
            for (var sensorEntry : part.getSensors().entrySet()) {
                if (partThresholds == null) continue;
                SensorThreshold t = partThresholds.get(sensorEntry.getKey());
                if (t == null) continue;
                String result = sensorService.evaluate(sensorEntry.getValue(), t);
                if (result.equals("REPLACE")) { worstWarning = "REPLACE"; break; } // schlimmst möglich
                if (result.equals("WARNING") && worstWarning.equals("OK")) worstWarning = "WARNING";
            }
        }

        // Je nach schlimmster Warnstufe die Labels einstellen.
        switch (worstWarning) {
            case "OK" -> {
                lblWarning.setText("No warnings");
                lblWarning.setStyle("-fx-text-fill: #ffcc00; -fx-font-size: 16px;");
                lblStatus.setText("Systems nominal");
            }
            case "WARNING" -> {
                lblWarning.setText("Minor issues detected");
                lblWarning.setStyle("-fx-text-fill: #ffcc00; -fx-font-size: 16px;");
                lblStatus.setText("Technician check recommended");
            }
            case "REPLACE" -> {
                lblWarning.setText("Critical issue detected");
                lblWarning.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 16px;");
                lblStatus.setText("Immediate replacement required");
            }
        }
    }

    // Lädt eine andere Ansicht und ersetzt den Inhalt des Fensters.
    // fxml ist der Dateiname der Zielseite, z. B. "mission_control.fxml".
    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) btnMission.getScene().getWindow(); // aktuelles Fenster holen
            stage.getScene().setRoot(root); // Fensterinhalt tauschen
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
