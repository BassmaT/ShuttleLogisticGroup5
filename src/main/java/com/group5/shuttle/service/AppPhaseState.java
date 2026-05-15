package com.group5.shuttle.service;

import java.time.Instant;
import java.util.Map;
import com.group5.shuttle.service.IPhaseTracker.AppPhase;
import com.group5.shuttle.service.IPhaseTracker.ScheduleStatus;

// Single responsibility: manages the app phase (LANDING → SENSOR_LOADING → OPERATIONAL),
// the countdown, and the schedule status.
// Extracted from TakeoverState (was previously mixed with worker, repair, and approval logic).
public class AppPhaseState {

    private AppPhase appPhase = AppPhase.LANDING;

    private Instant landingStartedAt       = null;
    private Instant sensorLoadingStartedAt = null;
    private Instant operationalStartedAt   = null;

    // Simulation rate: 1 real-time second = 2 simulated minutes (1 hour = 30 real seconds)
    private static final double SIM_SECONDS_PER_HOUR = 30.0;

    // Delay thresholds for schedule status
    private static final double LAG_THRESHOLD_DAY_H   = 24.0;
    private static final double LAG_THRESHOLD_HOURS_H = 2.0;

    // Milestones: partKey → simulated hours from operational start
    // OCP: adding a new part → only register it here, getScheduleStatus() remains unchanged.
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

    // approval is passed in from TakeoverState so that AppPhaseState does not need its own approval logic.
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
