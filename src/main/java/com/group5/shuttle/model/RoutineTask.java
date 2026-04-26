package com.group5.shuttle.model;

import javafx.beans.property.SimpleBooleanProperty;

// Repräsentiert eine Routineaufgabe, die bei jeder Übergabe durchgeführt wird
public class RoutineTask {

    // Eindeutige ID der Aufgabe, z. B. "RT-001"
    private final String id;

    // Beschreibender Name der Aufgabe, z. B. "Refuel Main Tanks"
    private final String name;

    // Das Shuttle-Teil, auf das sich die Aufgabe bezieht, z. B. "Orbiter"
    private final String shuttlePart;

    // Geschätzte Dauer in Minuten
    private final int estimatedMinutes;

    // ID des zuständigen Mitarbeiters, z. B. "EMP-001"
    private final String assignedEmployeeId;

    // Erledigt-Flag – wird nur im Arbeitsspeicher gespeichert, nicht persistiert
    // SimpleBooleanProperty ermöglicht die Bindung an eine CheckBox in der TableView
    private final SimpleBooleanProperty done = new SimpleBooleanProperty(false);

    // Zeitstempel der Erledigung, z. B. "2026-04-06 14:32" – null wenn noch nicht erledigt
    private String completedAt = null;

    public RoutineTask(String id, String name, String shuttlePart,
                       int estimatedMinutes, String assignedEmployeeId) {
        this.id                 = id;
        this.name               = name;
        this.shuttlePart        = shuttlePart;
        this.estimatedMinutes   = estimatedMinutes;
        this.assignedEmployeeId = assignedEmployeeId;
    }

    public String getId()                 { return id; }
    public String getName()               { return name; }
    public String getShuttlePart()        { return shuttlePart; }
    public int getEstimatedMinutes()      { return estimatedMinutes; }
    public String getAssignedEmployeeId() { return assignedEmployeeId; }

    // Gibt zurück, ob die Aufgabe bereits erledigt ist
    public boolean isDone()               { return done.get(); }

    // Setzt den Erledigt-Status
    public void setDone(boolean v)        { done.set(v); }

    // Gibt die JavaFX-Property zurück – wird für CheckBoxTableCell benötigt
    public SimpleBooleanProperty doneProperty() { return done; }

    // Gibt den Erledigungszeitstempel zurück (null = noch nicht erledigt)
    public String getCompletedAt()        { return completedAt; }

    // Setzt den Erledigungszeitstempel
    public void setCompletedAt(String ts) { this.completedAt = ts; }
}
