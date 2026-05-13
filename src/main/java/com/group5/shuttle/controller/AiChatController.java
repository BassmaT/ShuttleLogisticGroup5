package com.group5.shuttle.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import com.group5.shuttle.model.UserRole;
import com.group5.shuttle.service.SessionState;

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

    public void setContext(UserRole role) {
        this.userRole = role;

        chatHistory.getChildren().clear();
        addAiMessage("AI Advisor online for role: " + role);
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

        // --- STORY LOGIK (aus MainController übernommen) ---

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
             - Duration: 6 days (planned)
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
                
                Elena Vance
                - Duration: 6 days
                - Cost: ~€90,000
                - No delay
                
                Marco Stein
                - Duration: 7–8 days
                - Extra delay: +1–2 days
                - Additional cost: ~€120,000
                
                Recommendation:
                Elena Vance (faster & cheaper)
                """);
            addAiButton("Assign Elena Vance", () -> handleTechnicianSelected("ELENA"));
            addAiButton("Assign Marco Stein", () -> handleTechnicianSelected("MARCO"));
        }
    }

    private void handleTechnicianSelected(String technician) {
        clearButtons();
        aiState = AiState.SCHEDULE_PROPOSED;

        if ("ELENA".equals(technician)) {
            addAiMessage("""
            AI Advisor:
            Elena Vance assigned
            
            Outcome:
            - Duration: 6 days
            - No launch delay
            - Cost: ~€90,000
            
            Confirm schedule?
            """);
            } else {
                addAiMessage("""
            AI Advisor:
            Marco Stein assigned
            
            Outcome:
            - Duration: 7–8 days
            - Launch delay: +1–2 days
            - Total cost: ~€120,000
            
            Confirm schedule?
            """);
        }

        addAiButton("Accept schedule", () -> {
            clearButtons();
            addAiMessage("""
            AI Advisor:
            - Maintenance scheduled
            - Plan integrated
            
            Monitoring continues.
            """);
            aiState = AiState.FINISHED;
        });

        addAiButton("Reject schedule", () -> handleScheduleRejected());
    }

    private void handleScheduleRejected() {
        clearButtons();

        addAiMessage("""
            AI:
            Schedule rejected.
        
            Alternative options:
        
            Option A (Faster)
            - Duration: 4 days
            - Cost: ~€140,000
        
            Option B (Cheaper)
            - Duration: 7 days
            - Cost: ~€75,000
            - Launch delay: +2 days
        
            Option C (Balanced)
            - Duration: 5 days
            - Cost: ~€110,000
        
            Select option:
        """);

        addAiButton("Option A (4 days)", () -> {
            clearButtons();
            addAiMessage("""
            AI:
            Fast-track schedule selected
            
            Order forwarded to technician.
            """);
            aiState = AiState.FINISHED;
        });

        addAiButton("Option B (7 days)", () -> {
            clearButtons();
            addAiMessage("""
            AI:
            Cost-optimized schedule selected
            
            Order forwarded to technician.
            """);
            aiState = AiState.FINISHED;
        });

        addAiButton("Option C (5 days)", () -> {
            clearButtons();
            addAiMessage("""
            AI:
            Balanced schedule selected
            
            Order forwarded to technician.
            """);
            aiState = AiState.FINISHED;
        });


        addAiButton("Custom adjustment", () -> {
            clearButtons();

            addAiMessage("""
            AI:
            Please specify adjustments.
            
            (e.g. reduce duration, minimize cost, limit delay)
            """);

            aiState = AiState.SCHEDULE_PROPOSED;
        });

    }

    // --- HILFSMETHODEN FÜR UI ---

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