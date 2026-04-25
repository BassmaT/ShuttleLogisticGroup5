package com.group5.shuttle.model;

import java.util.List;

// Repräsentiert einen Tag im 3-Tage-Zeitplan
public class ScheduleDay {

    // Tagnummer: 1, 2 oder 3
    private final int dayNumber;

    // Beschriftung des Tages, z. B. "Day 1 – Inspection & Diagnostics"
    private final String label;

    // Alle Zeitplan-Einträge für diesen Tag
    private final List<ScheduleEntry> entries;

    public ScheduleDay(int dayNumber, String label, List<ScheduleEntry> entries) {
        this.dayNumber = dayNumber;
        this.label     = label;
        this.entries   = entries;
    }

    public int getDayNumber()               { return dayNumber; }
    public String getLabel()                { return label; }
    public List<ScheduleEntry> getEntries() { return entries; }
}
