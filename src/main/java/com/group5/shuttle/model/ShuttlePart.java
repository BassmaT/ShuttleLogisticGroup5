package com.group5.shuttle.model;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;

// Dieses Modell repräsentiert einen Teil des Shuttles (z. B. Orbiter, SRB, Tank).
// Jeder Teil hat mehrere Sensoren mit ihren aktuellen Messwerten.
public class ShuttlePart {

    // Eine Map speichert Sensorname → Messwert, z. B. "hullTemperature" → 520.0
    // HashMap wird verwendet, weil wir vorher nicht wissen, wie viele Sensoren es gibt.
    private Map<String, Double> sensors = new HashMap<>();

    // @JsonAnySetter bedeutet: Jedes unbekannte JSON-Feld wird hier hineingeschrieben.
    // Wenn Jackson z. B. "hullTemperature": 520 liest, ruft es diese Methode auf.
    // So landen alle Sensorwerte automatisch in der sensors-Map.
    @JsonAnySetter
    public void setSensor(String key, Double value) {
        sensors.put(key, value); // Sensorname als Schlüssel, Messwert als Wert speichern
    }

    // Gibt die komplette Sensor-Map zurück, damit andere Klassen darauf zugreifen können.
    public Map<String, Double> getSensors() {
        return sensors;
    }
}
