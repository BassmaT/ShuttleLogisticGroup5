package com.group5.shuttle.model;

import java.util.HashMap;
import java.util.Map;

// This model represents a part of the shuttle (e.g. Orbiter, SRB, Tank).
// Each part has multiple sensors with their current measurement values.
public class ShuttlePart {

    // A map stores sensor name → measurement value, e.g. "hullTemperature" → 520.0
    private Map<String, Double> sensors = new HashMap<>();

    // Adds a sensor value to the map.
    public void setSensor(String key, Double value) {
        sensors.put(key, value); // Store sensor name as key, measurement value as value
    }

    // Returns the complete sensor map so that other classes can access it.
    public Map<String, Double> getSensors() {
        return sensors;
    }
}
