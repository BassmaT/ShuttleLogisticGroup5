package com.group5.shuttle.service;

import com.group5.shuttle.model.SensorThreshold;

// Single responsibility: calculation of trend values and recommendation texts.
// Extracted from PredictiveAnalysisService (was previously mixed with data-flow logic).
public final class TrendCalculator {

    private TrendCalculator() {}

    // Calculates how many flights remain until the threshold is reached.
    // Returns -1 if no calculation is possible.
    public static int computeFlightsUntilLimit(double current, double trend, SensorThreshold t) {
        if (t == null || Math.abs(trend) < 0.001) return -1;

        if (trend > 0 && t.getMax() != null) {
            double remaining = t.getMax() - current;
            if (remaining <= 0) return 0;
            return (int) Math.ceil(remaining / trend);
        }
        if (trend < 0 && t.getMin() != null) {
            double remaining = current - t.getMin();
            if (remaining <= 0) return 0;
            return (int) Math.ceil(remaining / (-trend));
        }
        return -1;
    }

    // Generates a human-readable recommendation text for display on the dashboard.
    public static String buildRecommendation(String sensor, double trend, String direction, int flights) {
        String trendStr = String.format("%.2f/Flight", Math.abs(trend));

        if (direction.equals("STABLE")) {
            return sensor + " is stable – no action required.";
        }

        String dirStr = direction.equals("RISING") ? "rising" : "decreases";
        String base = sensor + " " + dirStr + " ~" + trendStr;

        if (flights == 0) {
            return base + ". THRESHOLD REACHED – immediate replacement recommended!";
        }
        if (flights > 0 && flights <= 3) {
            return base + ". Replacement in " + flights + " flights recommended.";
        }
        if (flights > 0) {
            return base + ". Monitor – Threshold expected to be reached at " + flights + " flights.";
        }
        return base + ".";
    }
}
