package com.group5.shuttle.model;

import java.util.Map;

// Repräsentiert einen einzelnen historischen Flugdatensatz für die Trendanalyse
public class FlightRecord {

    // Bezeichnung des Fluges, z. B. "STS-133"
    private final String flightId;

    // Flugsnummer (1–5) – wird zur chronologischen Sortierung verwendet
    private final int flightNumber;

    // Sensorwerte nach Shuttle-Teil und Sensor-Name:
    // Schlüssel: Teil (z. B. "orbiter") → Sensor (z. B. "hullTemperature") → Wert
    private final Map<String, Map<String, Double>> sensors;

    public FlightRecord(String flightId, int flightNumber,
                        Map<String, Map<String, Double>> sensors) {
        this.flightId     = flightId;
        this.flightNumber = flightNumber;
        this.sensors      = sensors;
    }

    public String getFlightId()                          { return flightId; }
    public int getFlightNumber()                         { return flightNumber; }
    public Map<String, Map<String, Double>> getSensors() { return sensors; }
}
