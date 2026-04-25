package com.group5.shuttle.service;

import com.group5.shuttle.model.RoutineTask;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Singleton: hält alle Routineaufgaben der aktuellen Sitzung im Arbeitsspeicher.
// Der Erledigt-Status ist nur transient – er wird beim Schließen der App gelöscht.
public class RoutineTaskStore {

    private static final class Holder {
        static final RoutineTaskStore INSTANCE = new RoutineTaskStore();
    }

    private final List<RoutineTask> tasks;

    private RoutineTaskStore() {
        tasks = List.of(
            new RoutineTask("RT-001", "Refuel Main Tanks",          "External Tank", 120, "EMP-001"),
            new RoutineTask("RT-002", "Clean Cabin",                 "Orbiter",        60, "EMP-001"),
            new RoutineTask("RT-003", "Check Fire Suppression",      "Orbiter",        45, "EMP-001"),
            new RoutineTask("RT-004", "Inspect Landing Gear",        "Orbiter",        90, "EMP-001"),
            new RoutineTask("RT-005", "Lubricate Docking Mechanism", "Orbiter",        30, "EMP-001"),
            new RoutineTask("RT-006", "Calibrate Instruments",       "Orbiter",        75, "EMP-001"),
            new RoutineTask("RT-007", "Inspect SRB Nozzles",         "SRB",            60, "EMP-001"),
            new RoutineTask("RT-008", "Check External Tank Seals",   "External Tank",  50, "EMP-001")
        );
    }

    public static RoutineTaskStore getInstance() {
        return Holder.INSTANCE;
    }

    public List<RoutineTask> getAllTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public List<RoutineTask> getTasksForEmployee(String employeeId) {
        return tasks.stream()
            .filter(t -> employeeId.equals(t.getAssignedEmployeeId()))
            .collect(Collectors.toList());
    }

    public List<RoutineTask> getTasksForPart(String shuttlePart) {
        return tasks.stream()
            .filter(t -> shuttlePart.equalsIgnoreCase(t.getShuttlePart()))
            .collect(Collectors.toList());
    }

    public Optional<RoutineTask> getById(String id) {
        return tasks.stream()
            .filter(t -> id.equals(t.getId()))
            .findFirst();
    }

    // Setzt alle Aufgaben auf "nicht erledigt" zurück – wird beim Takeover-Reset aufgerufen
    public void reset() {
        tasks.forEach(t -> {
            t.setDone(false);
            t.setCompletedAt(null);
        });
    }
}
