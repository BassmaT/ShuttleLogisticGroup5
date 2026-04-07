package com.group5.shuttle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// Repräsentiert ein Team von Mitarbeitern, z. B. "Team Alpha" mit vier Mitgliedern
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeTeam {

    // Name des Teams, z. B. "Team Alpha"
    public String name;

    // Liste aller Teammitglieder
    public List<Employee> members;

    // Getter für den Teamnamen
    public String getName() { return name; }

    // Getter für die Mitgliederliste
    public List<Employee> getMembers() { return members; }
}
