package com.group5.shuttle.util;

import com.group5.shuttle.model.SensorStatus;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.service.SensorEvaluator;

import java.util.Map;

public final class ShuttleDataHelper {

    private ShuttleDataHelper() {}

    public static SensorStatus evaluateSensor(
            String sensorName,
            double value,
            Map<String, SensorThreshold> partThresholds,
            SensorEvaluator evaluator) {
        if (partThresholds == null) return SensorStatus.OK;
        SensorThreshold t = partThresholds.get(sensorName);
        return t != null ? evaluator.evaluate(value, t) : SensorStatus.OK;
    }
}
