package com.group5.shuttle.service;

import com.group5.shuttle.util.Styles;

// Focused interface for the phase lifecycle and time tracking (SRP).
// Only code that controls or queries the app phase depends on this interface.
public interface IPhaseTracker {

    enum AppPhase { LANDING, SENSOR_LOADING, OPERATIONAL }

    /**
     * OCP: new status → add an entry here.
     * MainController.updateProgressAndSchedule() remains unchanged.
     */
    enum ScheduleStatus {
        ON_TIME      (Styles.ACCENT_SUCCESS, "On Schedule",    "#66ff66"),
        HOURS_BEHIND (Styles.ACCENT_WARNING, "Behind Schedule","#ffcc00"),
        DAY_BEHIND   (Styles.ACCENT_ERROR,   "1+ Day Behind",  "#ff4444");

        private final String progressStyle;
        private final String label;
        private final String color;

        ScheduleStatus(String progressStyle, String label, String color) {
            this.progressStyle = progressStyle;
            this.label         = label;
            this.color         = color;
        }

        public String getProgressStyle() { return progressStyle; }
        public String getLabel()         { return label; }
        public String getColor()         { return color; }
    }

    AppPhase getAppPhase();
    int getRemainingSeconds();
    double getSimulatedHoursElapsed();
    ScheduleStatus getScheduleStatus();

    void beginLanding();
    void beginSensorLoading();
    void setOperational();
    void reset();
}
