package com.group5.shuttle.service;

import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;

import java.util.List;
import java.util.Map;

// Fokussierte Schnittstelle für Reparaturverwaltung (I – Interface Segregation).
// Nur Controller, die Reparaturen lesen oder steuern, hängen von dieser ab.
public interface IRepairAccess {
    List<RepairTask> getRepairs(String partKey);
    boolean allRepairsDone(String partKey);
    boolean isTechnicianDone(String partKey);
    void markTechnicianDone(String partKey, String techName);
    void generateRepairs(ShuttleData data,
                         Map<String, Map<String, SensorThreshold>> thresholds,
                         SensorEvaluator evaluator);
}
