package com.group5.shuttle.controller;

import com.group5.shuttle.model.ShuttleData;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.Employee;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.IScheduleService;
import com.group5.shuttle.service.ISensorDataService;
import com.group5.shuttle.service.PredictiveAnalysisService;
import com.group5.shuttle.service.IPredictiveAnalysisService;
import com.group5.shuttle.service.ScheduleService;
import com.group5.shuttle.service.SensorDataService;
import com.group5.shuttle.service.SensorStatusAggregator;
import com.group5.shuttle.service.SessionState;
import com.group5.shuttle.service.PhaseTimerService;
import com.group5.shuttle.service.IPhaseTracker;
import com.group5.shuttle.service.IPartApproval;
import com.group5.shuttle.service.ITakeoverProgress;
import com.group5.shuttle.service.IWorkerRegistry;
import com.group5.shuttle.service.IRepairAccess;
import com.group5.shuttle.util.Styles;
import com.group5.shuttle.service.TakeoverState;
import com.group5.shuttle.service.RoutineTaskStore;
import com.group5.shuttle.service.OrderStore;
import com.group5.shuttle.service.IPhaseTracker.AppPhase;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.Map;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.Parent;

// Controller für das Haupt-Dashboard (main_view.fxml).
// Zeigt den Übernahme-Fortschritt, aktive Arbeiter, Sensorwarnungen und Sensor-Details.
// Diese Klasse wird als erstes geladen, wenn die App startet.
public class MainController extends BaseController {

    @Override
    protected Button getNavigationButton() {
        return btnMission;
    }

    @Override
    protected void loadView(String fxml) {
        phaseTimer.stop();
        super.loadView(fxml);
    }

    // Navigations-Buttons in der linken Seitenleiste.
    @FXML
    private Button btnMission;    // öffnet Mission Control
    @FXML
    private Button btnTechnician; // öffnet das Techniker-Panel
    @FXML
    private Button btnInventory;  // öffnet die Lagerübersicht
    @FXML
    private Button btnHistory;    // öffnet die Wartungshistorie
    @FXML
    private Button btnStaff;      // öffnet die Mitarbeiter-Übersicht
    @FXML
    private Button btnLogistics;  // öffnet das Logistik-System
    @FXML
    private Button btnSchedule;   // öffnet den interaktiven Zeitplan

    // Button, der nach vollständiger Übernahme (100%) erscheint.
    @FXML
    private Button btnFinish;

    // Fortschrittsbalken – zeigt 0% bis 100% Übernahme-Fortschritt.
    @FXML
    private ProgressBar progressTakeover;

    // Zeigt die aktuelle Warnstufe an, z. B. "Critical issue detected".
    @FXML
    private Label lblWarning;

    // Zeigt den allgemeinen Systemstatus an, z. B. "Systems nominal".
    @FXML
    private Label lblStatus;

    // Zeigt den Freigabe-Status aller drei Teile an, z. B. "Orbiter ✓  SRB ✗  External Tank ✗".
    @FXML
    private Label lblApprovalStatus;

    // Zeigt den aktuellen Zeitplan-Status an (grün = im Plan, gelb = Stunden hinter Plan, rot = Tag hinter Plan).
    @FXML
    private Label lblScheduleStatus;

    // Container, der dynamisch befüllt wird mit den aktiven Arbeitern pro Shuttle-Teil.
    @FXML
    private VBox sensorContainer;

    // Container für die "Aktive Arbeit"-Sektion – wer arbeitet gerade wo.
    @FXML
    private VBox activeWorkContainer;

    // Zeigt die zuletzt durchgeführte Aktion an, z. B. "J. Miller completed repairs on Orbiter".
    @FXML
    private Label lblLastActivity;

    // Container für die Schedule-Vorschau
    @FXML
    private VBox scheduleContainer;

    // Container für Predictive-Maintenance-Warnungen
    @FXML
    private VBox predictiveContainer;

