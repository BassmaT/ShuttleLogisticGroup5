package com.group5.shuttle.model;

import java.util.Map;

// Repräsentiert einen einzelnen historischen Flugdatensatz für die Trendanalyse
public class FlightRecord {

    // Bezeichnung des Fluges, z. B. "STS-133"
    public String flightId;

    // Flugsnummer (1–5) – wird zur chronologischen Sortierung verwendet
    public int flightNumber;

    // Sensorwerte nach Shuttle-Teil und Sensor-Name:
    // Schlüssel: Teil (z. B. "orbiter") → Sensor (z. B. "hullTemperature") → Wert
    public Map<String, Map<String, Double>> sensors;

    // Getter-Methoden
    public String getFlightId()                          { return flightId; }
    public int getFlightNumber()                         { return flightNumber; }
    public Map<String, Map<String, Double>> getSensors() { return sensors; }
}
