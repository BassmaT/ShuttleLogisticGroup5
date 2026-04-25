package com.group5.shuttle.service;

import java.time.Instant;
import java.util.Map;
import com.group5.shuttle.service.IPhaseTracker.AppPhase;
import com.group5.shuttle.service.IPhaseTracker.ScheduleStatus;

// Einzel-Verantwortung: verwaltet die App-Phase (LANDING → SENSOR_LOADING → OPERATIONAL),
// den Countdown und den Zeitplan-Status.
// Extrahiert aus TakeoverState (war dort gemischt mit Arbeiter-, Reparatur- und Freigabe-Logik).
public class AppPhaseState {

    private AppPhase appPhase = AppPhase.LANDING;

    private Instant landingStartedAt       = null;
    private Instant sensorLoadingStartedAt = null;
    private Instant operationalStartedAt   = null;

    // Simulationsrate: 1 Echtzeit-Sekunde = 2 Sim-Minuten (1 Stunde = 30 Sekunden real)
    private static final double SIM_SECONDS_PER_HOUR = 30.0;

    // Verzugs-Schwellenwerte für Zeitplan-Status
    private static final double LAG_THRESHOLD_DAY_H   = 24.0;
    private static final double LAG_THRESHOLD_HOURS_H = 2.0;

    // Meilensteine: partKey → simulierte Stunden ab Operational-Start
    // OCP: neuen Part hinzufügen → nur hier eintragen, getScheduleStatus() bleibt unverändert.
    private static final Map<String, Double> MILESTONES = Map.of(
        "orbiter",      15.0,
        "srb",          38.0,
        "externalTank", 40.0
    );

    public AppPhaseState() {
        beginLanding();
    }

    public AppPhase getAppPhase() { return appPhase; }

    public void beginLanding() {
        this.appPhase = AppPhase.LANDING;
        this.landingStartedAt = Instant.now();
    }

    public void beginSensorLoading() {
        this.appPhase = AppPhase.SENSOR_LOADING;
        this.sensorLoadingStartedAt = Instant.now();
    }

    public void setOperational() {
        this.appPhase = AppPhase.OPERATIONAL;
        this.operationalStartedAt = Instant.now();
    }

    public int getRemainingSeconds() {
        if (appPhase == AppPhase.LANDING)
            return computeRemaining(landingStartedAt, TakeoverState.LANDING_SECONDS);
        if (appPhase == AppPhase.SENSOR_LOADING)
            return computeRemaining(sensorLoadingStartedAt, TakeoverState.SENSOR_LOADING_SECONDS);
        return 0;
    }

    private int computeRemaining(Instant startedAt, int total) {
        if (startedAt == null) return 0;
        long elapsed = java.time.Duration.between(startedAt, Instant.now()).getSeconds();
        return (int) Math.max(0, total - elapsed);
    }

    public double getSimulatedHoursElapsed() {
        if (operationalStartedAt == null) return 0;
        return java.time.Duration.between(operationalStartedAt, Instant.now()).getSeconds()
               / SIM_SECONDS_PER_HOUR;
    }

    // approval wird von TakeoverState übergeben, damit AppPhaseState keine eigene Freigabe-Logik kennt.
    public ScheduleStatus getScheduleStatus(IPartApproval approval) {
        if (operationalStartedAt == null) return ScheduleStatus.ON_TIME;

        double simHours = getSimulatedHoursElapsed();
        double maxLag = MILESTONES.entrySet().stream()
            .filter(e -> simHours > e.getValue() && !approval.isPartApproved(e.getKey()))
            .mapToDouble(e -> simHours - e.getValue())
            .max().orElse(0);

        if (maxLag >= LAG_THRESHOLD_DAY_H)   return ScheduleStatus.DAY_BEHIND;
        if (maxLag >= LAG_THRESHOLD_HOURS_H) return ScheduleStatus.HOURS_BEHIND;
        return ScheduleStatus.ON_TIME;
    }

    public void reset() {
        operationalStartedAt = null;
    }
}