    // ── Landing-Banner (sichtbar während LANDING und SENSOR_LOADING) ──────────

    // Wrapper-VBox des Landing-Banners
    @FXML
    private VBox landingBanner;

    // Countdown-Text, z. B. "Approaching in 12:00 min..."
    @FXML
    private Label lblLandingCountdown;

    // Fortschrittsbalken des Landing-Countdowns
    @FXML
    private ProgressBar progressLanding;

    // Status-Text unter dem Fortschrittsbalken
    @FXML
    private Label lblLandingStatus;

    @FXML
    private Label lblCurrentUser;

    // ── Services & State ──────────────────────────────────────────────────────

    private final ISensorDataService sensorService = SensorDataService.getInstance();
    private final IScheduleService scheduleService = ScheduleService.getInstance();
    private final IPhaseTracker phaseTracker = TakeoverState.getInstance();
    private final IPartApproval partApproval = TakeoverState.getInstance();
    private final ITakeoverProgress takeoverProgress = TakeoverState.getInstance();
    private final IWorkerRegistry workerReg = TakeoverState.getInstance();
    private final IRepairAccess repairAccess = TakeoverState.getInstance();
    private final IPredictiveAnalysisService predictiveService = new PredictiveAnalysisService();

    private SensorPanelController sensorPanel;
    private SchedulePanelController schedulePanel;
    private PredictivePanelController predictivePanel;

    private RoleAccessController roleAccess;

    // Aktiver Timer – Singleton-Service, damit er beim Navigieren gestoppt werden kann
    private final PhaseTimerService phaseTimer = PhaseTimerService.getInstance();

    // initialize() wird automatisch aufgerufen, wenn das FXML geladen ist.
    @FXML
    public void initialize() {
        sensorPanel = new SensorPanelController(sensorContainer, sensorService, partApproval);
        schedulePanel = new SchedulePanelController(scheduleContainer, scheduleService,
                EmployeeService.getInstance());
        predictivePanel = new PredictivePanelController(predictiveContainer, predictiveService);

        initAiChat();
        initNavigation();
        initPhase();
    }

    // Verknüpft alle Navigations-Buttons mit ihren Ziel-Views und setzt Hover-Effekte.
    private void initNavigation() {
        phaseTimer.stop();

        List<Button> navButtons = List.of(btnMission, btnTechnician, btnInventory,
                btnHistory, btnStaff, btnLogistics, btnSchedule);
        for (Button btn : navButtons) {
            btn.setOnMouseEntered(e -> btn.setStyle(Styles.NAV_BTN_HOVER));
            btn.setOnMouseExited(e -> btn.setStyle(Styles.NAV_BTN_NORMAL));
        }

        btnMission.setOnAction(e -> loadView("mission_control.fxml"));
        btnTechnician.setOnAction(e -> loadView("technician.fxml"));
        btnInventory.setOnAction(e -> loadView("inventory.fxml"));
        btnHistory.setOnAction(e -> loadView("history.fxml"));
        btnStaff.setOnAction(e -> loadView("staff.fxml"));
        btnLogistics.setOnAction(e -> loadView("logistics.fxml"));
        btnSchedule.setOnAction(e -> loadView("schedule_view.fxml"));

        btnFinish.setOnAction(e -> {
            phaseTracker.reset();
            RoutineTaskStore.getInstance().reset();
            OrderStore.getInstance().clear();
            ShuttleData data = sensorService.loadSensorData();
            Map<String, Map<String, SensorThreshold>> thresholds = sensorService.loadThresholds();
            repairAccess.generateRepairs(data, thresholds, sensorService);
            updateDashboard();
        });

        updateProfileLabel();

        Map<String, Button> btnMap = Map.of(
                "btnMission", btnMission,
                "btnTechnician", btnTechnician,
                "btnInventory", btnInventory,
                "btnHistory", btnHistory,
                "btnStaff", btnStaff,
                "btnLogistics", btnLogistics,
                "btnSchedule", btnSchedule);
        roleAccess = new RoleAccessController(RoleConfig.RESTRICTED_BUTTONS, btnMap);
    }

