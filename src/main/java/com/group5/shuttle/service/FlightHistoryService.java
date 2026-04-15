package com.group5.shuttle.service;

import com.group5.shuttle.model.FlightHistory;
import com.group5.shuttle.model.FlightRecord;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Verantwortlich für: historische Flugdaten (wird von PredictiveAnalysisService verwendet).
 * Singleton – Flugdaten sind unveränderlich während der Sitzung.
 */
public class FlightHistoryService {

    private static FlightHistoryService instance;

    private FlightHistoryService() {}

    public static FlightHistoryService getInstance() {
        if (instance == null) instance = new FlightHistoryService();
        return instance;
    }

    public FlightHistory loadFlightHistory() {
        FlightHistory history = new FlightHistory();
        history.flights = Arrays.asList(
            flight("STS-133", 1,
                orbiterSensors(480.0, 103.0, 97.0, 4.1),
                srbSensors(1820.0, 640.0, 0.14),
                tankSensors(-155.0, 4.9, 10.0)),
            flight("STS-134", 2,
                orbiterSensors(490.0, 102.0, 97.0, 3.8),
                srbSensors(1815.0, 660.0, 0.16),
                tankSensors(-152.0, 4.85, 13.0)),
            flight("STS-135", 3,
                orbiterSensors(500.0, 101.0, 96.0, 3.4),
                srbSensors(1808.0, 720.0, 0.18),
                tankSensors(-151.0, 4.82, 15.0)),
            flight("STS-136", 4,
                orbiterSensors(510.0, 101.0, 96.0, 3.1),
                srbSensors(1803.0, 760.0, 0.20),
                tankSensors(-150.0, 4.80, 18.0)),
            flight("STS-137", 5,
                orbiterSensors(520.0, 101.0, 96.0, 2.8),
                srbSensors(1800.0, 797.0, 0.22),
                tankSensors(-150.0, 4.80, 22.0))
        );
        return history;
    }

    private FlightRecord flight(String id, int number,
                                Map<String, Double> orbiter,
                                Map<String, Double> srb,
                                Map<String, Double> tank) {
        FlightRecord r = new FlightRecord();
        r.flightId     = id;
        r.flightNumber = number;
        r.sensors      = new HashMap<>();
        r.sensors.put("orbiter",      orbiter);
        r.sensors.put("srb",          srb);
        r.sensors.put("externalTank", tank);
        return r;
    }

    private Map<String, Double> orbiterSensors(double hull, double cabin,
                                               double oxygen, double coolant) {
        Map<String, Double> m = new HashMap<>();
        m.put("hullTemperature", hull);
        m.put("cabinPressure",   cabin);
        m.put("oxygenLevel",     oxygen);
        m.put("coolantPressure", coolant);
        return m;
    }

    private Map<String, Double> srbSensors(double thrust, double casing, double vibration) {
        Map<String, Double> m = new HashMap<>();
        m.put("thrust",            thrust);
        m.put("casingTemperature", casing);
        m.put("vibration",         vibration);
        return m;
    }

    private Map<String, Double> tankSensors(double fuelTemp, double fuelPressure, double stress) {
        Map<String, Double> m = new HashMap<>();
        m.put("fuelTemperature", fuelTemp);
        m.put("fuelPressure",    fuelPressure);
        m.put("stress",          stress);
        return m;
    }
}
