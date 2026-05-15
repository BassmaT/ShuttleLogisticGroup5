package com.group5.shuttle.service;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

// Single responsibility: simulates the 10-second delivery delay for orders.
// Extracted from MissionControlController and LogisticsController (was duplicated there).
public final class OrderDeliveryService {

    public static final int DELIVERY_SECONDS = 10;

    private OrderDeliveryService() {}

    public static void scheduleDelivery(Runnable onDelivered) {
        PauseTransition t = new PauseTransition(Duration.seconds(DELIVERY_SECONDS));
        t.setOnFinished(e -> onDelivered.run());
        t.play();
    }
}
