package com.group5.shuttle.util;

public final class Styles {

    private Styles() {}

    // ── Navigation sidebar buttons ─────────────────────────────────────────────
    public static final String NAV_BTN_HOVER  =
        "-fx-background-color: rgba(75, 156, 255, 0.1); -fx-text-fill: white; " +
        "-fx-min-width: 160; -fx-alignment: BASELINE_LEFT;";
    public static final String NAV_BTN_NORMAL =
        "-fx-background-color: transparent; -fx-text-fill: #8b949e; " +
        "-fx-min-width: 160; -fx-alignment: BASELINE_LEFT;";

    // ── Part-selection buttons (MissionControlController) ─────────────────────
    public static final String PART_BTN_NORMAL =
        "-fx-font-size: 13px; -fx-min-width: 120; -fx-background-color: #3a3a3a; -fx-text-fill: white;";
    public static final String PART_BTN_SELECTED =
        "-fx-font-size: 13px; -fx-min-width: 120; -fx-background-color: #3a7bd5; -fx-text-fill: white; -fx-font-weight: bold;";

    // ── Tables ─────────────────────────────────────────────────────────────────
    public static final String TABLE_DARK =
        "-fx-background: #2a2a2a; -fx-background-color: #2a2a2a;";

    // ── Progress-bar accents ───────────────────────────────────────────────────
    public static final String ACCENT_SUCCESS = "-fx-accent: #66ff66;";
    public static final String ACCENT_WARNING = "-fx-accent: #ffcc00;";
    public static final String ACCENT_ERROR   = "-fx-accent: #ff4444;";

    // ── Dynamic label helpers ──────────────────────────────────────────────────
    public static String label12(String color) {
        return "-fx-text-fill: " + color + "; -fx-font-size: 12px;";
    }

    public static String label13(String color) {
        return "-fx-text-fill: " + color + "; -fx-font-size: 13px;";
    }

    public static String label16(String color) {
        return "-fx-text-fill: " + color + "; -fx-font-size: 16px;";
    }

    public static String labelBold16(String color) {
        return "-fx-text-fill: " + color + "; -fx-font-size: 16px; -fx-font-weight: bold;";
    }
}
