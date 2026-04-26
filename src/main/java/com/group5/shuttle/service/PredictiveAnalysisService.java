package com.group5.shuttle.service;

import com.group5.shuttle.model.FlightHistory;
import com.group5.shuttle.model.FlightRecord;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.TrendResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Analysiert historische Flugdaten und berechnet Trendvorhersagen für jeden Sensor.
// Gibt TrendResult-Objekte zurück, die im Dashboard als Warnungen angezeigt werden.
// Kein Singleton – diese Klasse hat keinen veränderbaren Zustand.
public class PredictiveAnalysisService implements IPredictiveAnalysisService {

    private final IFlightHistoryService flightHistoryService = FlightHistoryService.getInstance();
    private final ISensorDataService    sensorDataService    = SensorDataService.getInstance();

    public Map<String, List<TrendResult>> analyzeAll() {
        FlightHistory history = flightHistoryService.loadFlightHistory();
        Map<String, Map<String, SensorThreshold>> thresholds = sensorDataService.loadThresholds();
        Map<String, List<TrendResult>> result = new LinkedHashMap<>();

        // Ohne Flugdaten ist keine Analyse möglich
        if (history == null || history.getFlights() == null) return result;

        // Flüge chronologisch sortieren (nach Flugnummer aufsteigend)
        List<FlightRecord> flights = new ArrayList<>(history.getFlights());
        flights.sort(Comparator.comparingInt(FlightRecord::getFlightNumber));

        // Jeden Shuttle-Teil analysieren KI 
        for (String partKey : TakeoverState.PART_KEYS) {
            List<TrendResult> partTrends = new ArrayList<>();

            // Sensor-Namen aus dem ersten Flugdatensatz ermitteln
            Map<String, Double> firstSensors = flights.get(0).getSensors().get(partKey);
            if (firstSensors == null) continue;

            for (String sensorName : firstSensors.keySet()) {
                // Messwerte über alle Flüge für diesen Sensor sammeln
                List<Double> values = new ArrayList<>();
                for (FlightRecord f : flights) {
                    Map<String, Double> partSensors = f.getSensors().get(partKey);
                    if (partSensors != null && partSensors.containsKey(sensorName)) {
                        values.add(partSensors.get(sensorName));
                    }
                }
                // Mindestens zwei Messpunkte sind für eine Trendberechnung erforderlich
                if (values.size() < 2) continue;

                // Linearer Trend: (letzter Wert − erster Wert) / (Anzahl Intervalle)
                double delta = values.get(values.size() - 1) - values.get(0);
                double trendPerFlight = delta / (values.size() - 1);

                // Trendrichtung bestimmen
                String direction;
                if (Math.abs(trendPerFlight) < 0.01) direction = "STABLE";
                else if (trendPerFlight > 0)          direction = "RISING";
                else                                   direction = "FALLING";

                // Grenzwert für diesen Sensor ermitteln (kann null sein)
                SensorThreshold threshold = null;
                if (thresholds != null && thresholds.get(partKey) != null) {
                    threshold = thresholds.get(partKey).get(sensorName);
                }

                // Aktueller Wert = letzter bekannter Messwert
                double currentValue = values.get(values.size() - 1);

                // Flüge bis zum Grenzwert berechnen
                int flightsUntilLimit = TrendCalculator.computeFlightsUntilLimit(currentValue, trendPerFlight, threshold);

                // Lesbaren Empfehlungstext erzeugen
                String recommendation = TrendCalculator.buildRecommendation(
                    sensorName, trendPerFlight, direction, flightsUntilLimit);

                partTrends.add(new TrendResult(
                    partKey, sensorName, trendPerFlight, direction, recommendation, flightsUntilLimit));
            }
            result.put(partKey, partTrends);
        }
        return result;
    }
}

