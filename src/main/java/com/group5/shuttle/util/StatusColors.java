package com.group5.shuttle.util;

/**
 * Zentrale Utility-Klasse für Status-Farbkodierungen (DRY-Prinzip).
 * Alle Farben werden hier definiert und in allen Controllern wiederverwendet.
 */
public final class StatusColors {

    private StatusColors() {}

    /** Farbe für Sensor-Status (WARNING, REPLACE, OK) */
    public static String forSensorStatus(String status) {
        return switch (status) {
            case "REPLACE" -> "#ff4444"; // rot
            case "WARNING" -> "#ffcc00"; // gelb
            default        -> "#66ff66"; // grün
        };
    }

    /** Farbe für Lagerbestand-Status (OUT_OF_STOCK, LOW, OK) */
    public static String forStockStatus(String status) {
        return switch (status) {
            case "OUT_OF_STOCK" -> "#ff4444"; // rot
            case "LOW"          -> "#ffcc00"; // gelb
            default             -> "#66ff66"; // grün
        };
    }

    /** Farbe für Bestellungs-Status (DELIVERED, ORDERED, APPROVED, REJECTED, PENDING) */
    public static String forOrderStatus(String status) {
        return switch (status) {
            case "DELIVERED"        -> "#66ff66"; // grün
            case "ORDERED"          -> "#66aaff"; // blau
            case "APPROVED"         -> "#ffcc00"; // gelb
            case "REJECTED"         -> "#ff4444"; // rot
            default                 -> "#aaaaaa"; // grau (PENDING_APPROVAL)
        };
    }
}
