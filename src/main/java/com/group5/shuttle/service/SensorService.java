package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.EmployeeRoster;
import com.group5.shuttle.model.EmployeeTeam;
import com.group5.shuttle.model.FlightHistory;
import com.group5.shuttle.model.FlightRecord;
import com.group5.shuttle.model.InventoryItem;
import com.group5.shuttle.model.MaintenanceTicket;
import com.group5.shuttle.model.RoutineTask;
import com.group5.shuttle.model.ScheduleDay;
import com.group5.shuttle.model.ScheduleEntry;
import com.group5.shuttle.model.SensorThreshold;
import com.group5.shuttle.model.ShuttleData;
import com.group5.shuttle.model.TakeoverSchedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Diese Klasse stellt alle Anwendungsdaten bereit.
// Alle Daten sind direkt in Java kodiert – es werden keine externen Dateien geladen.
public class SensorService {

    // Zwischenspeicher für die Lagerliste – bleibt für die gesamte Sitzung erhalten.
    private List<InventoryItem> inventoryCache = null;

    // ── Sensordaten ──────────────────────────────────────────────────────────

    // Gibt die aktuellen Sensorwerte aller drei Shuttle-Teile zurück.
    public ShuttleData loadSensorData() {
        ShuttleData data = new ShuttleData();

        data.orbiter = new com.group5.shuttle.model.ShuttlePart();
        data.orbiter.setSensor("hullTemperature", 520.0);
        data.orbiter.setSensor("cabinPressure",   101.0);
        data.orbiter.setSensor("oxygenLevel",      96.0);
        data.orbiter.setSensor("coolantPressure",   2.8);

        data.srb = new com.group5.shuttle.model.ShuttlePart();
        data.srb.setSensor("thrust",             1800.0);
        data.srb.setSensor("casingTemperature",   797.0);
        data.srb.setSensor("vibration",             0.22);

        data.externalTank = new com.group5.shuttle.model.ShuttlePart();
        data.externalTank.setSensor("fuelTemperature", -150.0);
        data.externalTank.setSensor("fuelPressure",       4.8);
        data.externalTank.setSensor("stress",            22.0);

        return data;
    }

    // ── Grenzwerte ───────────────────────────────────────────────────────────

    // Gibt die Alarm-Grenzwerte aller Sensoren zurück.
    // Aufbau: Teil → Sensor → SensorThreshold(min, max)
    public Map<String, Map<String, SensorThreshold>> loadThresholds() {
        Map<String, Map<String, SensorThreshold>> result = new HashMap<>();
//Sensordaten sind KI generiert
        // Orbiter
        Map<String, SensorThreshold> orbiter = new HashMap<>();
        orbiter.put("hullTemperature", threshold(null,  650.0));
        orbiter.put("cabinPressure",   threshold(95.0,  110.0));
        orbiter.put("oxygenLevel",     threshold(90.0,  null));
        orbiter.put("coolantPressure", threshold(3.5,   null));
        result.put("orbiter", orbiter);

        // SRB
        Map<String, SensorThreshold> srb = new HashMap<>();
        srb.put("thrust",             threshold(1500.0, null));
        srb.put("casingTemperature",  threshold(null,   800.0));
        srb.put("vibration",          threshold(null,     0.40));
        result.put("srb", srb);

        // External Tank
        Map<String, SensorThreshold> tank = new HashMap<>();
        tank.put("fuelTemperature", threshold(-170.0, -120.0));
        tank.put("fuelPressure",    threshold(4.0,    null));
        tank.put("stress",          threshold(null,   20.0));
        result.put("externalTank", tank);

        return result;
    }

    // Hilfsmethode: erstellt ein SensorThreshold-Objekt mit min und max.
    private SensorThreshold threshold(Double min, Double max) {
        SensorThreshold t = new SensorThreshold();
        t.min = min;
        t.max = max;
        return t;
    }

