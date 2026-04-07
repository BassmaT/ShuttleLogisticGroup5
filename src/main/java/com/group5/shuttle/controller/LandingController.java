package com.group5.shuttle.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;

// Controller für den Landing-Screen.
// Zeigt einen 15-Sekunden-Countdown (simuliert 15 Minuten Landeanflug).
// Nach Ablauf erscheint die Erfolgsmeldung; 3 Sekunden später wechselt die App
// automatisch zum Haupt-Dashboard (main_view.fxml).
public class LandingController {

    // Simulierte Gesamtdauer in Sekunden (= 15 Minuten in Echtzeit)
    private static final int TOTAL_SECONDS = 15;

    // Countdown-Label, z. B. "Approaching in 14:30 min..."
    @FXML private Label lblCountdown;

    // Fortschrittsbalken: füllt sich von 0.0 auf 1.0 während des Countdowns
    @FXML private ProgressBar progressLanding;

    // Status-Label unter dem Fortschrittsbalken
    @FXML private Label lblStatus;

    // Verbleibende Sekunden bis zur Landung
    private int secondsLeft = TOTAL_SECONDS;

    // Wird automatisch aufgerufen, sobald die FXML geladen ist
    @FXML
    public void initialize() {
        startCountdown();
    }

    // Startet den Timeline-basierten Countdown (1 Tick pro Sekunde)
    private void startCountdown() {
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), e -> tick())
        );
        timeline.setCycleCount(TOTAL_SECONDS); // genau TOTAL_SECONDS mal ausführen
        timeline.setOnFinished(e -> onLanded()); // nach dem letzten Tick: Landung
        timeline.play();
    }

    // Wird jede Sekunde aufgerufen – aktualisiert Countdown und Fortschrittsbalken
    private void tick() {
        secondsLeft--;

        // Fortschritt berechnen (0.0 = Start, 1.0 = gelandet)
        double progress = 1.0 - (double) secondsLeft / TOTAL_SECONDS;
        progressLanding.setProgress(progress);

        // Simulierte Minuten anzeigen (15s = 15min, jede Sekunde = 1 Minute weniger)
        int minutesLeft = secondsLeft; // 1 Sekunde simuliert 1 Minute
        lblCountdown.setText(String.format("Approaching in %02d:00 min...", minutesLeft));

        // Status-Text je nach Fortschritt anpassen
        if (progress < 0.4) {
            lblStatus.setText("Shuttle on approach – systems nominal");
        } else if (progress < 0.75) {
            lblStatus.setText("Initiating landing sequence...");
        } else {
            lblStatus.setText("Final approach – deploying landing gear...");
        }
    }

    // Wird aufgerufen, wenn der Countdown abgelaufen ist (Landung erfolgt)
    private void onLanded() {
        // Balken voll auffüllen
        progressLanding.setProgress(1.0);
        progressLanding.setStyle("-fx-accent: #66ff66;");

        // Erfolgsmeldung anzeigen
        lblCountdown.setText("Shuttle landed successfully.");
        lblCountdown.setStyle("-fx-text-fill: #66ff66; -fx-font-size: 22px; -fx-font-weight: bold;");
        lblStatus.setText("Takeover process is starting soon...");
        lblStatus.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14px;");

        // 3 Sekunden warten, dann automatisch zum Haupt-Dashboard wechseln
        Timeline delay = new Timeline(
            new KeyFrame(Duration.seconds(3), e -> loadDashboard())
        );
        delay.play();
    }

    // Lädt das Haupt-Dashboard und ersetzt den Landing-Screen
    private void loadDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml"));
            Parent root = loader.load();
            // Stage über das aktuelle Node-Objekt holen
            Stage stage = (Stage) progressLanding.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
