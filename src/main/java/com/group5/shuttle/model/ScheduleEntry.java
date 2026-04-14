package com.group5.shuttle.model;

// Repräsentiert einen einzelnen Eintrag im 3-Tage-Zeitplan der Übergabe
public class ScheduleEntry {

    // Uhrzeit des Eintrags, z. B. "08:00"
    public String time;

    // Beschreibung der Aufgabe, z. B. "Inspect Landing Gear"
    public String task;

    // Kategorie: "routine", "repair", "approval", "diagnostic" oder "break"
    // Wird für die Farbdarstellung im Dashboard verwendet
    public String category;

    // ID des zuständigen Mitarbeiters – kann null sein (z. B. bei Pausen)
    public String assignedEmployeeId;

    // Betroffenes Shuttle-Teil – kann null sein
    public String shuttlePart;

    // --- Getter-Methoden für PropertyValueFactory ---
    public String getTime()               { return time; }
    public String getTask()               { return task; }
    public String getCategory()           { return category; }
    public String getAssignedEmployeeId() { return assignedEmployeeId; }
    public String getShuttlePart()        { return shuttlePart; }

    // Setter für interaktive Umplanung im ScheduleController
    public void setAssignedEmployeeId(String id) { this.assignedEmployeeId = id; }
}
