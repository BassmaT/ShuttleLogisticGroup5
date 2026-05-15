package com.group5.shuttle.model;

import java.util.List;

// Wrapper object for all historical flight records
public class FlightHistory {

    // List of all historical flights (sorted chronologically)
    private final List<FlightRecord> flights;

    public FlightHistory(List<FlightRecord> flights) {
        this.flights = flights;
    }

    public List<FlightRecord> getFlights() { return flights; }
}
