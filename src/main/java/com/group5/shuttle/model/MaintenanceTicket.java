package com.group5.shuttle.model;

// Ein Wartungsticket dokumentiert eine durchgeführte Reparatur.
// Wird nach jeder abgeschlossenen Reparatur in der History gespeichert.
public class MaintenanceTicket {

    // Eindeutige Ticket-Nummer, z. B. "TKT-001".
    private String id;

    // Datum und Uhrzeit der Reparatur, z. B. "2026-04-05 14:32:10".
    private String date;

    // Welcher Shuttle-Teil wurde repariert, z. B. "Orbiter".
    private String part;

    // Welcher Sensor hat die Reparatur ausgelöst, z. B. "coolantPressure".
    private String sensor;

    // Welchen Status hatte der Sensor vor der Reparatur: "WARNING" oder "REPLACE".
    private String oldStatus;

    // Was wurde konkret gemacht, z. B. "Replace component – used: Heat Shield Panel".
    private String action;

    // Wer hat die Reparatur durchgeführt – Name des Technikers.
    private String technician;

    // Konstruktor – initialisiert ein vollständiges Ticket in einem Schritt.
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

    // Getter-Methoden – werden von PropertyValueFactory für die History-Tabelle benötigt.
    public String getId()         { return id; }
    public String getDate()       { return date; }
    public String getPart()       { return part; }
    public String getSensor()     { return sensor; }
    public String getOldStatus()  { return oldStatus; }
    public String getAction()     { return action; }
    public String getTechnician() { return technician; }
}
