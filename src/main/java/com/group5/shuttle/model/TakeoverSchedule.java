package com.group5.shuttle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// Wrapper-Objekt für die schedule.json-Datei – enthält den 3-Tage-Zeitplan der Übergabe
@JsonIgnoreProperties(ignoreUnknown = true)
public class TakeoverSchedule {

    // Titel der Übergabe, z. B. "Pre-Launch Processing – Takeover Session"
    public String takeoverTitle;

    // Liste der drei Tage mit ihren Einträgen
    public List<ScheduleDay> days;

    // Getter-Methoden
    public String getTakeoverTitle()   { return takeoverTitle; }
    public List<ScheduleDay> getDays() { return days; }
}
