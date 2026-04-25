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

// Diese Klasse verwaltet den gesamten Zustand des Übergabe-Workflows als Singleton.
// Sie speichert, welche Techniker und Security Chiefs eingetragen sind,
// welche Reparaturaufgaben für jeden Shuttle-Teil anfallen,
// und ob der jeweilige Teil bereits freigegeben wurde.
public class TakeoverState implements IWorkerRegistry, IRepairAccess, IPartApproval, ITakeoverProgress, IPhaseTracker {

    private static final class Holder {
        static final TakeoverState INSTANCE = new TakeoverState();
    }

    // partKey → Zustandsobjekt mit allen Feldern für diesen Teil.
    private final Map<String, PartState> partStates = new HashMap<>();

    // Letzte protokollierte Aktivität – wird im Dashboard angezeigt.
    private String lastActivity = "";

    // ── App-Phase (delegiert an AppPhaseState) ────────────────────────────────

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

    // Interne Schlüssel → Anzeigename der drei Shuttle-Teile.
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

    // ── Mitarbeiter-Registrierung ────────────────────────────────────────────

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

    // ── Techniker-Fertigmeldung ──────────────────────────────────────────────

    public void markTechnicianDone(String partKey, String techName) {
        partStates.get(partKey).setTechnicianDone(true);
        lastActivity = techName + " completed repairs on " + getDisplayName(partKey);
    }

    public boolean isTechnicianDone(String partKey) {
        PartState ps = partStates.get(partKey);
        return ps != null && ps.isTechnicianDone();
    }

    // ── Reparaturaufgaben ────────────────────────────────────────────────────

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

    // ── Freigabe ─────────────────────────────────────────────────────────────

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

    // ── Fortschritt ──────────────────────────────────────────────────────────

    // Jeder freigegebene Teil zählt als gleichwertiger Anteil am Gesamtfortschritt.
    public double getProgress() {
        long approved = partStates.values().stream().filter(PartState::isSecurityApproved).count();
        return approved / (double) partStates.size();
    }

    public boolean isTakeoverComplete() {
        return getProgress() >= 1.0;
    }

    // ── Letzte Aktivität ─────────────────────────────────────────────────────

    public String getLastActivity() {
        return lastActivity;
    }

    // ── Zurücksetzen ─────────────────────────────────────────────────────────

    public void reset() {
        partStates.values().forEach(PartState::reset);
        lastActivity = "";
        phaseState.reset();
    }

    // ── Hilfsmethoden ────────────────────────────────────────────────────────

    public static String getDisplayName(String partKey) {
        return PART_NAMES.getOrDefault(partKey, partKey);
    }
}
