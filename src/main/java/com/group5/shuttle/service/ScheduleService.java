package com.group5.shuttle.service;

import com.group5.shuttle.model.ScheduleDay;
import com.group5.shuttle.model.ScheduleEntry;
import com.group5.shuttle.model.TakeoverSchedule;

import java.util.Arrays;

/**
 * Verantwortlich für: den 3-Tage-Übergabe-Zeitplan.
 * Singleton – Zeitplandaten ändern sich nicht während der Sitzung.
 */
public class ScheduleService {

    private static ScheduleService instance;

    private ScheduleService() {}

    public static ScheduleService getInstance() {
        if (instance == null) instance = new ScheduleService();
        return instance;
    }

    public TakeoverSchedule loadSchedule() {
        TakeoverSchedule schedule = new TakeoverSchedule();
        schedule.takeoverTitle = "Pre-Launch Processing – Takeover Session";

        ScheduleDay day1 = new ScheduleDay();
        day1.dayNumber = 1;
        day1.label     = "Day 1 – Inspection & Diagnostics";
        day1.entries   = Arrays.asList(
            se("06:00", "Full sensor diagnostic run",        "diagnostic", "EMP-001", "Orbiter"),
            se("08:00", "Inspect Landing Gear",              "routine",    "EMP-001", "Orbiter"),
            se("09:00", "Check Fire Suppression System",     "routine",    "EMP-001", "Orbiter"),
            se("10:00", "Inspect SRB Nozzles",               "routine",    "EMP-001", "SRB"),
            se("12:00", "Lunch Break",                       "break",      null,      null),
            se("13:00", "Repair: coolantPressure (Orbiter)", "repair",     "EMP-001", "Orbiter"),
            se("15:00", "Security Chief Review – Orbiter",   "approval",   "EMP-004", "Orbiter")
        );

        ScheduleDay day2 = new ScheduleDay();
        day2.dayNumber = 2;
        day2.label     = "Day 2 – Repairs & Systems Check";
        day2.entries   = Arrays.asList(
            se("07:00", "Repair: casingTemperature (SRB)",   "repair",   "EMP-001", "SRB"),
            se("09:00", "Repair: stress (External Tank)",    "repair",   "EMP-001", "External Tank"),
            se("10:00", "Refuel Main Tanks",                  "routine",  "EMP-001", "External Tank"),
            se("12:00", "Calibrate Instruments",              "routine",  "EMP-001", "Orbiter"),
            se("14:00", "Security Chief Review – SRB",        "approval", "EMP-004", "SRB"),
            se("16:00", "Security Chief Review – Ext. Tank",  "approval", "EMP-004", "External Tank")
        );

        ScheduleDay day3 = new ScheduleDay();
        day3.dayNumber = 3;
        day3.label     = "Day 3 – Final Checks & Sign-Off";
        day3.entries   = Arrays.asList(
            se("07:00", "Clean Cabin",                   "routine",    "EMP-001", "Orbiter"),
            se("08:00", "Lubricate Docking Mechanism",   "routine",    "EMP-001", "Orbiter"),
            se("09:00", "Check External Tank Seals",     "routine",    "EMP-001", "External Tank"),
            se("10:00", "Final systems walkthrough",     "diagnostic", "EMP-001", null),
            se("12:00", "Takeover Complete – Sign-Off",  "approval",   "EMP-004", null)
        );

        schedule.days = Arrays.asList(day1, day2, day3);
        return schedule;
    }

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
}
