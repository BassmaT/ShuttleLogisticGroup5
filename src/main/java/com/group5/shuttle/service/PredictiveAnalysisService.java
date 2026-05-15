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

// Analyzes historical flight data and calculates trend predictions for each sensor.
// Returns TrendResult objects that are displayed as warnings on the dashboard.
// Not a singleton – this class has no mutable state.
public class PredictiveAnalysisService implements IPredictiveAnalysisService {

    private final IFlightHistoryService flightHistoryService = FlightHistoryService.getInstance();
    private final ISensorDataService    sensorDataService    = SensorDataService.getInstance();

    public Map<String, List<TrendResult>> analyzeAll() {
        FlightHistory history = flightHistoryService.loadFlightHistory();
        Map<String, Map<String, SensorThreshold>> thresholds = sensorDataService.loadThresholds();
        Map<String, List<TrendResult>> result = new LinkedHashMap<>();

        // No analysis possible without flight data
        if (history == null || history.getFlights() == null) return result;

        // Sort flights chronologically (ascending by flight number)
        List<FlightRecord> flights = new ArrayList<>(history.getFlights());
        flights.sort(Comparator.comparingInt(FlightRecord::getFlightNumber));

        // Analyze each shuttle part
        for (String partKey : TakeoverState.PART_KEYS) {
            List<TrendResult> partTrends = new ArrayList<>();

            // Determine sensor names from the first flight data record
            Map<String, Double> firstSensors = flights.get(0).getSensors().get(partKey);
            if (firstSensors == null) continue;

            for (String sensorName : firstSensors.keySet()) {
                // Collect readings across all flights for this sensor
                List<Double> values = new ArrayList<>();
                for (FlightRecord f : flights) {
                    Map<String, Double> partSensors = f.getSensors().get(partKey);
                    if (partSensors != null && partSensors.containsKey(sensorName)) {
                        values.add(partSensors.get(sensorName));
                    }
                }
                // At least two data points are required for a trend calculation
                if (values.size() < 2) continue;

                // Linear trend: (last value − first value) / (number of intervals)
                double delta = values.get(values.size() - 1) - values.get(0);
                double trendPerFlight = delta / (values.size() - 1);

                // Determine trend direction
                String direction;
                if (Math.abs(trendPerFlight) < 0.01) direction = "STABLE";
                else if (trendPerFlight > 0)          direction = "RISING";
                else                                   direction = "FALLING";

                // Retrieve the threshold for this sensor (may be null)
                SensorThreshold threshold = null;
                if (thresholds != null && thresholds.get(partKey) != null) {
                    threshold = thresholds.get(partKey).get(sensorName);
                }

                // Current value = last known reading
                double currentValue = values.get(values.size() - 1);

                // Calculate flights until the threshold is reached
                int flightsUntilLimit = TrendCalculator.computeFlightsUntilLimit(currentValue, trendPerFlight, threshold);

                // Generate a human-readable recommendation text
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