    // Liest die aktuelle App-Phase und startet den passenden Timer oder wechselt direkt in OPERATIONAL.
    private void initPhase() {
        AppPhase phase = phaseTracker.getAppPhase();
        int remaining = phaseTracker.getRemainingSeconds();

        if (phase == AppPhase.LANDING) {
            applyPhase(AppPhase.LANDING);
            if (remaining > 0) {
                startLandingTimer(remaining);
            } else {
                phaseTracker.beginSensorLoading();
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
            activateOperational();
        }
    }

    @FXML
    private void toggleAiChat() {
        if (aiDrawer != null) {
            boolean wirdSichtbar = !aiDrawer.isVisible();
            aiDrawer.setVisible(wirdSichtbar);
            aiDrawer.setManaged(wirdSichtbar); // WICHTIG: Reserviert den Platz im Layout

            if (wirdSichtbar) {
                aiDrawer.setMinWidth(300); // Breite erzwingen
                aiDrawer.setPrefWidth(300);

                // Falls der Chat noch nie geladen wurde, jetzt initialisieren
                if (aiChat == null) {
                    initAiChat();
                }
            }
        }
    }

    private void updateProfileLabel() {
        Employee current = SessionState.getInstance().getCurrentUser();
        if (current != null && lblCurrentUser != null) {
            lblCurrentUser.setText(current.getName() + " (" + current.getRole() + ")");
        }
    }

    @FXML
    private void switchUser() {
        List<Employee> all = EmployeeService.getInstance().getAllEmployees();
        Employee current = SessionState.getInstance().getCurrentUser();

        ChoiceDialog<Employee> dlg = new ChoiceDialog<>(current, all);
        dlg.setTitle("Switch User");
        dlg.setHeaderText("Select user — progress is kept:");

        // Styling für das Dialog-Fenster (optional, damit es zum Dark-Theme passt)
        dlg.getDialogPane().setStyle("-fx-background-color: #161B22; -fx-text-fill: white;");

        dlg.showAndWait().ifPresent(sel -> {
            SessionState.getInstance().setCurrentUser(sel);

            // WICHTIG: Hier muss der Name stehen, der in deinem Controller definiert ist
            updateProfileLabel();

            // Falls du Rollen-Einschränkungen hast:
            if (phaseTracker.getAppPhase() == AppPhase.OPERATIONAL) {
                applyRoleRestrictions();
            }

            // Bonus für die KI: Kontext beim Wechsel aktualisieren
            if (aiChat != null) {
                aiChat.setContext(sel.getRole().toString());
            }
        });
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

        // Im Betrieb: Rollenbeschränkungen des eingeloggten Mitarbeiters anwenden
        if (isOperational) {
            applyRoleRestrictions();
        }
    }

    private void applyRoleRestrictions() {
        roleAccess.applyRestrictions(List.of(btnMission, btnTechnician, btnInventory,
                btnHistory, btnStaff, btnLogistics, btnSchedule));
    }

    // Startet den 1-Sekunden-Takt für den Landecountdown mit HILFE von KI erstellt
    private void startLandingTimer(int seconds) {
        phaseTimer.start(seconds, remaining -> {
            double progress = 1.0 - (double) remaining / TakeoverState.LANDING_SECONDS;
            progressLanding.setProgress(progress);
            lblLandingCountdown.setText(String.format("Approaching in %02d:00 min...", remaining));
            if (remaining > 10) {
                lblLandingStatus.setText("Shuttle on approach – systems nominal");
            } else if (remaining > 4) {
                lblLandingStatus.setText("Initiating landing sequence...");
            } else {
                lblLandingStatus.setText("Final approach – deploying landing gear...");
            }
        }, () -> {
            progressLanding.setProgress(1.0);
            progressLanding.setStyle(Styles.ACCENT_SUCCESS);
            lblLandingCountdown.setText("Shuttle landed successfully.");
            lblLandingCountdown.setStyle(Styles.labelBold16("#66ff66"));
            lblLandingStatus.setText("Takeover process is starting soon...");
            lblWarning.setText("Shuttle landed successfully. Takeover process is starting soon.");
            lblWarning.setStyle("-fx-text-fill: #66ff66; -fx-font-size: 14px;");
            // 3 Sekunden Pause, dann Sensor-Ladephase starten
            Timeline pause = new Timeline(new KeyFrame(Duration.seconds(3), ev -> {
                phaseTracker.beginSensorLoading();
                applyPhase(AppPhase.SENSOR_LOADING);
                startSensorLoadingTimer(TakeoverState.SENSOR_LOADING_SECONDS);
            }));
            pause.play();
        });
    }

    // Startet den Countdown für das Laden der Sensordaten (10 Sek. = 30 Min. simuliert) mit HILFE von KI erstellt
    private void startSensorLoadingTimer(int seconds) {
        progressLanding.setProgress(0.0);
        progressLanding.setStyle(Styles.ACCENT_WARNING);
        lblLandingCountdown.setStyle(Styles.labelBold16("#ffcc00"));
        phaseTimer.start(seconds, remaining -> {
            double progress = 1.0 - (double) remaining / TakeoverState.SENSOR_LOADING_SECONDS;
            progressLanding.setProgress(progress);
            lblLandingCountdown.setText(String.format("Loading sensor data... %d min", remaining));
            lblLandingStatus.setText("Calibrating sensors – please wait");
        }, this::activateOperational);
    }

    // Wechselt in den Betriebsmodus: Sensordaten laden, Dashboard vollständig anzeigen.
    private void activateOperational() {
        phaseTracker.setOperational();
        ShuttleData data = sensorService.loadSensorData();
        Map<String, Map<String, SensorThreshold>> thresholds = sensorService.loadThresholds();
        if (data != null && thresholds != null) {
            repairAccess.generateRepairs(data, thresholds, sensorService);
        }
        applyPhase(AppPhase.OPERATIONAL);
        updateDashboard();
    }

    // Aktualisiert alle sichtbaren Informationen auf dem Dashboard.
    // Wird auch von anderen Controllern aufgerufen (z. B. nach dem Zurück-Navigieren).
    public void updateDashboard() {
        if (phaseTracker.getAppPhase() != AppPhase.OPERATIONAL) return;
        ShuttleData data = sensorService.loadSensorData();
        Map<String, Map<String, SensorThreshold>> thresholds = sensorService.loadThresholds();

        updateProgressAndSchedule();
        updateApprovalAndWorkers();
        updateFinishButton(data, thresholds);

        if (data == null) return;
        updateSensorList(data, thresholds);
        updateSchedulePreview();
        updatePredictiveWarnings();
    }

    // Aktualisiert Fortschrittsbalken, Tooltip und Zeitplan-Statuslabel.
    private void updateProgressAndSchedule() {
        double progress = takeoverProgress.getProgress();
        progressTakeover.setProgress(progress);
        int pct = (int) Math.round(progress * 100);
        progressTakeover.setTooltip(new Tooltip(pct + "% approved"));

        IPhaseTracker.ScheduleStatus schedStatus = phaseTracker.getScheduleStatus();
        progressTakeover.setStyle(schedStatus.getProgressStyle());

        double simH = phaseTracker.getSimulatedHoursElapsed();
        int simDay = Math.min((int) (simH / 24) + 1, 3);
        int simHour = (int) (simH % 24);
        String timeText = String.format("Day %d, %02d:00 (sim)", simDay, simHour);
        lblScheduleStatus.setText(timeText + "  –  " + schedStatus.getLabel());
        lblScheduleStatus.setStyle("-fx-text-fill: " + schedStatus.getColor() + "; -fx-font-size: 11px;");
    }

    // Aktualisiert die Freigabe-Übersicht, aktive Arbeiter und letzte Aktivität.
    private void updateApprovalAndWorkers() {
        String orbiterStatus = partApproval.isPartApproved("orbiter") ? "Orbiter ✓" : "Orbiter ✗";
        String srbStatus = partApproval.isPartApproved("srb") ? "SRB ✓" : "SRB ✗";
        String tankStatus = partApproval.isPartApproved("externalTank") ? "External Tank ✓" : "External Tank ✗";
        lblApprovalStatus.setText(orbiterStatus + "   " + srbStatus + "   " + tankStatus);

        activeWorkContainer.getChildren().clear();
        for (String partKey : TakeoverState.PART_KEYS) {
            String worker = workerReg.getWorkerInfo(partKey);
            if (worker == null) continue;
            boolean approved = partApproval.isPartApproved(partKey);
            String suffix = approved ? " – Approved ✓" : " – In Progress";
            String color = approved ? "#66ff66" : "#ffcc00";
            Label lbl = new Label(TakeoverState.getDisplayName(partKey) + ": " + worker + suffix);
            lbl.setStyle(Styles.label13(color));
            activeWorkContainer.getChildren().add(lbl);
        }
        if (activeWorkContainer.getChildren().isEmpty()) {
            Label none = new Label("No active workers");
            none.setStyle(Styles.label13("#555555"));
            activeWorkContainer.getChildren().add(none);
        }

        String lastAct = takeoverProgress.getLastActivity();
        lblLastActivity.setText(lastAct.isEmpty() ? "–" : lastAct);
    }

    // Steuert Sichtbarkeit des Finish-Buttons und zeigt passende Warn-/Statusmeldungen.
    private void updateFinishButton(ShuttleData data,
                                    Map<String, Map<String, SensorThreshold>> thresholds) {
        if (takeoverProgress.isTakeoverComplete()) {
            btnFinish.setVisible(true);
            lblWarning.setText("All parts approved – Takeover complete!");
            lblWarning.setStyle(Styles.label16("#66ff66"));
        } else {
            btnFinish.setVisible(false);
            if (data == null || thresholds == null) {
                lblWarning.setText("Error loading sensor data");
                lblStatus.setText("System offline");
            } else {
                updateWarningLabel(data, thresholds);
            }
        }
    }

    private void updateSensorList(ShuttleData data,
                                  Map<String, Map<String, SensorThreshold>> thresholds) {
        sensorPanel.update(data, thresholds);
    }

    private void updateSchedulePreview() {
        schedulePanel.update();
    }

    private void updatePredictiveWarnings() {
        predictivePanel.update();
    }

    // und aktualisiert die Warn- und Status-Labels entsprechend.
    private void updateWarningLabel(ShuttleData data,
                                    Map<String, Map<String, SensorThreshold>> thresholds) {
        switch (SensorStatusAggregator.findWorstSensorStatus(data, thresholds, sensorService)) {
            case OK -> {
                lblWarning.setText("No warnings");
                lblWarning.setStyle(Styles.label16("#ffcc00"));
                lblStatus.setText("Systems nominal");
            }
            case WARNING -> {
                lblWarning.setText("Minor issues detected");
                lblWarning.setStyle(Styles.label16("#ffcc00"));
                lblStatus.setText("Technician check recommended");
            }
            case REPLACE -> {
                lblWarning.setText("Critical issue detected");
                lblWarning.setStyle(Styles.label16("#ff4444"));
                lblStatus.setText("Immediate replacement required");
            }
        }
    }

    @FXML
    private StackPane aiDrawer;
    private AiChatController aiChat;


    private void initAiChat() {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/view/ai_chat.fxml"));
            Parent root = loader.load();

            aiChat = loader.getController();

            aiDrawer.getChildren().clear();
            aiDrawer.getChildren().add(root);

            var user = SessionState.getInstance().getCurrentUser();
            if (user != null) {
                aiChat.setContext(user.getRole().toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