    // ── Lagerhaltung ─────────────────────────────────────────────────────────

    // Gibt die aktuelle Lagerliste zurück.
    // Beim ersten Aufruf werden die Standarddaten angelegt (einmalig pro Sitzung).
    public List<InventoryItem> loadInventory() {
        if (inventoryCache == null) {
            inventoryCache = createDefaultInventory();
        }
        return inventoryCache;
    }

    // Speichert eine geänderte Lagerliste im Arbeitsspeicher.
    // Die Daten bleiben bis zum Beenden der Anwendung erhalten.
    public void saveInventory(List<InventoryItem> items) {
        inventoryCache = items;
    }

    // Erstellt die Ausgangslage der Lagerliste mit sieben Bauteilen.
    private List<InventoryItem> createDefaultInventory() {
        List<InventoryItem> list = new ArrayList<>();
//Die InventoryItems sind KI generiert
        list.add(item("INV-001", "Heat Shield Panel",      "Orbiter",       0, "OUT_OF_STOCK",
                "Thermal protection tile that shields the orbiter hull from extreme heat during atmospheric re-entry. Must be replaced after severe temperature anomalies."));
        list.add(item("INV-002", "O2 Sensor Module",        "Orbiter",       2, "LOW",
                "Monitors cabin oxygen concentration. Replaced when oxygenLevel sensor falls below safe thresholds to ensure crew life support integrity."));
        list.add(item("INV-003", "Coolant Pressure Valve",  "Orbiter",       3, "IN_STOCK",
                "Regulates coolant flow through the orbiter's thermal control system. Required when coolantPressure drops below minimum operating level."));
        list.add(item("INV-004", "SRB Nozzle Assembly",     "SRB",           3, "IN_STOCK",
                "Directs exhaust from the solid rocket booster to generate thrust. Inspected and replaced when casingTemperature or thrust readings deviate from nominal."));
        list.add(item("INV-005", "Vibration Damper",         "SRB",           1, "LOW",
                "Absorbs mechanical vibrations from the solid rocket booster during ignition and ascent. Replaced when vibration sensor exceeds safe limits."));
        list.add(item("INV-006", "Fuel Tank Liner",          "External Tank", 4, "IN_STOCK",
                "Insulating liner inside the external tank that prevents cryogenic fuel from warming. Inspected after every flight for cracks or stress deformation."));
        list.add(item("INV-007", "Cryogenic Seal",           "External Tank", 0, "OUT_OF_STOCK",
                "High-performance seal for cryogenic fuel connections. Required when stress or fuelPressure readings indicate structural fatigue in the external tank."));

        return list;
    }

    // Hilfsmethode: erstellt ein einzelnes InventoryItem.
    private InventoryItem item(String id, String name, String part,
                               int quantity, String status, String description) {
        InventoryItem it = new InventoryItem();
        it.id          = id;
        it.name        = name;
        it.part        = part;
        it.quantity    = quantity;
        it.status      = status;
        it.description = description;
        return it;
    }

    // ── Wartungshistorie (nur im Arbeitsspeicher) KI generiert ────────────────────────────

    public List<MaintenanceTicket> loadTickets() {
        return TicketStore.getInstance().getTickets();
    }

    public void appendTicket(MaintenanceTicket ticket) {
        List<MaintenanceTicket> list = TicketStore.getInstance().getTickets();
        list.add(ticket);
        TicketStore.getInstance().saveTickets(list);
    }

    public void saveTickets(List<MaintenanceTicket> tickets) {
        TicketStore.getInstance().saveTickets(tickets);
    }

    // ── Sensorauswertung ─────────────────────────────────────────────────────

