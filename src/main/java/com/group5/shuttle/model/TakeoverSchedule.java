package com.group5.shuttle.model;

import java.util.List;

// Wrapper object for the 3-day handover schedule
public class TakeoverSchedule {

    // Title of the handover, e.g. "Pre-Launch Processing – Takeover Session"
    private final String takeoverTitle;

    // List of the three days with their entries
    private final List<ScheduleDay> days;

    public TakeoverSchedule(String takeoverTitle, List<ScheduleDay> days) {
        this.takeoverTitle = takeoverTitle;
        this.days          = days;
    }

    public String getTakeoverTitle()   { return takeoverTitle; }
    public List<ScheduleDay> getDays() { return days; }
}
