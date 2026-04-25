package com.group5.shuttle.model;

import java.util.List;

// Wrapper-Objekt für den 3-Tage-Zeitplan der Übergabe
public class TakeoverSchedule {

    // Titel der Übergabe, z. B. "Pre-Launch Processing – Takeover Session"
    private final String takeoverTitle;

    // Liste der drei Tage mit ihren Einträgen
    private final List<ScheduleDay> days;

    public TakeoverSchedule(String takeoverTitle, List<ScheduleDay> days) {
        this.takeoverTitle = takeoverTitle;
        this.days          = days;
    }

    public String getTakeoverTitle()   { return takeoverTitle; }
    public List<ScheduleDay> getDays() { return days; }
}
