package com.group5.shuttle.util;

import com.group5.shuttle.model.SensorStatus;
import com.group5.shuttle.model.StockStatus;
import com.group5.shuttle.model.OrderStatus;

/**
 * Zentrale Utility-Klasse für Status-Farbkodierungen (DRY-Prinzip).
 * Farben sind in den Enums definiert – StatusColors delegiert nur noch.
 * Neuer Status-Wert → nur das Enum erweitern, diese Klasse bleibt unverändert (OCP).
 */
public final class StatusColors {

    private StatusColors() {}

    public static String forSensorStatus(SensorStatus status) { return status.getColor(); }
    public static String forSensorStatus(String status)       { return forSensorStatus(SensorStatus.valueOf(status)); }

    public static String forStockStatus(StockStatus status)   { return status.getColor(); }
    public static String forStockStatus(String status)        { return forStockStatus(StockStatus.valueOf(status)); }

    public static String forOrderStatus(OrderStatus status)   { return status.getColor(); }
    public static String forOrderStatus(String status)        { return forOrderStatus(OrderStatus.valueOf(status)); }

    /** Farbe für Trend-Dringlichkeit (flightsUntilLimit: 0 = sofort, ≤2 = kritisch, sonst = beobachten) */
    public static String forTrendUrgency(int flightsUntilLimit) {
        if (flightsUntilLimit == 0) return "#ff4444";
        if (flightsUntilLimit <= 2) return "#ff8800";
        return "#ffcc00";
    }

    // OCP: neue Kategorie → ScheduleCategory erweitern, diese Methoden bleiben unverändert.
    public static String forScheduleCategoryBg(String category) { return ScheduleCategory.from(category).getBg(); }
    public static String forScheduleCategoryFg(String category) { return ScheduleCategory.from(category).getFg(); }

    /**
     * Hintergrund- und Vordergrundfarben für eine Schedule-Kategorie.
     * OCP: neuer Kategorie-Typ → hier einen Eintrag ergänzen, forScheduleCategoryBg/Fg ändern sich nicht.
     */
    public enum ScheduleCategory {
        REPAIR    ("repair",     "#3a1a1a", "#ff6666"),
        APPROVAL  ("approval",   "#1a3a1a", "#66ff66"),
        ROUTINE   ("routine",    "#3a3a1a", "#ffcc00"),
        DIAGNOSTIC("diagnostic", "#1a2a3a", "#66aaff"),
        BREAK     ("break",      "#2a2a2a", "#aaaaaa"),
        DEFAULT   ("",           "#252525", "#aaaaaa");

        private final String key;
        private final String bg;
        private final String fg;

        ScheduleCategory(String key, String bg, String fg) {
            this.key = key; this.bg = bg; this.fg = fg;
        }

        public String getBg() { return bg; }
        public String getFg() { return fg; }

        public static ScheduleCategory from(String category) {
            if (category == null) return DEFAULT;
            for (ScheduleCategory c : values())
                if (c.key.equals(category)) return c;
            return DEFAULT;
        }
    }
}

