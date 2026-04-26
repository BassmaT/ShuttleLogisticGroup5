package com.group5.shuttle.service;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

// Einzelverantwortung: Simuliert die 10-Sekunden-Lieferungsverzögerung für Bestellungen.
// Extrahiert aus MissionControlController und LogisticsController (war dort dupliziert).
public final class OrderDeliveryService {

    public static final int DELIVERY_SECONDS = 10;

    private OrderDeliveryService() {}

    public static void scheduleDelivery(Runnable onDelivered) {
        PauseTransition t = new PauseTransition(Duration.seconds(DELIVERY_SECONDS));
        t.setOnFinished(e -> onDelivered.run());
        t.play();
    }
}
