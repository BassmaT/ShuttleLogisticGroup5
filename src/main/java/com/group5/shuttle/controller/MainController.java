package com.group5.shuttle.controller;

import com.group5.shuttle.model.ShuttleData;
import com.group5.shuttle.model.ShuttlePart;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ScheduleDay;
import com.group5.shuttle.model.ScheduleEntry;
import com.group5.shuttle.model.TakeoverSchedule;
import com.group5.shuttle.model.TrendResult;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.PredictiveAnalysisService;
import com.group5.shuttle.service.SensorService;
import com.group5.shuttle.service.TakeoverState;
import com.group5.shuttle.service.TakeoverState.AppPhase;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

// Controller für das Haupt-Dashboard (main_view.fxml).
// Zeigt den Übernahme-Fortschritt, aktive Arbeiter, Sensorwarnungen und Sensor-Details.
// Diese Klasse wird als erstes geladen, wenn die App startet.
public class MainController {

    // Navigations-Buttons in der linken Seitenleiste.
    @FXML private Button btnMission;    // öffnet Mission Control
    @FXML private Button btnTechnician; // öffnet das Techniker-Panel
    @FXML private Button btnInventory;  // öffnet die Lagerübersicht
    @FXML private Button btnHistory;    // öffnet die Wartungshistorie
    @FXML private Button btnStaff;      // öffnet die Mitarbeiter-Übersicht
    @FXML private Button btnLogistics;  // öffnet das Logistik-System
    @FXML private Button btnSchedule;   // öffnet den interaktiven Zeitplan

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

    // Container für die Schedule-Vorschau
    @FXML private VBox scheduleContainer;

    // Container für Predictive-Maintenance-Warnungen
    @FXML private VBox predictiveContainer;

    // ── Landing-Banner (sichtbar während LANDING und SENSOR_LOADING) ──────────

    // Wrapper-VBox des Landing-Banners
    @FXML private VBox landingBanner;

    // Countdown-Text, z. B. "Approaching in 12:00 min..."
    @FXML private Label lblLandingCountdown;

    // Fortschrittsbalken des Landing-Countdowns
    @FXML private ProgressBar progressLanding;

    // Status-Text unter dem Fortschrittsbalken
    @FXML private Label lblLandingStatus;

    @FXML private VBox chatHistory;
    @FXML private TextField txtChatInput;

    @FXML private StackPane aiDrawer;

    @FXML

    // ── Services & State ──────────────────────────────────────────────────────

    private void addAiMessage(String text) {
        Label label = new Label(text);
        boolean isUser = text.startsWith("You:");

        label.setStyle(isUser
                ? "-fx-background-color: #21262d; -fx-text-fill: #E6EDF3; -fx-padding: 8 12; -fx-background-radius: 10;"
                : "-fx-background-color: #161B22; -fx-text-fill: #8957e5; -fx-padding: 8 12; -fx-background-radius: 10; -fx-border-color: #8957e5; -fx-border-width: 0.5;");

        label.setWrapText(true);
        label.setMaxWidth(280);
        chatHistory.getChildren().add(label);
    }

    // SensorService lädt die Sensordaten und Grenzwerte aus den JSON-Dateien.
    private final SensorService sensorService = new SensorService();

    // TakeoverState ist der zentrale Zustandsspeicher der Übernahme – Singleton.
    private final TakeoverState takeoverState = TakeoverState.getInstance();

    // Service für die Trendanalyse aus den letzten 5 Flügen
    private final PredictiveAnalysisService predictiveService = new PredictiveAnalysisService();

    // Aktiver Timer – static, damit er beim Navigieren und Zurückkommen gestoppt werden kann
    private static Timeline activeTimer = null;

