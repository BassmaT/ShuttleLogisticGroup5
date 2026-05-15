package com.group5.shuttle.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import com.group5.shuttle.model.UserRole;
import com.group5.shuttle.service.SessionState;
import com.group5.shuttle.model.TakeoverSchedule;
import com.group5.shuttle.model.ScheduleDay;
import com.group5.shuttle.model.ScheduleEntry;
import com.group5.shuttle.service.ScheduleService;
import javafx.collections.ObservableList;
import javafx.application.Platform;
import com.group5.shuttle.model.Employee; // Adjust path to your project if needed
import javafx.scene.control.Alert;

/**
 * AI Chat Controller.
 * Provides a role-dependent advisory dialog based on a finite state machine.
 * Currently implemented for the Planner role.
 */

public class AiChatController {

    @FXML private VBox chatHistory;
    @FXML private TextField inputField;
    @FXML private ScrollPane scrollPane;

    private enum AiState { IDLE, SENSOR_WARNING, TECHNICIAN_RECOMMENDATION, SCHEDULE_PROPOSED, FINISHED }
    private AiState aiState = AiState.IDLE;
    private String currentContext = "DEFAULT";
    private UserRole userRole;
    private boolean interactionEnabled = false;
    // In AiChatController.java at the top with the other fields:
    private MainController mainController;

    // The method called by MainController:
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setContext(UserRole role) {
        this.userRole = role;

        javafx.application.Platform.runLater(() -> {
            // 1. Remove all old buttons and messages
            chatHistory.getChildren().clear();

            // 2. Display status message
            addAiMessage("AI Advisor online for role: " + role);

            // 3. Role-specific logic
            // We compare directly against the enum UserRole.PLANNER here
            if (role != UserRole.PLANNER) {
                addAiMessage("Read-only mode: As a Technician, you can monitor the advisor's analysis, but scheduling is reserved for Planners.");
            } else {
                addAiMessage("Sensor data available. Analysis started.");
            }
        });
    }

    @FXML
    private void initialize() {
        userRole = SessionState.getInstance().getUserRole();

        chatHistory.getChildren().clear();

        addAiMessage("AI Advisor online for role: " + userRole);
        addAiMessage("Waiting for sensor data...");
    }

    public void enableInteraction() {
        interactionEnabled = true;
        addAiMessage("Sensor data available. Analysis started.");
        startStory();
    }

    private void startStory() {
        if (userRole != UserRole.PLANNER) {
            addAiMessage("Access denied. Planner role required.");
            return;
        }

        handleSensorRecommendation();
    }

    @FXML
    private void handleInput() {
        String text = inputField.getText();
        if (text == null || text.isBlank()) return;

        addUserMessage(text);
        inputField.clear();

        addAiMessage("""
            AI:
            Custom adjustment received
            Applying requested changes to schedule...
            Updated plan forwarded to technician.
        """);
        aiState = AiState.FINISHED;
    }
    private void addAiMessage(String text) {
        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setStyle(
                "-fx-text-fill: #c9d1d9;" +
                        "-fx-background-color: #1f2937;" +
                        "-fx-padding: 10;" +
                        "-fx-background-radius: 6;"
        );
        chatHistory.getChildren().add(lbl);
    }

