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
public class PredictiveAnalysisService {

    // Fokussierte Services werden per Singleton bezogen (DIP-konform)
    private final FlightHistoryService flightHistoryService = FlightHistoryService.getInstance();
    private final SensorDataService sensorDataService = SensorDataService.getInstance();

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
                int flightsUntilLimit = computeFlightsUntilLimit(currentValue, trendPerFlight, threshold);

                // Lesbaren Empfehlungstext erzeugen
                String recommendation = buildRecommendation(
                    sensorName, trendPerFlight, direction, flightsUntilLimit);

                partTrends.add(new TrendResult(
                    partKey, sensorName, trendPerFlight, direction, recommendation, flightsUntilLimit));
            }
            result.put(partKey, partTrends);
        }
        return result;
    }

    // Berechnet, wie viele Flüge es noch dauert, bis der Grenzwert erreicht wird.
    // Gibt -1 zurück, wenn keine Berechnung möglich ist (kein Trend oder kein Grenzwert) KI
    private int computeFlightsUntilLimit(double current, double trend, SensorThreshold t) {
        if (t == null || Math.abs(trend) < 0.001) return -1;

        // Steigender Trend → nähert sich dem Maximum
        if (trend > 0 && t.getMax() != null) {
            double remaining = t.getMax() - current;
            if (remaining <= 0) return 0; // bereits überschritten
            return (int) Math.ceil(remaining / trend);
        }
        // Fallender Trend → nähert sich dem Minimum
        if (trend < 0 && t.getMin() != null) {
            double remaining = current - t.getMin();
            if (remaining <= 0) return 0; // bereits unterschritten
            return (int) Math.ceil(remaining / (-trend));
        }
        return -1; // keine passende Grenzwert-Richtung vorhanden
    }

    // Erzeugt einen verständlichen  Empfehlungstext für die Anzeige im Dashboard mit HILFE von KI erstellt 
    private String buildRecommendation(String sensor, double trend, String direction, int flights) {
        String trendStr = String.format("%.2f/Flight", Math.abs(trend));

        if (direction.equals("STABLE")) {
            return sensor + " is stable – no action required.";
        }

        // Richtungsangabe 
        String dirStr = direction.equals("RISING") ? "rising" : "decreases";
        String base = sensor + " " + dirStr + " ~" + trendStr;

        if (flights == 0) {
            return base + ". THRESHOLD REACHED – immediate replacement recommended!";
        }
        if (flights > 0 && flights <= 3) {
            return base + ". Replacement in " + flights + " flights recommended.";
        }
        if (flights > 0) {
            return base + ". Monitor – Threshold expected to be reached at " + flights + " flights.";
        }
        return base + ".";
    }
}
