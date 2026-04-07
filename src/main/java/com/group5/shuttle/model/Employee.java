package com.group5.shuttle.model;

// Jackson-Annotation: unbekannte JSON-Felder werden ignoriert, kein Fehler beim Laden
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Repräsentiert einen einzelnen Mitarbeiter mit seiner Rolle und Teamzugehörigkeit
@JsonIgnoreProperties(ignoreUnknown = true)
public class Employee {

    // Eindeutige Mitarbeiter-ID, z. B. "EMP-001"
    public String id;

    // Vollständiger Name des Mitarbeiters, z. B. "Max Müller"
    public String name;

    // Rolle: entweder "Technician" oder "Security Chief"
    public String role;

    // Teamname, z. B. "Team Alpha"
    public String team;

    // Getter-Methoden – werden von PropertyValueFactory in TableView benötigt
    public String getId()   { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getTeam() { return team; }

    // toString() gibt den Namen zurück – wird von ComboBox zur Anzeige verwendet
    @Override
    public String toString() { return name; }
}
