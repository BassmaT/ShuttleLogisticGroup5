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

        data.orbiter = new com.group5.shuttle.model.ShuttlePart();
        data.orbiter.setSensor("hullTemperature", 520.0);
        data.orbiter.setSensor("cabinPressure",   101.0);
        data.orbiter.setSensor("oxygenLevel",      96.0);
        data.orbiter.setSensor("coolantPressure",   2.8);

        data.srb = new com.group5.shuttle.model.ShuttlePart();
        data.srb.setSensor("thrust",            1800.0);
        data.srb.setSensor("casingTemperature",  797.0);
        data.srb.setSensor("vibration",            0.22);

        data.externalTank = new com.group5.shuttle.model.ShuttlePart();
        data.externalTank.setSensor("fuelTemperature", -150.0);
        data.externalTank.setSensor("fuelPressure",       4.8);
        data.externalTank.setSensor("stress",            22.0);

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
        SensorThreshold t = new SensorThreshold();
        t.min = min;
        t.max = max;
        return t;
    }

    // ── Sensorauswertung ──────────────────────────────────────────────────────

    public String evaluate(double value, SensorThreshold t) {
        if (t.min != null && value < t.min) return "REPLACE";
        if (t.max != null && value > t.max) return "REPLACE";
        if (t.min != null && value < t.min + 5) return "WARNING";
        if (t.max != null && value > t.max - 5) return "WARNING";
        return "OK";
    }

    // ── Mitarbeiter ───────────────────────────────────────────────────────────

    public EmployeeRoster loadEmployees() {
        EmployeeRoster roster = new EmployeeRoster();

        EmployeeTeam alpha = new EmployeeTeam();
        alpha.name = "Team Alpha";
        alpha.members = Arrays.asList(
            emp("EMP-001", "Max Müller",    "Technician",     "Team Alpha"),
            emp("EMP-002", "Sarah Schmidt", "Technician",     "Team Alpha"),
            emp("EMP-003", "Hans Weber",    "Technician",     "Team Alpha"),
            emp("EMP-004", "Lena Fischer",  "Security Chief", "Team Alpha")
        );

        EmployeeTeam beta = new EmployeeTeam();
        beta.name = "Team Beta";
        beta.members = Arrays.asList(
            emp("EMP-005", "Tom Braun",     "Technician",     "Team Beta"),
            emp("EMP-006", "Julia Krause",  "Technician",     "Team Beta"),
            emp("EMP-007", "Erik Hoffmann", "Technician",     "Team Beta"),
            emp("EMP-008", "Anna Meyer",    "Security Chief", "Team Beta")
        );

        roster.teams = Arrays.asList(alpha, beta);
        return roster;
    }

    private Employee emp(String id, String name, String role, String team) {
        Employee e = new Employee();
        e.id   = id;
        e.name = name;
        e.role = role;
        e.team = team;
        return e;
    }

    // ── Routineaufgaben ───────────────────────────────────────────────────────

    public List<RoutineTask> loadRoutineTasks() {
        return Arrays.asList(
            rt("RT-001", "Refuel Main Tanks",          "External Tank", 120, "EMP-001"),
            rt("RT-002", "Clean Cabin",                 "Orbiter",        60, "EMP-002"),
            rt("RT-003", "Check Fire Suppression",      "Orbiter",        45, "EMP-003"),
            rt("RT-004", "Inspect Landing Gear",        "Orbiter",        90, "EMP-005"),
            rt("RT-005", "Lubricate Docking Mechanism", "Orbiter",        30, "EMP-006"),
            rt("RT-006", "Calibrate Instruments",       "Orbiter",        75, "EMP-007"),
            rt("RT-007", "Inspect SRB Nozzles",         "SRB",            60, "EMP-003"),
            rt("RT-008", "Check External Tank Seals",   "External Tank",  50, "EMP-006")
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
