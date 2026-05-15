package com.group5.shuttle.model;

// A maintenance ticket documents a completed repair.
// Saved after every finished repair in the history.
public class MaintenanceTicket {

    // Unique ticket number, e.g. "TKT-001".
    private String id;

    // Date and time of the repair, e.g. "2026-04-05 14:32:10".
    private String date;

    // Which shuttle part was repaired, e.g. "Orbiter".
    private String part;

    // Which sensor triggered the repair, e.g. "coolantPressure".
    private String sensor;

    // What status the sensor had before the repair: "WARNING" or "REPLACE".
    private String oldStatus;

    // What was specifically done, e.g. "Replace component – used: Heat Shield Panel".
    private String action;

    // Who carried out the repair – name of the technician.
    private String technician;

    // Constructor – initializes a complete ticket in one step.
    public MaintenanceTicket(String id, String date, String part, String sensor,
                             String oldStatus, String action, String technician) {
        this.id         = id;
        this.date       = date;
        this.part       = part;
        this.sensor     = sensor;
        this.oldStatus  = oldStatus;
        this.action     = action;
        this.technician = technician;
    }

    // Getter methods – required by PropertyValueFactory for the history table.
    public String getId()         { return id; }
    public String getDate()       { return date; }
    public String getPart()       { return part; }
    public String getSensor()     { return sensor; }
    public String getOldStatus()  { return oldStatus; }
    public String getAction()     { return action; }
    public String getTechnician() { return technician; }
}
