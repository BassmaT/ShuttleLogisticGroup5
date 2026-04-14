package com.group5.shuttle.model;

import java.util.HashMap;
import java.util.Map;

// Dieses Modell repräsentiert einen Teil des Shuttles (z. B. Orbiter, SRB, Tank).
// Jeder Teil hat mehrere Sensoren mit ihren aktuellen Messwerten.
public class ShuttlePart {

    // Eine Map speichert Sensorname → Messwert, z. B. "hullTemperature" → 520.0
    private Map<String, Double> sensors = new HashMap<>();

    // Fügt einen Sensorwert zur Map hinzu.
    public void setSensor(String key, Double value) {
        sensors.put(key, value); // Sensorname als Schlüssel, Messwert als Wert speichern
    }

    // Gibt die komplette Sensor-Map zurück, damit andere Klassen darauf zugreifen können.
    public Map<String, Double> getSensors() {
        return sensors;
    }
}
