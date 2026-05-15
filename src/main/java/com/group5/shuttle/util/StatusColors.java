package com.group5.shuttle.util;

import com.group5.shuttle.model.SensorStatus;
import com.group5.shuttle.model.StockStatus;
import com.group5.shuttle.model.OrderStatus;

/**
 * Central utility class for status color codes (DRY principle).
 * Colors are defined in the enums – StatusColors only delegates.
 * New status value → only extend the enum, this class remains unchanged (OCP).
 */
public final class StatusColors {

    private StatusColors() {}

    public static String forSensorStatus(SensorStatus status) { return status.getColor(); }
    public static String forSensorStatus(String status)       { return forSensorStatus(SensorStatus.valueOf(status)); }

    public static String forStockStatus(StockStatus status)   { return status.getColor(); }
    public static String forStockStatus(String status) {
        for (StockStatus s : StockStatus.values()) {
            if (status.equals(s.getLabel())) return s.getColor();
        }
        return "#ffffff";
    }

    public static String forOrderStatus(OrderStatus status)   { return status.getColor(); }
    public static String forOrderStatus(String status)        { return forOrderStatus(OrderStatus.valueOf(status)); }

    /** Color for trend urgency (flightsUntilLimit: 0 = immediate, ≤2 = critical, otherwise = monitor) */
    public static String forTrendUrgency(int flightsUntilLimit) {
        if (flightsUntilLimit == 0) return "#ff4444";
        if (flightsUntilLimit <= 2) return "#ff8800";
        return "#ffcc00";
    }

    // OCP: new category → extend ScheduleCategory, these methods remain unchanged.
    public static String forScheduleCategoryBg(String category) { return ScheduleCategory.from(category).getBg(); }
    public static String forScheduleCategoryFg(String category) { return ScheduleCategory.from(category).getFg(); }

    /**
     * Background and foreground colors for a schedule category.
     * OCP: new category type → add an entry here, forScheduleCategoryBg/Fg do not change.
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
