package com.group5.shuttle.model;

import java.util.Map;

// Represents a single historical flight record for trend analysis
public class FlightRecord {

    // Flight designation, e.g. "STS-133"
    private final String flightId;

    // Flight number (1–5) – used for chronological sorting
    private final int flightNumber;

    // Sensor values by shuttle part and sensor name:
    // Key: part (e.g. "orbiter") → sensor (e.g. "hullTemperature") → value
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
