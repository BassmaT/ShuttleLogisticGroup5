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


    public void setContext(UserRole role) {
        this.userRole = role;

        chatHistory.getChildren().clear();
        addAiMessage("AI Advisor online for role: " + role);
        startStory();
    }

    @FXML
    private void initialize() {
        userRole = SessionState.getInstance().getUserRole();

        chatHistory.getChildren().clear();
        addAiMessage("Chat started for role: " + userRole);

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

        addAiMessage("Acknowledged: " + text);
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
        addAiMessage("AI Advisor:\nCritical sensor warning...");
        addAiButton("Yes! Dispatch technician", this::handleDecisionYes);
        addAiButton("No! Postpone", () -> addAiMessage("AI: Decision postponed. Monitoring mode."));
    }

    private void handleDecisionYes() {
        clearButtons();
        if (aiState == AiState.SENSOR_WARNING) {
            aiState = AiState.TECHNICIAN_RECOMMENDATION;
            addAiMessage("AI: Qualified technicians identified:\n- Elena Vance\n- Marco Stein\n\nPlease select.");
            addAiButton("Select Elena Vance", this::handleTechnicianSelected);
            addAiButton("Select Marco Stein", this::handleMarcoSelected);
        }
    }

    private void handleMarcoSelected() {
        clearButtons();
        addAiMessage("AI: Marco Stein is currently assigned to thrusters. Assigning him anyway might cause delays.");
        addAiButton("Assign anyway", () -> {
            addAiMessage("AI: Warning: Schedule delay likely. Elena Vance remains recommended.");
        });
        addAiButton("Select Elena Vance instead", this::handleTechnicianSelected);
    }

    private void handleTechnicianSelected() {
        clearButtons();
        aiState = AiState.SCHEDULE_PROPOSED;
        addAiMessage("AI: Elena Vance assigned.\nRecommended schedule: 6 days total.\nAccept?");
        addAiButton("Accept schedule", () -> {
            addAiMessage("AI: Schedule accepted. Maintenance order forwarded.");
            aiState = AiState.FINISHED;
        });
    }

    // --- HILFSMETHODEN FÜR UI ---

    private void addAiButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: #21262d; -fx-text-fill: white; -fx-border-color: #8957e5; -fx-cursor: hand; -fx-padding: 5;");
        btn.setOnAction(e -> action.run());
        chatHistory.getChildren().add(btn);
    }

    private void clearButtons() {
        chatHistory.getChildren().removeIf(n -> n instanceof Button);
    }

}