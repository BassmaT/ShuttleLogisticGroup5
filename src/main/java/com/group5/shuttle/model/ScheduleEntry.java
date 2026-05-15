package com.group5.shuttle.model;

// Represents a single entry in the 3-day handover schedule
public class ScheduleEntry {

    // Time of the entry, e.g. "08:00"
    private final String time;

    // Description of the task, e.g. "Inspect Landing Gear"
    private final String task;

    // Category: "routine", "repair", "approval", "diagnostic" or "break"
    // Used for color rendering in the dashboard
    private final String category;

    // ID of the responsible employee – can be null (e.g. for breaks)
    private String assignedEmployeeId;

    // Affected shuttle part – can be null
    private final String shuttlePart;

    public ScheduleEntry(String time, String task, String category,
                         String assignedEmployeeId, String shuttlePart) {
        this.time               = time;
        this.task               = task;
        this.category           = category;
        this.assignedEmployeeId = assignedEmployeeId;
        this.shuttlePart        = shuttlePart;
    }

    public String getTime()               { return time; }
    public String getTask()               { return task; }
    public String getCategory()           { return category; }
    public String getAssignedEmployeeId() { return assignedEmployeeId; }
    public String getShuttlePart()        { return shuttlePart; }

    // Setter for interactive rescheduling in ScheduleController
    public void setAssignedEmployeeId(String id) { this.assignedEmployeeId = id; }
}
