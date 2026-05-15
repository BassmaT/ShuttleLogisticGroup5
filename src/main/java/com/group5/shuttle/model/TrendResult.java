package com.group5.shuttle.model;

// Result of the trend analysis for a single sensor over the last 5 flights
public class TrendResult {

    // Key of the shuttle part, e.g. "srb"
    private final String partKey;

    // Name of the sensor, e.g. "casingTemperature"
    private final String sensorName;

    // Average change per flight (positive = rising, negative = falling)
    private final double trendPerFlight;

    // Direction: "RISING", "FALLING" or "STABLE"
    private final String direction;

    // Human-readable recommendation text for display in the dashboard
    private final String recommendation;

    // Estimated number of flights until the limit is reached (-1 = cannot be calculated)
    private final int flightsUntilLimit;

    // Constructor – sets all fields at once
    public TrendResult(String partKey, String sensorName, double trendPerFlight,
                       String direction, String recommendation, int flightsUntilLimit) {
        this.partKey          = partKey;
        this.sensorName       = sensorName;
        this.trendPerFlight   = trendPerFlight;
        this.direction        = direction;
        this.recommendation   = recommendation;
        this.flightsUntilLimit = flightsUntilLimit;
    }

    // Getter methods
    public String getPartKey()         { return partKey; }
    public String getSensorName()      { return sensorName; }
    public double getTrendPerFlight()  { return trendPerFlight; }
    public String getDirection()       { return direction; }
    public String getRecommendation()  { return recommendation; }
    public int getFlightsUntilLimit()  { return flightsUntilLimit; }
}
