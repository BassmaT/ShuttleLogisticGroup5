package com.group5.shuttle.service;

import com.group5.shuttle.model.RoutineTask;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// Singleton: hält alle Routineaufgaben der aktuellen Sitzung im Arbeitsspeicher.
// Die Aufgabenliste wird einmalig aus routine_tasks.json geladen.
// Der Erledigt-Status ist nur transient – er wird beim Schließen der App gelöscht.
public class RoutineTaskStore {

    // Die einzige Instanz dieser Klasse
    private static RoutineTaskStore instance;

    // Liste aller geladenen Routineaufgaben
    private List<RoutineTask> tasks = new ArrayList<>();

    // Privater Konstruktor: lädt die Aufgaben sofort beim ersten Zugriff
    private RoutineTaskStore() {
        tasks = new SensorService().loadRoutineTasks();
    }

    // Gibt die einzige Instanz zurück – erstellt sie beim ersten Aufruf
    public static RoutineTaskStore getInstance() {
        if (instance == null) instance = new RoutineTaskStore();
        return instance;
    }

    // Gibt eine unveränderliche Sicht auf alle Aufgaben zurück
    public List<RoutineTask> getAllTasks() {
        return Collections.unmodifiableList(tasks);
    }

    // Gibt alle Aufgaben zurück, die einem bestimmten Mitarbeiter (per ID) zugeordnet sind
    public List<RoutineTask> getTasksForEmployee(String employeeId) {
        return tasks.stream()
            .filter(t -> employeeId.equals(t.getAssignedEmployeeId()))
            .collect(Collectors.toList());
    }

    // Gibt alle Aufgaben zurück, die einem bestimmten Shuttle-Teil zugeordnet sind
    public List<RoutineTask> getTasksForPart(String shuttlePart) {
        return tasks.stream()
            .filter(t -> shuttlePart.equalsIgnoreCase(t.getShuttlePart()))
            .collect(Collectors.toList());
    }

    // Gibt eine einzelne Aufgabe anhand ihrer ID zurück (oder null, falls nicht gefunden)
    public RoutineTask getById(String id) {
        return tasks.stream()
            .filter(t -> id.equals(t.getId()))
            .findFirst().orElse(null);
    }

    // Setzt alle Aufgaben auf "nicht erledigt" zurück – wird beim Takeover-Reset aufgerufen
    public void reset() {
        tasks.forEach(t -> {
            t.setDone(false);         // Erledigt-Flag zurücksetzen
            t.setCompletedAt(null);   // Zeitstempel löschen
        });
    }
}
