package com.group5.shuttle.service;

import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;
import com.group5.shuttle.model.ShuttlePart;
import com.group5.shuttle.model.SensorStatus;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepairPlanningService {

    private RepairPlanningService() {}

    public static Map<String, List<RepairTask>> plan(
            ShuttleData data,
            Map<String, Map<String, SensorThreshold>> thresholds,
            SensorEvaluator sensorEvaluator,
            IInventoryService inventoryService) {

        Map<String, List<RepairTask>> result = new HashMap<>();
        for (String key : TakeoverState.PART_KEYS) result.put(key, new ArrayList<>());

        if (data == null || thresholds == null) return result;

        Map<String, List<InventoryItem>> inventoryByPart = new HashMap<>();
        for (InventoryItem item : inventoryService.loadInventory()) {
            inventoryByPart.computeIfAbsent(item.getPart(), k -> new ArrayList<>()).add(item);
        }

        Map<String, ShuttlePart> parts = data.getAllParts();

        Map<String, Integer> partIndex = new HashMap<>();

        for (String partKey : TakeoverState.PART_KEYS) {
            ShuttlePart part = parts.get(partKey);
            if (part == null || part.getSensors() == null) continue;

            Map<String, SensorThreshold> pt = thresholds.get(partKey);
            String displayName = TakeoverState.getDisplayName(partKey);
            List<InventoryItem> invItems = inventoryByPart.getOrDefault(displayName, List.of());

            for (var entry : part.getSensors().entrySet()) {
                if (pt == null) continue;
                SensorThreshold t = pt.get(entry.getKey());
                if (t == null) continue;

                SensorStatus evalResult = sensorEvaluator.evaluate(entry.getValue(), t);
                if (evalResult == SensorStatus.WARNING || evalResult == SensorStatus.REPLACE) {
                    String action = evalResult == SensorStatus.REPLACE
                        ? "Replace component" : "Inspect and adjust";
                    RepairTask task = new RepairTask(partKey, entry.getKey(), evalResult, action);
                    if (!invItems.isEmpty()) {
                        int idx = partIndex.getOrDefault(partKey, 0) % invItems.size();
                        task.setRequiredItemName(invItems.get(idx).getName());
                        partIndex.put(partKey, idx + 1);
                    }
                    result.get(partKey).add(task);
                }
            }
        }

        return result;
    }
}
