package com.group5.shuttle.model;

import java.util.List;

// Repräsentiert einen Tag im 3-Tage-Zeitplan
public class ScheduleDay {

    // Tagnummer: 1, 2 oder 3
    public int dayNumber;

    // Beschriftung des Tages, z. B. "Day 1 – Inspection & Diagnostics"
    public String label;

    // Alle Zeitplan-Einträge für diesen Tag
    public List<ScheduleEntry> entries;

    // Getter-Methoden
    public int getDayNumber()               { return dayNumber; }
    public String getLabel()                { return label; }
    public List<ScheduleEntry> getEntries() { return entries; }
}
