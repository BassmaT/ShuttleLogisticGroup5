package com.group5.shuttle.model;

import java.util.List;

// Wrapper-Objekt für alle historischen Flugdatensätze
public class FlightHistory {

    // Liste aller historischen Flüge (chronologisch sortiert)
    public List<FlightRecord> flights;

    // Getter für die Flugliste
    public List<FlightRecord> getFlights() { return flights; }
}
