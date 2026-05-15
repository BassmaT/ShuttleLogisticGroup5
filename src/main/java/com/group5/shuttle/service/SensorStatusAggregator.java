package com.group5.shuttle.service;

import com.group5.shuttle.model.SensorStatus;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;
import com.group5.shuttle.model.ShuttlePart;

import java.util.Map;

// Single responsibility: aggregates the worst sensor status across all shuttle parts.
// Extracted from SensorDataService (was previously mixed with data loading and individual evaluation).
public final class SensorStatusAggregator {

    private SensorStatusAggregator() {}

    public static SensorStatus findWorstSensorStatus(ShuttleData data,
                                                     Map<String, Map<String, SensorThreshold>> thresholds,
                                                     SensorEvaluator evaluator) {
        SensorStatus worst = SensorStatus.OK;
        Map<String, ShuttlePart> parts = data.getAllParts();

        for (var partEntry : parts.entrySet()) {
            ShuttlePart part = partEntry.getValue();
            if (part == null || part.getSensors() == null) continue;
            Map<String, SensorThreshold> pt = thresholds.get(partEntry.getKey());
            for (var sensorEntry : part.getSensors().entrySet()) {
                if (pt == null) continue;
                SensorThreshold t = pt.get(sensorEntry.getKey());
                if (t == null) continue;
                SensorStatus result = evaluator.evaluate(sensorEntry.getValue(), t);
                if (result == SensorStatus.REPLACE) return SensorStatus.REPLACE;
                if (result == SensorStatus.WARNING && worst == SensorStatus.OK) worst = SensorStatus.WARNING;
            }
        }
        return worst;
    }
}
