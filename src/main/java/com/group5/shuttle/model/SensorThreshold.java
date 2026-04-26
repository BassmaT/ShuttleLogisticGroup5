package com.group5.shuttle.model;

public class SensorThreshold {

    private final Double min;
    private final Double max;
    private final double warningBuffer;

    public SensorThreshold(Double min, Double max) {
        this(min, max, 5.0);
    }

    public SensorThreshold(Double min, Double max, double warningBuffer) {
        this.min           = min;
        this.max           = max;
        this.warningBuffer = warningBuffer;
    }

    public Double getMin()           { return min; }
    public Double getMax()           { return max; }
    public double getWarningBuffer() { return warningBuffer; }
}
