package com.group5.shuttle.model;

// Ein Wartungsticket dokumentiert eine durchgeführte Reparatur.
// Wird nach jeder abgeschlossenen Reparatur in der History gespeichert.
public class MaintenanceTicket {

    // Eindeutige Ticket-Nummer, z. B. "TKT-001".
    public String id;

    // Datum und Uhrzeit der Reparatur, z. B. "2026-04-05 14:32:10".
    public String date;

    // Welcher Shuttle-Teil wurde repariert, z. B. "Orbiter".
    public String part;

    // Welcher Sensor hat die Reparatur ausgelöst, z. B. "coolantPressure".
    public String sensor;

    // Welchen Status hatte der Sensor vor der Reparatur: "WARNING" oder "REPLACE".
    public String oldStatus;

    // Was wurde konkret gemacht, z. B. "Replace component – used: Heat Shield Panel".
    public String action;

    // Wer hat die Reparatur durchgeführt – Name des Technikers.
    public String technician;

    // Getter-Methoden – werden von PropertyValueFactory für die History-Tabelle benötigt.
    public String getId()         { return id; }
    public String getDate()       { return date; }
    public String getPart()       { return part; }
    public String getSensor()     { return sensor; }
    public String getOldStatus()  { return oldStatus; }
    public String getAction()     { return action; }
    public String getTechnician() { return technician; }
}
