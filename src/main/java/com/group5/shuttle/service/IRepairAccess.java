package com.group5.shuttle.service;

import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;

import java.util.List;
import java.util.Map;

// Focused interface for repair management (I – Interface Segregation).
// Only controllers that read or control repairs depend on this interface.
public interface IRepairAccess {
    List<RepairTask> getRepairs(String partKey);
    boolean allRepairsDone(String partKey);
    boolean isTechnicianDone(String partKey);
    void markTechnicianDone(String partKey, String techName);
    void generateRepairs(ShuttleData data,
                         Map<String, Map<String, SensorThreshold>> thresholds,
                         SensorEvaluator evaluator);
}