    // Vergleicht einen Messwert mit den Grenzwerten:
    // "REPLACE"  → Wert liegt außerhalb des erlaubten Bereichs
    // "WARNING"  → Wert nähert sich dem Grenzwert (Puffer: 5 Einheiten)
    // "OK"       → Wert liegt im normalen Bereich
    public String evaluate(double value, SensorThreshold t) {
        if (t.min != null && value < t.min) return "REPLACE";
        if (t.max != null && value > t.max) return "REPLACE";
        if (t.min != null && value < t.min + 5) return "WARNING";
        if (t.max != null && value > t.max - 5) return "WARNING";
        return "OK";
    }

    // ── Mitarbeiter ──────────────────────────────────────────────────────────

    // Gibt alle Mitarbeiter in zwei Teams zurück.
    public EmployeeRoster loadEmployees() {
        EmployeeRoster roster = new EmployeeRoster();
//Mitarbeiter KI Generiert
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

    // Hilfsmethode: erstellt einen einzelnen Mitarbeiter.
    private Employee emp(String id, String name, String role, String team) {
        Employee e = new Employee();
        e.id   = id;
        e.name = name;
        e.role = role;
        e.team = team;
        return e;
    }

    // ── Routineaufgaben ──────────────────────────────────────────────────────

    // Gibt die acht Standard-Routineaufgaben zurück KI Generiert
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

    // Hilfsmethode: erstellt eine einzelne Routineaufgabe.
    private RoutineTask rt(String id, String name, String part, int minutes, String empId) {
        RoutineTask t = new RoutineTask();
        t.id                 = id;
        t.name               = name;
        t.shuttlePart        = part;
        t.estimatedMinutes   = minutes;
        t.assignedEmployeeId = empId;
        return t;
    }

    // ── Zeitplan ─────────────────────────────────────────────────────────────

    // Gibt den 3-Tage-Übergabe-Zeitplan zurück (Schedules sind KI Generiert)
    public TakeoverSchedule loadSchedule() {
        TakeoverSchedule schedule = new TakeoverSchedule();
        schedule.takeoverTitle = "Pre-Launch Processing – Takeover Session";

        // Tag 1
        ScheduleDay day1 = new ScheduleDay();
        day1.dayNumber = 1;
        day1.label     = "Day 1 – Inspection & Diagnostics";
        day1.entries   = Arrays.asList(
            se("06:00", "Full sensor diagnostic run",        "diagnostic", "EMP-001", "Orbiter"),
            se("08:00", "Inspect Landing Gear",              "routine",    "EMP-005", "Orbiter"),
            se("09:00", "Check Fire Suppression System",     "routine",    "EMP-003", "Orbiter"),
            se("10:00", "Inspect SRB Nozzles",               "routine",    "EMP-003", "SRB"),
            se("12:00", "Lunch Break",                       "break",      null,      null),
            se("13:00", "Repair: coolantPressure (Orbiter)", "repair",     "EMP-002", "Orbiter"),
            se("15:00", "Security Chief Review – Orbiter",   "approval",   "EMP-004", "Orbiter")
        );

        // Tag 2
        ScheduleDay day2 = new ScheduleDay();
        day2.dayNumber = 2;
        day2.label     = "Day 2 – Repairs & Systems Check";
        day2.entries   = Arrays.asList(
            se("07:00", "Repair: casingTemperature (SRB)",   "repair",   "EMP-005", "SRB"),
            se("09:00", "Repair: stress (External Tank)",    "repair",   "EMP-001", "External Tank"),
            se("10:00", "Refuel Main Tanks",                  "routine",  "EMP-001", "External Tank"),
            se("12:00", "Calibrate Instruments",              "routine",  "EMP-007", "Orbiter"),
            se("14:00", "Security Chief Review – SRB",        "approval", "EMP-008", "SRB"),
            se("16:00", "Security Chief Review – Ext. Tank",  "approval", "EMP-004", "External Tank")
        );

        // Tag 3
        ScheduleDay day3 = new ScheduleDay();
        day3.dayNumber = 3;
        day3.label     = "Day 3 – Final Checks & Sign-Off";
        day3.entries   = Arrays.asList(
            se("07:00", "Clean Cabin",                   "routine",    "EMP-002", "Orbiter"),
            se("08:00", "Lubricate Docking Mechanism",   "routine",    "EMP-006", "Orbiter"),
            se("09:00", "Check External Tank Seals",     "routine",    "EMP-006", "External Tank"),
            se("10:00", "Final systems walkthrough",     "diagnostic", "EMP-007", null),
            se("12:00", "Takeover Complete – Sign-Off",  "approval",   "EMP-004", null)
        );

        schedule.days = Arrays.asList(day1, day2, day3);
        return schedule;
    }

    // Hilfsmethode: erstellt einen einzelnen Zeitplan-Eintrag.
    private ScheduleEntry se(String time, String task, String category,
                             String empId, String part) {
        ScheduleEntry e = new ScheduleEntry();
        e.time               = time;
        e.task               = task;
        e.category           = category;
        e.assignedEmployeeId = empId;
        e.shuttlePart        = part;
        return e;
    }

    // ── Flughistorie (Trendanalyse) ───────────────────────────────────────────

    // Gibt die simulierten Sensordaten der letzten 5 Flüge zurück (daten)
    public FlightHistory loadFlightHistory() {
        FlightHistory history = new FlightHistory();
        history.flights = Arrays.asList(
            flight("STS-133", 1,
                orbiterSensors(480.0, 103.0, 97.0, 4.1),
                srbSensors(1820.0, 640.0, 0.14),
                tankSensors(-155.0, 4.9, 10.0)),
            flight("STS-134", 2,
                orbiterSensors(490.0, 102.0, 97.0, 3.8),
                srbSensors(1815.0, 660.0, 0.16),
                tankSensors(-152.0, 4.85, 13.0)),
            flight("STS-135", 3,
                orbiterSensors(500.0, 101.0, 96.0, 3.4),
                srbSensors(1808.0, 720.0, 0.18),
                tankSensors(-151.0, 4.82, 15.0)),
            flight("STS-136", 4,
                orbiterSensors(510.0, 101.0, 96.0, 3.1),
                srbSensors(1803.0, 760.0, 0.20),
                tankSensors(-150.0, 4.80, 18.0)),
            flight("STS-137", 5,
                orbiterSensors(520.0, 101.0, 96.0, 2.8),
                srbSensors(1800.0, 797.0, 0.22),
                tankSensors(-150.0, 4.80, 22.0))
        );
        return history;
    }

    // Hilfsmethoden für die Flughistorie

    private FlightRecord flight(String id, int number,
                                Map<String, Double> orbiter,
                                Map<String, Double> srb,
                                Map<String, Double> tank) {
        FlightRecord r = new FlightRecord();
        r.flightId     = id;
        r.flightNumber = number;
        r.sensors      = new HashMap<>();
        r.sensors.put("orbiter",      orbiter);
        r.sensors.put("srb",          srb);
        r.sensors.put("externalTank", tank);
        return r;
    }

    private Map<String, Double> orbiterSensors(double hull, double cabin,
                                               double oxygen, double coolant) {
        Map<String, Double> m = new HashMap<>();
        m.put("hullTemperature", hull);
        m.put("cabinPressure",   cabin);
        m.put("oxygenLevel",     oxygen);
        m.put("coolantPressure", coolant);
        return m;
    }

    private Map<String, Double> srbSensors(double thrust, double casing, double vibration) {
        Map<String, Double> m = new HashMap<>();
        m.put("thrust",             thrust);
        m.put("casingTemperature",  casing);
        m.put("vibration",          vibration);
        return m;
    }

    private Map<String, Double> tankSensors(double fuelTemp, double fuelPressure, double stress) {
        Map<String, Double> m = new HashMap<>();
        m.put("fuelTemperature", fuelTemp);
        m.put("fuelPressure",    fuelPressure);
        m.put("stress",          stress);
        return m;
    }
}
