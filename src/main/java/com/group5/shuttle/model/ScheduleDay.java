package com.group5.shuttle.model;

import javafx.collections.ObservableList;

// Represents a single day in the 3-day schedule
public class ScheduleDay {

    // Day number: 1, 2 or 3
    private final int dayNumber;

    // Label for the day, e.g. "Day 1 – Inspection & Diagnostics"
    private final String label;

    // All schedule entries for this day
    private final ObservableList<ScheduleEntry> entries;

    public ScheduleDay(int dayNumber, String label, ObservableList<ScheduleEntry> entries) {
        this.dayNumber = dayNumber;
        this.label     = label;
        this.entries   = entries;
    }

    public int getDayNumber()               { return dayNumber; }
    public String getLabel()                { return label; }
    public ObservableList<ScheduleEntry> getEntries() { return entries; }
}
