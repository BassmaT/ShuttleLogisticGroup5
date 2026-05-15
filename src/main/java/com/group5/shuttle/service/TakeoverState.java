package com.group5.shuttle.service;

import com.group5.shuttle.model.PartState;
import com.group5.shuttle.model.RepairTask;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// This class manages the entire state of the takeover workflow as a singleton.
// It stores which technicians and security chiefs are registered,
// which repair tasks are required for each shuttle part,
// and whether the respective part has already been approved.
public class TakeoverState implements IWorkerRegistry, IRepairAccess, IPartApproval, ITakeoverProgress, IPhaseTracker {

    private static final class Holder {
        static final TakeoverState INSTANCE = new TakeoverState();
    }

    // partKey → state object containing all fields for this part.
    private final Map<String, PartState> partStates = new HashMap<>();

    // Last recorded activity – displayed on the dashboard.
    private String lastActivity = "";

    // ── App phase (delegated to AppPhaseState) ────────────────────────────────

    private final AppPhaseState phaseState = new AppPhaseState();

    public static final int LANDING_SECONDS        = 15;
    public static final int SENSOR_LOADING_SECONDS = 10;

    public AppPhase getAppPhase()                { return phaseState.getAppPhase(); }
    public void beginLanding()                   { phaseState.beginLanding(); }
    public void beginSensorLoading()             { phaseState.beginSensorLoading(); }
    public void setOperational()                 { phaseState.setOperational(); }
    public int  getRemainingSeconds()            { return phaseState.getRemainingSeconds(); }
    public double getSimulatedHoursElapsed()     { return phaseState.getSimulatedHoursElapsed(); }
    public ScheduleStatus getScheduleStatus()    { return phaseState.getScheduleStatus(this); }

    // Internal keys → display names of the three shuttle parts.
    private static final LinkedHashMap<String, String> PART_NAMES = new LinkedHashMap<>();
    static {
        PART_NAMES.put("orbiter",      "Orbiter");
        PART_NAMES.put("srb",          "SRB");
        PART_NAMES.put("externalTank", "External Tank");
    }
    public static final String[] PART_KEYS = PART_NAMES.keySet().toArray(new String[0]);

    public static final String ROLE_TECHNICIAN     = "Technician";
    public static final String ROLE_SECURITY_CHIEF = "Security Chief";

    private TakeoverState() {
        for (String key : PART_KEYS) partStates.put(key, new PartState());
        beginLanding();
    }

    public static TakeoverState getInstance() {
        return Holder.INSTANCE;
    }

    // ── Employee registration ────────────────────────────────────────────────

    public void registerWorker(String partKey, String name, String role) {
        PartState ps = partStates.get(partKey);
        if (ROLE_TECHNICIAN.equals(role)) {
            ps.setTechnicianName(name);
        } else {
            ps.setSecurityChiefName(name);
        }
    }

    public String getWorkerInfo(String partKey) {
        PartState ps    = partStates.get(partKey);
        String    tech  = ps.getTechnicianName();
        String    chief = ps.getSecurityChiefName();
        if (tech != null && chief != null)
            return tech + " (Tech) / " + chief + " (Chief)";
        if (tech  != null) return tech  + " (Technician)";
        if (chief != null) return chief + " (Security Chief)";
        return null;
    }

    public String getTechnicianName(String partKey) {
        return partStates.get(partKey).getTechnicianName();
    }

    public String getSecurityChiefName(String partKey) {
        return partStates.get(partKey).getSecurityChiefName();
    }

    // ── Technician completion sign-off ──────────────────────────────────────────────

    public void markTechnicianDone(String partKey, String techName) {
        partStates.get(partKey).setTechnicianDone(true);
        lastActivity = techName + " completed repairs on " + getDisplayName(partKey);
    }

    public boolean isTechnicianDone(String partKey) {
        PartState ps = partStates.get(partKey);
        return ps != null && ps.isTechnicianDone();
    }

    // ── Repair tasks ────────────────────────────────────────────────────────

    public void generateRepairs(ShuttleData data, Map<String, Map<String, SensorThreshold>> thresholds,
                                SensorEvaluator sensorEvaluator) {
        for (String key : PART_KEYS) partStates.get(key).getRepairs().clear();
        RepairPlanningService.plan(data, thresholds, sensorEvaluator, InventoryService.getInstance())
                             .forEach((key, list) -> partStates.get(key).getRepairs().addAll(list));
    }

    public List<RepairTask> getRepairs(String partKey) {
        PartState ps = partStates.get(partKey);
        return ps != null ? ps.getRepairs() : new ArrayList<>();
    }

    public boolean allRepairsDone(String partKey) {
        PartState ps = partStates.get(partKey);
        List<RepairTask> tasks = ps != null ? ps.getRepairs() : null;
        if (tasks == null || tasks.isEmpty()) return true;
        return tasks.stream().allMatch(RepairTask::isDone);
    }

    // ── Approval ─────────────────────────────────────────────────────────────

    public boolean canApprove(String partKey) {
        return isTechnicianDone(partKey) && !isPartApproved(partKey);
    }

    public void approve(String partKey, String chiefName) {
        partStates.get(partKey).setSecurityApproved(true);
        lastActivity = chiefName + " approved " + getDisplayName(partKey);
    }

    public boolean isPartApproved(String partKey) {
        PartState ps = partStates.get(partKey);
        return ps != null && ps.isSecurityApproved();
    }

    // ── Progress ──────────────────────────────────────────────────────────────

    // Each approved part counts as an equal share of the overall progress.
    public double getProgress() {
        long approved = partStates.values().stream().filter(PartState::isSecurityApproved).count();
        return approved / (double) partStates.size();
    }

    public boolean isTakeoverComplete() {
        return getProgress() >= 1.0;
    }

    // ── Last activity ─────────────────────────────────────────────────────────

    public String getLastActivity() {
        return lastActivity;
    }

    // ── Reset ─────────────────────────────────────────────────────────────────

    public void reset() {
        partStates.values().forEach(PartState::reset);
        lastActivity = "";
        phaseState.reset();
    }

    // ── Helper methods ────────────────────────────────────────────────────────

    public static String getDisplayName(String partKey) {
        return PART_NAMES.getOrDefault(partKey, partKey);
    }
}