    private void addUserMessage(String text) {
        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-background-color: #8957e5;" +
                        "-fx-padding: 10;" +
                        "-fx-background-radius: 6;"
        );
        chatHistory.getChildren().add(lbl);
    }

        // --- STORY LOGIC (moved from MainController) ---

    private void handleSensorRecommendation() {
        aiState = AiState.SENSOR_WARNING;

        addAiMessage("""
            AI Advisor:
             ⚠ Critical issue detected

            Sensor: coolantPressure
             Value: 2.80 (critical)

            If ignored:
             - High risk of failure during pre-launch check
             - Possible launch abort or system shutdown

            Impact:
             - Expected delay: 2–4 days
             - Additional cost: €180,000 – €300,000

            Reason:
             - Unplanned repair
             - Increased damage to cooling system
             - Loss of launch slot

            If repaired now:
             - Duration: 2 days (planned)
             - Cost: ~€90,000
             - No additional delays expected

            Recommendation:
            Immediate maintenance required.
            """);

                    addAiButton("Dispatch technician", this::handleDecisionYes);
                    addAiButton("Postpone", () ->
                            addAiMessage("""
            AI Advisor:
            Decision postponed.

            Updated risk:
            - Launch delay risk increases to 4–6 days
            - Potential cost escalation: ~€350,000

            Monitoring continues.
            """));
    }

    private void handleDecisionYes() {
        clearButtons();
        if (aiState == AiState.SENSOR_WARNING) {
            aiState = AiState.TECHNICIAN_RECOMMENDATION;
            addAiMessage("""
                AI Advisor:
                Qualified technicians available:

                Ellen Vance
                - Duration: 2 days
                - Cost: ~€90,000
                - No delay

                Marc Stein
                - Duration: 3 days
                - Extra delay: +1 days
                - Additional cost: ~€120,000

                Recommendation:
                Ellen Vance (faster & cheaper)
                """);
            addAiButton("Assign Ellen Vance", () -> handleTechnicianSelected("ELENA"));
            addAiButton("Assign Marc Stein", () -> handleTechnicianSelected("MARCO"));
        }
    }

    private void handleTechnicianSelected(String technician) {
        clearButtons();
        aiState = AiState.SCHEDULE_PROPOSED;

        if ("ELENA".equals(technician)) {
            addAiMessage("""
        AI Advisor:
        Ellen Vance selected (Standard Shift).

        Current Status:
        - Repair window: Today, 13:00
        - Risk: 30% chance of parts arriving late
        - Total Duration: 2 days
        - Cost: ~€90,000

        Note: Standard delivery for coolant valves is currently congested.
        Confirm this risky schedule?
        """);
        } else {
            // Marc Stein logic remains the same...
        }

        addAiButton("Accept (Risk)", () -> {
            clearButtons();
            assignTechnician(technician);
            addAiMessage("AI Advisor: Standard plan integrated. Monitoring part delivery...");
            aiState = AiState.FINISHED;
        });

        addAiButton("Reject / Find better", () -> handleScheduleRejected(technician));
    }

    private void handleScheduleRejected(String technician) {
        clearButtons();

        addAiMessage("""
        AI Advisor:
        Schedule rejected. Analyzing high-priority alternatives...

        Option A (Express Priority)
        - Duration: 2 days (Guaranteed)
        - Cost: ~€145,000
        - Impact: Uses emergency courier for valves.
        - Outcome: 100% chance to meet 13:00 slot.

        Option B (Budget/Safe)
        - Duration: 7 days
        - Cost: ~€75,000
        - Impact: Wait for regular part stock.

        Select option:
    """);

        addAiButton("Option A (Express)", () -> {
            clearButtons();
            assignTechnician(technician);
            addAiMessage("""
        AI Advisor:
        Emergency courier dispatched.
        Parts will arrive at 12:30.

        Ellen Vance informed for 13:00 start.
        """);
            aiState = AiState.FINISHED;
        });

        addAiButton("Option B (Wait)", () -> {
            clearButtons();
            addAiMessage("AI Advisor: Budget plan selected. Schedule pushed to next week.");
            aiState = AiState.FINISHED;
        });
    }

    private void assignTechnician(String technician) {
        if (this.userRole != UserRole.PLANNER) {
            System.out.println("Access denied for role: " + this.userRole);
            return;
        }

        TakeoverSchedule schedule = ScheduleService.getInstance().loadSchedule();
        ScheduleEntry newEntry = null;

        for (ScheduleDay day : schedule.getDays()) {
            if (day.getDayNumber() == 1) {
                newEntry = new ScheduleEntry(
                        "13:00",
                        "Repair: coolantPressure (Orbiter)",
                        "repair",
                        null, // Set below
                        "Orbiter"
                );

                // Get the reactive list
                ObservableList<ScheduleEntry> entries = (ObservableList<ScheduleEntry>) day.getEntries();

                // Find the correct index (between 12:00 and 15:00)
                int insertIndex = entries.size();
                for (int i = 0; i < entries.size(); i++) {
                    if (entries.get(i).getTime().compareTo("13:00") > 0) {
                        insertIndex = i;
                        break;
                    }
                }

                // Set the employee ID based on the selection
                String empId = "ELENA".equals(technician) ? "EMP-001" : "EMP-002";
                newEntry.setAssignedEmployeeId(empId);

                // ADD – this triggers the UI update via the ObservableList
                entries.add(insertIndex, newEntry);
                break;
            }
        }

        // Notify the UI thread
        Platform.runLater(() -> {
            if (mainController != null) {
                mainController.updateDashboard();
                showNotification(technician);
            }
        });

        if ("ELENA".equals(technician) || "Ellen Vance".equalsIgnoreCase(technician)) {
            SessionState.getInstance().setPendingNotification(true);
        }
    }

    private void showNotification(String assignedTechnician) {
        // Gets the currently logged-in user from the SessionState
        Employee currentUser = SessionState.getInstance().getCurrentUser();

        // Check whether anyone is logged in and whether it is the correct technician
        if (currentUser != null && currentUser.getName().equalsIgnoreCase(assignedTechnician)) {

            // Since UI elements (Alert) are being created, wrap in Platform.runLater to be safe
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Mission Control Update");
                alert.setHeaderText("New Work Order Received");
                alert.setContentText("Attention " + assignedTechnician + "! A critical repair task has been assigned to you. Please check your schedule.");

                // Styling (optional, matching the dark theme)
                alert.getDialogPane().setStyle("-fx-background-color: #161B22; -fx-text-fill: white;");

                alert.show();
            });
        }
    }

    // --- HELPER METHODS FOR UI ---

    private void addAiButton(String text, Runnable action) {
        if (!interactionEnabled) {
            return;
        }

        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("...");
        btn.setOnAction(e -> action.run());
        chatHistory.getChildren().add(btn);

    }

    private void clearButtons() {
        chatHistory.getChildren().removeIf(n -> n instanceof Button);
    }

}
