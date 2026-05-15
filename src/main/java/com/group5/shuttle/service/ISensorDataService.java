package com.group5.shuttle.service;

import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;

import java.util.Map;

// Abstraction for the sensor data service (D – Dependency Inversion).
// Controllers depend on this interface, not on the concrete class.
public interface ISensorDataService extends SensorEvaluator {
    ShuttleData loadSensorData();
    Map<String, Map<String, SensorThreshold>> loadThresholds();
}
