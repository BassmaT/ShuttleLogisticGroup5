package com.group5.shuttle.service;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

// Singleton: manages the phase countdown timers so they survive controller navigation.
public class PhaseTimerService {

    private static final class Holder {
        static final PhaseTimerService INSTANCE = new PhaseTimerService();
    }

    private Timeline activeTimer;

    private PhaseTimerService() {}

    public static PhaseTimerService getInstance() {
        return Holder.INSTANCE;
    }

    public void start(int totalSeconds, Consumer<Integer> onTick, Runnable onComplete) {
        stop();
        AtomicInteger remaining = new AtomicInteger(totalSeconds);
        activeTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int r = remaining.decrementAndGet();
            onTick.accept(r);
        }));
        activeTimer.setCycleCount(totalSeconds);
        activeTimer.setOnFinished(e -> { activeTimer = null; onComplete.run(); });
        activeTimer.play();
    }

    public void stop() {
        if (activeTimer != null) { activeTimer.stop(); activeTimer = null; }
    }
}
