package com.group5.shuttle.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;

import java.io.InputStream;
import java.util.Map;

public class SensorService {

    private final ObjectMapper mapper = new ObjectMapper();

    // Lädt die aktuellen Sensordaten aus sensors.json
    public ShuttleData loadSensorData() {
        try {
            InputStream is = getClass().getResourceAsStream("/data/sensors.json");
            return mapper.readValue(is, ShuttleData.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Lädt die Grenzwerte aus thresholds.json
    public Map<String, Map<String, SensorThreshold>> loadThresholds() {
        try {
            InputStream is = getClass().getResourceAsStream("/data/thresholds.json");
            return mapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Bewertet einen Sensorwert anhand seiner Grenzwerte
    public String evaluate(double value, SensorThreshold t) {

        // Harte Grenzverletzung → Teil muss ersetzt werden
        if (t.min != null && value < t.min) return "REPLACE";
        if (t.max != null && value > t.max) return "REPLACE";

        // Leichte Abweichung → Warnung
        if (t.min != null && value < t.min + 5) return "WARNING";
        if (t.max != null && value > t.max - 5) return "WARNING";

        // Alles im grünen Bereich
        return "OK";
    }
}
