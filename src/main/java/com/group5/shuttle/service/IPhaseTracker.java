package com.group5.shuttle.service;

import com.group5.shuttle.util.Styles;

// Fokussierte Schnittstelle für den Phase-Lifecycle und die Zeitmessung (SRP).
// Nur Code, der die App-Phase steuert oder abfragt, hängt von dieser ab.
public interface IPhaseTracker {

    enum AppPhase { LANDING, SENSOR_LOADING, OPERATIONAL }

    /**
     * OCP: neuer Status → hier einen Eintrag ergänzen.
     * MainController.updateProgressAndSchedule() bleibt unverändert.
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