    // initialize() wird automatisch aufgerufen, wenn das FXML geladen ist.
    @FXML
    public void initialize() {

        // --- 1. KI INITIALISIERUNG ---
        addAiMessage("AI Advisor: Systems online. Standing by for landing data.");

        // Chat-Eingabe verarbeiten (Wenn User Enter drückt)
        txtChatInput.setOnAction(e -> handleChatInput());

        // --- 2. NAVIGATION & STYLING ---
        if (activeTimer != null) { activeTimer.stop(); activeTimer = null; }

        // Wir erstellen eine Liste der Buttons für einfaches Styling
        List<Button> navButtons = List.of(btnMission, btnTechnician, btnInventory, btnHistory, btnStaff, btnLogistics, btnSchedule);

        for (Button btn : navButtons) {
            // Hover-Effekt für mehr "Gefühl" beim Nutzen
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(75, 156, 255, 0.1); -fx-text-fill: white; -fx-min-width: 160; -fx-alignment: BASELINE_LEFT;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #8b949e; -fx-min-width: 160; -fx-alignment: BASELINE_LEFT;"));
        }

        // Navigations-Buttons verknüpfen
        btnMission.setOnAction(e -> loadView("mission_control.fxml"));
        btnTechnician.setOnAction(e -> loadView("technician.fxml"));
        btnInventory.setOnAction(e -> loadView("inventory.fxml"));
        btnHistory.setOnAction(e -> loadView("history.fxml"));
        btnStaff.setOnAction(e -> loadView("staff.fxml"));
        btnLogistics.setOnAction(e -> loadView("logistics.fxml"));
        btnSchedule.setOnAction(e -> loadView("schedule_view.fxml"));

        // "Takeover Complete"-Button
        btnFinish.setOnAction(e -> {
            takeoverState.reset();
            ShuttleData data = sensorService.loadSensorData();
            Map<String, Map<String, SensorThreshold>> thresholds = sensorService.loadThresholds();
            takeoverState.generateRepairs(data, thresholds, sensorService);
            updateDashboard();
        });

        // Phase aus dem Singleton lesen und entsprechend reagieren
        AppPhase phase = takeoverState.getAppPhase();
        int remaining   = takeoverState.getRemainingSeconds();

        if (phase == AppPhase.LANDING) {
            applyPhase(AppPhase.LANDING);
            if (remaining > 0) {
                startLandingTimer(remaining);
            } else {
                // Landung bereits abgeschlossen (z. B. kam man von einer anderen View zurück)
                takeoverState.beginSensorLoading();
                applyPhase(AppPhase.SENSOR_LOADING);
                startSensorLoadingTimer(TakeoverState.SENSOR_LOADING_SECONDS);
            }
        } else if (phase == AppPhase.SENSOR_LOADING) {
            applyPhase(AppPhase.SENSOR_LOADING);
            if (remaining > 0) {
                startSensorLoadingTimer(remaining);
            } else {
                activateOperational();
            }
        } else {
            // Phase OPERATIONAL: normaler Betrieb
            activateOperational();
        }
    }

    @FXML
    private void toggleAiChat() {
        if (aiDrawer != null) {
            boolean isVisible = aiDrawer.isVisible();
            aiDrawer.setVisible(!isVisible);
            aiDrawer.setManaged(!isVisible);
        }
    }

    @FXML
    private void handleChatInput() {
        String message = txtChatInput.getText();

        // Validierung: Nur senden, wenn das Feld nicht leer ist
        if (message != null && !message.isBlank()) {
            addAiMessage("You: " + message);

            // KI Logik: Hier kannst du deine Datenbank-Werte "vorgaukeln"
            if (message.toLowerCase().contains("cost")) {
                addAiMessage("AI: Analyzing database... Current delay costs are 1.2M € per day.");
            } else if (message.toLowerCase().contains("status")) {
                addAiMessage("AI: All shuttle systems are currently within nominal parameters.");
            } else {
                addAiMessage("AI: Telemetry analysis in progress for: " + message);
            }

            // Feld leeren, damit man direkt neu tippen kann
            txtChatInput.clear();
        }
    }

    // ── Phase-Management ──────────────────────────────────────────────────────

    // Passt die UI-Sichtbarkeit und den Button-Status an die aktuelle Phase an.
    private void applyPhase(AppPhase phase) {
        boolean isOperational = (phase == AppPhase.OPERATIONAL);

        // Landing-Banner: sichtbar während LANDING und SENSOR_LOADING
        landingBanner.setVisible(!isOperational);
        landingBanner.setManaged(!isOperational);

        // Sensor-relevante Abschnitte: nur im Betrieb sichtbar
        sensorContainer.setVisible(isOperational);
        sensorContainer.setManaged(isOperational);
        predictiveContainer.setVisible(isOperational);
        predictiveContainer.setManaged(isOperational);
        scheduleContainer.setVisible(isOperational);
        scheduleContainer.setManaged(isOperational);

        // Nicht-Logistics-Buttons: deaktiviert während Landung/Laden
        btnMission.setDisable(!isOperational);
        btnTechnician.setDisable(!isOperational);
        btnInventory.setDisable(!isOperational);
        btnHistory.setDisable(!isOperational);
        btnStaff.setDisable(!isOperational);
        btnSchedule.setDisable(!isOperational);
        // Logistics ist immer zugänglich
        btnLogistics.setDisable(false);

        // Warn- und Status-Label während Landung überschreiben
        if (!isOperational) {
            lblWarning.setText(phase == AppPhase.LANDING
                ? "Shuttle is landing – pre-order parts in Logistics"
                : "Loading sensor data...");
            lblWarning.setStyle("-fx-text-fill: #66aaff; -fx-font-size: 15px;");
            lblStatus.setText(phase == AppPhase.LANDING
                ? "Operations begin after landing"
                : "Please wait – sensor calibration in progress");
        }
    }

