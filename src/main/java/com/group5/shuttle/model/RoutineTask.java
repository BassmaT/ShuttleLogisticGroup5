package com.group5.shuttle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
// JavaFX-Property für die Checkbox-Bindung in der TableView
import javafx.beans.property.SimpleBooleanProperty;

// Repräsentiert eine Routineaufgabe, die bei jeder Übergabe durchgeführt wird
// Im Gegensatz zu Reparaturaufgaben sind diese immer vorhanden, unabhängig von Sensordaten
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoutineTask {

    // Eindeutige ID der Aufgabe, z. B. "RT-001"
    public String id;

    // Beschreibender Name der Aufgabe, z. B. "Refuel Main Tanks"
    public String name;

    // Das Shuttle-Teil, auf das sich die Aufgabe bezieht, z. B. "Orbiter"
    public String shuttlePart;

    // Geschätzte Dauer in Minuten
    public int estimatedMinutes;

    // ID des zuständigen Mitarbeiters, z. B. "EMP-001"
    public String assignedEmployeeId;

    // Erledigt-Flag – wird nur im Arbeitsspeicher gespeichert, nicht in JSON
    // SimpleBooleanProperty ermöglicht die Bindung an eine CheckBox in der TableView
    private final SimpleBooleanProperty done = new SimpleBooleanProperty(false);

    // Zeitstempel der Erledigung, z. B. "2026-04-06 14:32" – null wenn noch nicht erledigt
    private String completedAt = null;

    // --- Getter-Methoden ---

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
