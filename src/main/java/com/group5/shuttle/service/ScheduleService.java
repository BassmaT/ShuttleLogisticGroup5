package com.group5.shuttle.service;

import com.group5.shuttle.model.ScheduleDay;
import com.group5.shuttle.model.ScheduleEntry;
import com.group5.shuttle.model.TakeoverSchedule;

import java.util.Arrays;

/**
 * Verantwortlich für: den 3-Tage-Übergabe-Zeitplan.
 * Singleton – Zeitplandaten ändern sich nicht während der Sitzung.
 */
public class ScheduleService implements IScheduleService {

    private static final class Holder {
        static final ScheduleService INSTANCE = new ScheduleService();
    }

    private ScheduleService() {}

    public static ScheduleService getInstance() {
        return Holder.INSTANCE;
    }

    public TakeoverSchedule loadSchedule() {
        ScheduleDay day1 = new ScheduleDay(1, "Day 1 – Inspection & Diagnostics", Arrays.asList(
            se("06:00", "Full sensor diagnostic run",        "diagnostic", "EMP-001", "Orbiter"),
            se("08:00", "Inspect Landing Gear",              "routine",    "EMP-001", "Orbiter"),
            se("09:00", "Check Fire Suppression System",     "routine",    "EMP-001", "Orbiter"),
            se("10:00", "Inspect SRB Nozzles",               "routine",    "EMP-001", "SRB"),
            se("12:00", "Lunch Break",                       "break",      null,      null),
            se("13:00", "Repair: coolantPressure (Orbiter)", "repair",     "EMP-001", "Orbiter"),
            se("15:00", "Security Chief Review – Orbiter",   "approval",   "EMP-004", "Orbiter")
        ));

        ScheduleDay day2 = new ScheduleDay(2, "Day 2 – Repairs & Systems Check", Arrays.asList(
            se("07:00", "Repair: casingTemperature (SRB)",   "repair",   "EMP-001", "SRB"),
            se("09:00", "Repair: stress (External Tank)",    "repair",   "EMP-001", "External Tank"),
            se("10:00", "Refuel Main Tanks",                  "routine",  "EMP-001", "External Tank"),
            se("12:00", "Calibrate Instruments",              "routine",  "EMP-001", "Orbiter"),
            se("14:00", "Security Chief Review – SRB",        "approval", "EMP-004", "SRB"),
            se("16:00", "Security Chief Review – Ext. Tank",  "approval", "EMP-004", "External Tank")
        ));

        ScheduleDay day3 = new ScheduleDay(3, "Day 3 – Final Checks & Sign-Off", Arrays.asList(
            se("07:00", "Clean Cabin",                   "routine",    "EMP-001", "Orbiter"),
            se("08:00", "Lubricate Docking Mechanism",   "routine",    "EMP-001", "Orbiter"),
            se("09:00", "Check External Tank Seals",     "routine",    "EMP-001", "External Tank"),
            se("10:00", "Final systems walkthrough",     "diagnostic", "EMP-001", null),
            se("12:00", "Takeover Complete – Sign-Off",  "approval",   "EMP-004", null)
        ));

        return new TakeoverSchedule("Pre-Launch Processing – Takeover Session",
                                    Arrays.asList(day1, day2, day3));
    }

    private ScheduleEntry se(String time, String task, String category,
                              String empId, String part) {
        return new ScheduleEntry(time, task, category, empId, part);
    }
}