    // Startet den 1-Sekunden-Takt für den Landecountdown.
    private void startLandingTimer(int seconds) {
        final int[] remaining = {seconds};
        activeTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remaining[0]--;
            double progress = 1.0 - (double) remaining[0] / TakeoverState.LANDING_SECONDS;
            progressLanding.setProgress(progress);
            lblLandingCountdown.setText(String.format("Approaching in %02d:00 min...", remaining[0]));
            if (remaining[0] > 10) {
                lblLandingStatus.setText("Shuttle on approach – systems nominal");
            } else if (remaining[0] > 4) {
                lblLandingStatus.setText("Initiating landing sequence...");
            } else {
                lblLandingStatus.setText("Final approach – deploying landing gear...");
            }
        }));
        activeTimer.setCycleCount(seconds);
        activeTimer.setOnFinished(e -> {
            activeTimer = null;
            // Landung abgeschlossen
            progressLanding.setProgress(1.0);
            progressLanding.setStyle("-fx-accent: #66ff66;");
            lblLandingCountdown.setText("Shuttle landed successfully.");
            lblLandingCountdown.setStyle("-fx-text-fill: #66ff66; -fx-font-size: 16px; -fx-font-weight: bold;");
            lblLandingStatus.setText("Takeover process is starting soon...");
            lblWarning.setText("Shuttle landed successfully. Takeover process is starting soon.");
            lblWarning.setStyle("-fx-text-fill: #66ff66; -fx-font-size: 14px;");
            // 3 Sekunden Pause, dann Sensor-Ladephase starten
            Timeline pause = new Timeline(new KeyFrame(Duration.seconds(3), ev -> {
                takeoverState.beginSensorLoading();
                applyPhase(AppPhase.SENSOR_LOADING);
                startSensorLoadingTimer(TakeoverState.SENSOR_LOADING_SECONDS);
            }));
            pause.play();
        });
        activeTimer.play();
    }

    // Startet den Countdown für das Laden der Sensordaten (10 Sek. = 30 Min. simuliert).
    private void startSensorLoadingTimer(int seconds) {
        progressLanding.setProgress(0.0);
        progressLanding.setStyle("-fx-accent: #ffcc00;");
        lblLandingCountdown.setStyle("-fx-text-fill: #ffcc00; -fx-font-size: 16px; -fx-font-weight: bold;");
        final int[] remaining = {seconds};
        activeTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remaining[0]--;
            double progress = 1.0 - (double) remaining[0] / TakeoverState.SENSOR_LOADING_SECONDS;
            progressLanding.setProgress(progress);
            lblLandingCountdown.setText(String.format("Loading sensor data... %d min", remaining[0]));
            lblLandingStatus.setText("Calibrating sensors – please wait");
        }));
        activeTimer.setCycleCount(seconds);
        activeTimer.setOnFinished(e -> {
            activeTimer = null;
            activateOperational();
        });
        activeTimer.play();
    }

    // Wechselt in den Betriebsmodus: Sensordaten laden, Dashboard vollständig anzeigen.
    private void activateOperational() {
        takeoverState.setOperational();
        ShuttleData data = sensorService.loadSensorData();
        Map<String, Map<String, SensorThreshold>> thresholds = sensorService.loadThresholds();
        if (data != null && thresholds != null) {
            takeoverState.generateRepairs(data, thresholds, sensorService);
        }
        applyPhase(AppPhase.OPERATIONAL);
        updateDashboard();
    }

    // Aktualisiert alle sichtbaren Informationen auf dem Dashboard.
    // Wird auch von anderen Controllern aufgerufen (z. B. nach dem Zurück-Navigieren).
    public void updateDashboard() {
        // Sensor-Abschnitte nur im Betriebsmodus aktualisieren
        if (takeoverState.getAppPhase() != AppPhase.OPERATIONAL) return;
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

        // 3-Tage-Zeitplan: Vorschau aller Tage aus schedule.json anzeigen
        updateSchedulePreview();

        // Predictive Maintenance: Trendwarnungen anzeigen
        updatePredictiveWarnings();
    }

    // Lädt alle Tage aus schedule.json und zeigt sie als strukturiertes Grid im scheduleContainer an
    private void updateSchedulePreview() {
        if (scheduleContainer == null) return;
        scheduleContainer.getChildren().clear();

        TakeoverSchedule schedule = sensorService.loadSchedule();
        if (schedule == null || schedule.getDays() == null) return;

        for (ScheduleDay day : schedule.getDays()) {
            // Tagesüberschrift
            Label dayLabel = new Label(day.getLabel());
            dayLabel.setStyle("-fx-text-fill: #66aaff; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 6 0 2 0;");
            scheduleContainer.getChildren().add(dayLabel);

            // Spalten-Header-Zeile
            HBox header = new HBox();
            header.setStyle("-fx-background-color: #333333; -fx-padding: 2 4;");
            Label hTime = new Label("Time");    hTime.setPrefWidth(65);  hTime.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px; -fx-font-weight: bold;");
            Label hTask = new Label("Task");    hTask.setPrefWidth(240); hTask.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px; -fx-font-weight: bold;");
            Label hEmp  = new Label("Employee");hEmp.setPrefWidth(150);  hEmp.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px; -fx-font-weight: bold;");
            header.getChildren().addAll(hTime, hTask, hEmp);
            scheduleContainer.getChildren().add(header);

            // Eintragszeilen
            for (ScheduleEntry entry : day.getEntries()) {
                // Hintergrundfarbe je nach Kategorie (gedimmt)
                String bg = switch (entry.getCategory()) {
                    case "repair"     -> "#3a1a1a";
                    case "approval"   -> "#1a3a1a";
                    case "routine"    -> "#3a3a1a";
                    case "diagnostic" -> "#1a2a3a";
                    case "break"      -> "#2a2a2a";
                    default           -> "#252525";
                };
                String fg = switch (entry.getCategory()) {
                    case "repair"     -> "#ff6666";
                    case "approval"   -> "#66ff66";
                    case "routine"    -> "#ffcc00";
                    case "diagnostic" -> "#66aaff";
                    default           -> "#aaaaaa";
                };

                // Mitarbeitername auflösen
                String empName = "–";
                if (entry.getAssignedEmployeeId() != null) {
                    com.group5.shuttle.model.Employee emp =
                        EmployeeService.getInstance().getById(entry.getAssignedEmployeeId());
                    empName = emp != null ? emp.getName() : entry.getAssignedEmployeeId();
                }

                HBox row = new HBox();
                row.setStyle("-fx-background-color: " + bg + "; -fx-padding: 3 4;");
                Label lTime = new Label(entry.getTime()); lTime.setPrefWidth(65);  lTime.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px;");
                Label lTask = new Label(entry.getTask()); lTask.setPrefWidth(240); lTask.setStyle("-fx-text-fill: " + fg + "; -fx-font-size: 12px;");
                Label lEmp  = new Label(empName);          lEmp.setPrefWidth(150);  lEmp.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 12px;");
                row.getChildren().addAll(lTime, lTask, lEmp);
                scheduleContainer.getChildren().add(row);
            }
        }
    }

    // Analysiert Sensortrends und zeigt Warnungen für kritische Entwicklungen an
    private void updatePredictiveWarnings() {
        if (predictiveContainer == null) return;
        predictiveContainer.getChildren().clear();

        Map<String, List<TrendResult>> trends = predictiveService.analyzeAll();
        boolean foundWarning = false;

        for (var partEntry : trends.entrySet()) {
            for (TrendResult trend : partEntry.getValue()) {
                // Nur Warnungen anzeigen, die in ≤5 Flügen den Grenzwert erreichen
                if (trend.getDirection().equals("STABLE")) continue;
                if (trend.getFlightsUntilLimit() < 0 || trend.getFlightsUntilLimit() > 5) continue;

                foundWarning = true;
                // Farbe je nach Dringlichkeit (0 = sofort, ≤2 = kritisch, ≤5 = beobachten)
                String color = trend.getFlightsUntilLimit() == 0 ? "#ff4444"
                             : trend.getFlightsUntilLimit() <= 2 ? "#ff8800"
                             : "#ffcc00";
                String partDisplay = TakeoverState.getDisplayName(trend.getPartKey());
                Label lbl = new Label("  [" + partDisplay + "] " + trend.getRecommendation());
                lbl.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");
                lbl.setWrapText(true);
                predictiveContainer.getChildren().add(lbl);
            }
        }
        if (!foundWarning) {
            Label ok = new Label("  No critical trends detected");
            ok.setStyle("-fx-text-fill: #66ff66; -fx-font-size: 12px;");
            predictiveContainer.getChildren().add(ok);
        }
    }
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
        // Timer pausieren, damit er beim Zurückkehren korrekt fortgesetzt wird
        if (activeTimer != null) { activeTimer.stop(); activeTimer = null; }
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
