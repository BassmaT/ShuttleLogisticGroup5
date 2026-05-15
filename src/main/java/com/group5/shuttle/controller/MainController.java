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
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import java.io.IOException;
import javafx.scene.Parent;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.animation.PauseTransition;
import javafx.scene.control.DialogPane;

/**
 * Main dashboard controller coordinating navigation, application phases,
 * role-based access control and the AI advisory chat.
 *
 * This controller is responsible for displaying takeover progress,
 * active workers, sensor warnings and sensor details.
 * It is the first controller loaded when the application starts
 * (main_view.fxml).
 */

public class MainController extends BaseController {

    @Override
    protected Button getNavigationButton() {
        return btnMission;
    }

    @Override
    public void loadView(String fxml) {
        // 1. Execute the standard logic for loading the FXML
        super.loadView(fxml);

        // 2. Wait a very brief moment until the UI elements are linked
        Platform.runLater(() -> {
            System.out.println("View geladen: " + fxml);

            // We automatically update the dashboard on every view switch
            updateDashboard();
        });
    }

    // Navigation buttons in the left sidebar.
    @FXML
    private Button btnMission;    // opens Mission Control
    @FXML
    private Button btnTechnician; // opens the technician panel
    @FXML
    private Button btnInventory;  // opens the inventory overview
    @FXML
    private Button btnHistory;    // opens the maintenance history
    @FXML
    private Button btnStaff;      // opens the staff overview
    @FXML
    private Button btnLogistics;  // opens the logistics system
    @FXML
    private Button btnSchedule;   // opens the interactive schedule

    // Button that appears after full takeover (100%) is complete.
    @FXML
    private Button btnFinish;

    // Progress bar – shows 0% to 100% takeover progress.
    @FXML
    private ProgressBar progressTakeover;

    // Shows the current warning level, e.g. "Critical issue detected".
    @FXML
    private Label lblWarning;

    // Shows the general system status, e.g. "Systems nominal".
    @FXML
    private Label lblStatus;

    // Shows the approval status of all three parts, e.g. "Orbiter ✓  SRB ✗  External Tank ✗".
    @FXML
    private Label lblApprovalStatus;

    // Shows the current schedule status (green = on schedule, yellow = hours behind, red = day behind).
    @FXML
    private Label lblScheduleStatus;

    // Container that is dynamically populated with active workers per shuttle part.
    @FXML
    private VBox sensorContainer;

    // Container for the "Active Work" section – who is working where.
    @FXML
    private VBox activeWorkContainer;

    // Shows the most recently performed action, e.g. "J. Miller completed repairs on Orbiter".
    @FXML
    private Label lblLastActivity;

    // Container for the schedule preview
    @FXML
    private VBox scheduleContainer;

    // Container for predictive maintenance warnings
    @FXML
    private VBox predictiveContainer;

    // ── Landing Banner (visible during LANDING and SENSOR_LOADING) ──────────

    // Wrapper VBox of the landing banner
    @FXML
    private VBox landingBanner;

    // Countdown text, e.g. "Approaching in 12:00 min..."
    @FXML
    private Label lblLandingCountdown;

    // Progress bar for the landing countdown
    @FXML
    private ProgressBar progressLanding;

    // Status text below the progress bar
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
    private boolean aiChatActivated = false;

    // Active timer – singleton service so it can be stopped when navigating away
    private final PhaseTimerService phaseTimer = PhaseTimerService.getInstance();

    // initialize() is called automatically when the FXML is loaded.
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

    private void onSensorDataReady() {
        applyPhase(AppPhase.OPERATIONAL);

        if (aiChat != null) {
            aiChat.enableInteraction();
        }
    }

