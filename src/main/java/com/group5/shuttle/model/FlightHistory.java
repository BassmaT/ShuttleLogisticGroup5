package com.group5.shuttle.model;

import java.util.List;

// Wrapper-Objekt für alle historischen Flugdatensätze
public class FlightHistory {

    // Liste aller historischen Flüge (chronologisch sortiert)
    private final List<FlightRecord> flights;

    public FlightHistory(List<FlightRecord> flights) {
        this.flights = flights;
    }

    public List<FlightRecord> getFlights() { return flights; }
}
