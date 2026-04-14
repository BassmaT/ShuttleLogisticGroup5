package com.group5.shuttle.model;

import java.util.List;

// Wrapper-Objekt für alle Teams
public class EmployeeRoster {

    // Liste aller Teams (Team Alpha, Team Beta usw.)
    public List<EmployeeTeam> teams;

    // Getter für die Teamliste
    public List<EmployeeTeam> getTeams() { return teams; }
}
