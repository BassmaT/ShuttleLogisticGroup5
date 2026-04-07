package com.group5.shuttle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// Wrapper-Objekt für die flight_history.json-Datei – enthält alle 5 Flugdatensätze
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightHistory {

    // Liste aller historischen Flüge (chronologisch sortiert)
    public List<FlightRecord> flights;

    // Getter für die Flugliste
    public List<FlightRecord> getFlights() { return flights; }
}
