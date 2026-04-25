package com.group5.shuttle.controller;

import com.group5.shuttle.service.AiAdvisorService;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Einzelverantwortung: Steuerung des AI-Chat-Panels (Nachrichten anzeigen, Eingabe verarbeiten, Panel ein-/ausblenden).
public class AiChatController {

    private final VBox       chatHistory;
    private final TextField  inputField;
    private final StackPane  drawer;

    public AiChatController(VBox chatHistory, TextField inputField, StackPane drawer) {
        this.chatHistory = chatHistory;
        this.inputField  = inputField;
        this.drawer      = drawer;
    }

    public void initialize() {
        addMessage("AI Advisor: Systems online. Standing by for landing data.");
        inputField.setOnAction(e -> handleInput());
    }

    public void toggle() {
        if (drawer == null) return;
        boolean visible = drawer.isVisible();
        drawer.setVisible(!visible);
        drawer.setManaged(!visible);
    }

    public void handleInput() {
        String message = inputField.getText();
        if (message != null && !message.isBlank()) {
            addMessage("You: " + message);
            addMessage(AiAdvisorService.getResponse(message));
            inputField.clear();
        }
    }

    private void addMessage(String text) {
        Label label = new Label(text);
        boolean isUser = text.startsWith("You:");
        label.setStyle(isUser
            ? "-fx-background-color: #21262d; -fx-text-fill: #E6EDF3; -fx-padding: 8 12; -fx-background-radius: 10;"
            : "-fx-background-color: #161B22; -fx-text-fill: #8957e5; -fx-padding: 8 12;"
              + " -fx-background-radius: 10; -fx-border-color: #8957e5; -fx-border-width: 0.5;");
        label.setWrapText(true);
        label.setMaxWidth(280);
        chatHistory.getChildren().add(label);
    }
}
