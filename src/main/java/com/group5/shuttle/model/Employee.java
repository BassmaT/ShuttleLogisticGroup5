package com.group5.shuttle.model;

// Repräsentiert einen einzelnen Mitarbeiter mit seiner Rolle und Teamzugehörigkeit
public class Employee {

    // Eindeutige Mitarbeiter-ID, z. B. "EMP-001"
    private String id;

    // Vollständiger Name des Mitarbeiters, z. B. "Max Müller"
    private String name;

    // Rolle: z. B. "Technician", "Security Chief", "Planner", "Logistics"
    private String role;

    // Teamname, z. B. "Team Alpha"
    private String team;

    // Konstruktor – erstellt einen vollständigen Mitarbeiter-Datensatz.
    public Employee(String id, String name, String role, String team) {
        this.id   = id;
        this.name = name;
        this.role = role;
        this.team = team;
    }

    // Getter-Methoden – werden von PropertyValueFactory in TableView benötigt
    public String getId()   { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getTeam() { return team; }

    @Override
    public String toString() { return name + " (" + role + ")"; }
}
