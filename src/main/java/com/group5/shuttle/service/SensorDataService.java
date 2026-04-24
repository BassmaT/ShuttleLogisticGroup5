package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.EmployeeRoster;
import com.group5.shuttle.model.EmployeeTeam;
import com.group5.shuttle.model.RoutineTask;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Verantwortlich für: Sensordaten, Schwellwerte, Sensorauswertung,
 * Mitarbeiterdaten und Routineaufgaben.
 * Singleton – wird einmalig pro Sitzung instanziiert.
 */
public class SensorDataService {

    private static SensorDataService instance;

    private SensorDataService() {}

    public static SensorDataService getInstance() {
        if (instance == null) instance = new SensorDataService();
        return instance;
    }

    // ── Sensordaten ───────────────────────────────────────────────────────────

    public ShuttleData loadSensorData() {
        ShuttleData data = new ShuttleData();

        data.setOrbiter(new com.group5.shuttle.model.ShuttlePart());
        data.getOrbiter().setSensor("hullTemperature", 520.0);
        data.getOrbiter().setSensor("cabinPressure",   101.0);
        data.getOrbiter().setSensor("oxygenLevel",      96.0);
        data.getOrbiter().setSensor("coolantPressure",   2.8);

        data.setSrb(new com.group5.shuttle.model.ShuttlePart());
        data.getSrb().setSensor("thrust",            1800.0);
        data.getSrb().setSensor("casingTemperature",  797.0);
        data.getSrb().setSensor("vibration",            0.22);

        data.setExternalTank(new com.group5.shuttle.model.ShuttlePart());
        data.getExternalTank().setSensor("fuelTemperature", -150.0);
        data.getExternalTank().setSensor("fuelPressure",       4.8);
        data.getExternalTank().setSensor("stress",            22.0);

        return data;
    }

    // ── Schwellwerte ──────────────────────────────────────────────────────────

    public Map<String, Map<String, SensorThreshold>> loadThresholds() {
        Map<String, Map<String, SensorThreshold>> result = new HashMap<>();

        Map<String, SensorThreshold> orbiter = new HashMap<>();
        orbiter.put("hullTemperature", threshold(null,  650.0));
        orbiter.put("cabinPressure",   threshold(95.0,  110.0));
        orbiter.put("oxygenLevel",     threshold(90.0,  null));
        orbiter.put("coolantPressure", threshold(3.5,   null));
        result.put("orbiter", orbiter);

        Map<String, SensorThreshold> srb = new HashMap<>();
        srb.put("thrust",            threshold(1500.0, null));
        srb.put("casingTemperature", threshold(null,   800.0));
        srb.put("vibration",         threshold(null,     0.40));
        result.put("srb", srb);

        Map<String, SensorThreshold> tank = new HashMap<>();
        tank.put("fuelTemperature", threshold(-170.0, -120.0));
        tank.put("fuelPressure",    threshold(4.0,    null));
        tank.put("stress",          threshold(null,   20.0));
        result.put("externalTank", tank);

        return result;
    }

    private SensorThreshold threshold(Double min, Double max) {
        return new SensorThreshold(min, max);
    }

    // ── Sensorauswertung ──────────────────────────────────────────────────────

    public String evaluate(double value, SensorThreshold t) {
        if (t.getMin() != null && value < t.getMin()) return "REPLACE";
        if (t.getMax() != null && value > t.getMax()) return "REPLACE";
        if (t.getMin() != null && value < t.getMin() + 5) return "WARNING";
        if (t.getMax() != null && value > t.getMax() - 5) return "WARNING";
        return "OK";
    }

    // ── Mitarbeiter ───────────────────────────────────────────────────────────

    public EmployeeRoster loadEmployees() {
        EmployeeRoster roster = new EmployeeRoster();

        EmployeeTeam team = new EmployeeTeam();
        team.name = "Team Alpha";
        team.members = Arrays.asList(
            emp("EMP-001", "Ellen Vance",   "Technician",     "Team Alpha"),
            emp("EMP-002", "Sandra Beck",   "Logistics",      "Logistics"),
            emp("EMP-003", "Leia Organa",   "Planner",        "Planning"),
            emp("EMP-004", "Markus Reuter", "Security Chief", "Team Alpha")
        );

        roster.teams = Arrays.asList(team);
        return roster;
    }

    private Employee emp(String id, String name, String role, String team) {
        return new Employee(id, name, role, team);
    }

    // ── Routineaufgaben ───────────────────────────────────────────────────────

    public List<RoutineTask> loadRoutineTasks() {
        return Arrays.asList(
            rt("RT-001", "Refuel Main Tanks",          "External Tank", 120, "EMP-001"),
            rt("RT-002", "Clean Cabin",                 "Orbiter",        60, "EMP-001"),
            rt("RT-003", "Check Fire Suppression",      "Orbiter",        45, "EMP-001"),
            rt("RT-004", "Inspect Landing Gear",        "Orbiter",        90, "EMP-001"),
            rt("RT-005", "Lubricate Docking Mechanism", "Orbiter",        30, "EMP-001"),
            rt("RT-006", "Calibrate Instruments",       "Orbiter",        75, "EMP-001"),
            rt("RT-007", "Inspect SRB Nozzles",         "SRB",            60, "EMP-001"),
            rt("RT-008", "Check External Tank Seals",   "External Tank",  50, "EMP-001")
        );
    }

    private RoutineTask rt(String id, String name, String part, int minutes, String empId) {
        RoutineTask t = new RoutineTask();
        t.id                 = id;
        t.name               = name;
        t.shuttlePart        = part;
        t.estimatedMinutes   = minutes;
        t.assignedEmployeeId = empId;
        return t;
    }
}