    // Links all navigation buttons to their target views and sets hover effects.
    private void initNavigation() {
        phaseTimer.stop();

        List<Button> navButtons = List.of(btnMission, btnTechnician, btnInventory,
                btnHistory, btnStaff, btnLogistics, btnSchedule);
        for (Button btn : navButtons) {
            btn.setOnMouseEntered(e -> btn.setStyle(Styles.NAV_BTN_HOVER));
            btn.setOnMouseExited(e -> btn.setStyle(Styles.NAV_BTN_NORMAL));
        }

        btnMission.setOnAction(e -> {
            loadView("mission_control.fxml");

            // We wait 300ms until the Mission Control page is truly "there"
            PauseTransition delay = new PauseTransition(Duration.millis(300));
            delay.setOnFinished(event -> {
                System.out.println("Trigger nach Seitenwechsel...");
                updateDashboard();
            });
            delay.play();
        });

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

    // Reads the current app phase and starts the appropriate timer or switches directly to OPERATIONAL.
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
            boolean willBeVisible = !aiDrawer.isVisible();
            aiDrawer.setVisible(willBeVisible);
            aiDrawer.setManaged(willBeVisible); // IMPORTANT: Reserves space in the layout

            if (willBeVisible) {
                aiDrawer.setMinWidth(300); // Force width
                aiDrawer.setPrefWidth(300);

                // If the chat has never been loaded, initialize it now
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

        // Styling for the dialog window (optional, to match the dark theme)
        dlg.getDialogPane().setStyle("-fx-background-color: #161B22; -fx-text-fill: white;");

        dlg.showAndWait().ifPresent(sel -> {
            SessionState.getInstance().setCurrentUser(sel);

            // IMPORTANT: This must be the name defined in your controller
            updateProfileLabel();

            // If role restrictions are in place:
            if (phaseTracker.getAppPhase() == AppPhase.OPERATIONAL) {
                applyRoleRestrictions();
            }

            // Bonus for the AI: update context when switching users
            if (aiChat != null) {
                aiChat.setContext(SessionState.getInstance().getUserRole());
            }
        });
    }

    // ── Phase Management ──────────────────────────────────────────────────────

    // Adjusts UI visibility and button state to match the current phase.
    private void applyPhase(AppPhase phase) {
        boolean isOperational = (phase == AppPhase.OPERATIONAL);

        // Landing banner: visible during LANDING and SENSOR_LOADING
        landingBanner.setVisible(!isOperational);
        landingBanner.setManaged(!isOperational);

        // Sensor-related sections: only visible during operation
        sensorContainer.setVisible(isOperational);
        sensorContainer.setManaged(isOperational);
        predictiveContainer.setVisible(isOperational);
        predictiveContainer.setManaged(isOperational);
        scheduleContainer.setVisible(isOperational);
        scheduleContainer.setManaged(isOperational);

        // Non-logistics buttons: disabled during landing/loading
        btnMission.setDisable(!isOperational);
        btnTechnician.setDisable(!isOperational);
        btnInventory.setDisable(!isOperational);
        btnHistory.setDisable(!isOperational);
        btnStaff.setDisable(!isOperational);
        btnSchedule.setDisable(!isOperational);
        // Logistics is always accessible
        btnLogistics.setDisable(false);

        // Override warning and status labels during landing
        if (!isOperational) {
            lblWarning.setText(phase == AppPhase.LANDING
                    ? "Shuttle is landing – pre-order parts in Logistics"
                    : "Loading sensor data...");
            lblWarning.setStyle("-fx-text-fill: #66aaff; -fx-font-size: 15px;");
            lblStatus.setText(phase == AppPhase.LANDING
                    ? "Operations begin after landing"
                    : "Please wait – sensor calibration in progress");
        }

        // In operation: apply role restrictions for the logged-in employee
        if (isOperational) {
            applyRoleRestrictions();

            if (!aiChatActivated && aiChat != null) {
                aiChat.enableInteraction();
                aiChatActivated = true;
            }

        }
    }

    private void applyRoleRestrictions() {
        roleAccess.applyRestrictions(List.of(btnMission, btnTechnician, btnInventory,
                btnHistory, btnStaff, btnLogistics, btnSchedule));
    }

    // Starts the 1-second tick for the landing countdown – created with AI assistance
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
            // 3-second pause, then start the sensor loading phase
            Timeline pause = new Timeline(new KeyFrame(Duration.seconds(3), ev -> {
                phaseTracker.beginSensorLoading();
                applyPhase(AppPhase.SENSOR_LOADING);
                startSensorLoadingTimer(TakeoverState.SENSOR_LOADING_SECONDS);
            }));
            pause.play();
        });
    }

    // Starts the countdown for loading sensor data (10 sec = 30 min simulated) – created with AI assistance
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

    // Switches to operational mode: loads sensor data, fully displays the dashboard.
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

    // Updates all visible information on the dashboard.
    // Also called by other controllers (e.g. after navigating back).
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

        checkAndShowNewTaskPopup();
    }

    private void checkAndShowNewTaskPopup() {
        SessionState session = SessionState.getInstance();
        Employee currentUser = session.getCurrentUser();

        if (session.hasPendingNotification() && currentUser != null &&
                "Technician".equalsIgnoreCase(currentUser.getRole())) {

            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Mission Control Update");
                alert.setHeaderText("New Task Assigned");

                // Text in English referencing the Orbiter
                alert.setContentText("Hello " + currentUser.getName() + ",\n\n" +
                        "A new maintenance task has been assigned to you.\n" +
                        "Please check the 'Repair Tasks – Orbiter' section for details.");

                // Styling for better readability (white text on dark background)
                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.setStyle("-fx-background-color: #161B22;");

                // Set all labels (header and content) to white
                dialogPane.lookupAll(".label").forEach(node ->
                        node.setStyle("-fx-text-fill: white; -fx-font-weight: bold;")
                );

                // Reset flag and show dialog
                session.setPendingNotification(false);
                alert.showAndWait();
            });
        }
    }

    // Updates the progress bar, tooltip and schedule status label.
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

    // Updates the approval overview, active workers and last activity.
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

    // Controls visibility of the Finish button and shows appropriate warning/status messages.
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

    // and updates the warning and status labels accordingly.
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
            // Loads the FXML file for the AI chat
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ai_chat.fxml"));
            Parent root = loader.load();

            // Gets the controller and links it to this MainController
            aiChat = loader.getController();

            // IMPORTANT: Passes the MainController instance to the AiChatController
            // so that it can call updateDashboard() after an assignment.
            aiChat.setMainController(this);

            // Update UI components in the side drawer
            aiDrawer.getChildren().clear();
            aiDrawer.getChildren().add(root);

            // Set the initial context (user role) for the AI
            var user = SessionState.getInstance().getCurrentUser();
            if (user != null) {
                aiChat.setContext(SessionState.getInstance().getUserRole());
            }

            System.out.println("AI Chat successfully initialized and connected to dashboard.");

        } catch (IOException e) {
            System.err.println("Error initializing AI Chat: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
