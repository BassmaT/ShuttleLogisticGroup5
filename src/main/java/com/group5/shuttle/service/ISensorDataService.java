package com.group5.shuttle.service;

import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;

import java.util.Map;

// Abstraktion für den Sensordaten-Service (D – Dependency Inversion).
// Controller abhängig von dieser Schnittstelle, nicht von der konkreten Klasse.
public interface ISensorDataService extends SensorEvaluator {
    ShuttleData loadSensorData();
    Map<String, Map<String, SensorThreshold>> loadThresholds();
}
