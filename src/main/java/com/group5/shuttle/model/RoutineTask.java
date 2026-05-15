package com.group5.shuttle.model;

import javafx.beans.property.SimpleBooleanProperty;

// Represents a routine task to be carried out at every handover
public class RoutineTask {

    // Unique task ID, e.g. "RT-001"
    private final String id;

    // Descriptive name of the task, e.g. "Refuel Main Tanks"
    private final String name;

    // The shuttle part this task relates to, e.g. "Orbiter"
    private final String shuttlePart;

    // Estimated duration in minutes
    private final int estimatedMinutes;

    // ID of the responsible employee, e.g. "EMP-001"
    private final String assignedEmployeeId;

    // Done flag – stored in memory only, not persisted.
    // SimpleBooleanProperty enables binding to a CheckBox in the TableView.
    private final SimpleBooleanProperty done = new SimpleBooleanProperty(false);

    // Completion timestamp, e.g. "2026-04-06 14:32" – null if not yet completed
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

    // Returns whether the task has already been completed
    public boolean isDone()               { return done.get(); }

    // Sets the done status
    public void setDone(boolean v)        { done.set(v); }

    // Returns the JavaFX property – required for CheckBoxTableCell
    public SimpleBooleanProperty doneProperty() { return done; }

    // Returns the completion timestamp (null = not yet completed)
    public String getCompletedAt()        { return completedAt; }

    // Sets the completion timestamp
    public void setCompletedAt(String ts) { this.completedAt = ts; }
}
