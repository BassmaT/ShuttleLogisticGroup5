package com.group5.shuttle.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

// Wrapper-Objekt für die employees.json-Datei – enthält alle Teams
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeRoster {

    // Liste aller Teams (Team Alpha, Team Beta usw.)
    public List<EmployeeTeam> teams;

    // Getter für die Teamliste
    public List<EmployeeTeam> getTeams() { return teams; }
}
