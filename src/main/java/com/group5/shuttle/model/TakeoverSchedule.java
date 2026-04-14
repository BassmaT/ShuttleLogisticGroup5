package com.group5.shuttle.model;

import java.util.List;

// Wrapper-Objekt für den 3-Tage-Zeitplan der Übergabe
public class TakeoverSchedule {

    // Titel der Übergabe, z. B. "Pre-Launch Processing – Takeover Session"
    public String takeoverTitle;

    // Liste der drei Tage mit ihren Einträgen
    public List<ScheduleDay> days;

    // Getter-Methoden
    public String getTakeoverTitle()   { return takeoverTitle; }
    public List<ScheduleDay> getDays() { return days; }
}
