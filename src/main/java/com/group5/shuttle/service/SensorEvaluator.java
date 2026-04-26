package com.group5.shuttle.service;

import com.group5.shuttle.model.SensorStatus;
import com.group5.shuttle.model.SensorThreshold;

public interface SensorEvaluator {
    SensorStatus evaluate(double value, SensorThreshold threshold);
}
