package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.model.EmployeeRoster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// Singleton-Service: lädt die Mitarbeiterliste einmalig 
// und hält sie für die gesamte Sitzung im Arbeitsspeicher.
// Wird von MissionControlController, StaffController und LogisticsController verwendet KI Generiert
public class EmployeeService {

    // Die einzige Instanz dieser Klasse
    private static EmployeeService instance;

    // Flache Liste aller Mitarbeiter (aus allen Teams zusammengeführt)
    private final List<Employee> allEmployees = new ArrayList<>();

    
    private EmployeeService() {
        EmployeeRoster roster = SensorDataService.getInstance().loadEmployees();
        // Teams durchgehen und alle Mitglieder in die flache Liste einfügen
        if (roster != null && roster.getTeams() != null) {
            for (var team : roster.getTeams()) {
                if (team.getMembers() != null) {
                    allEmployees.addAll(team.getMembers());
                }
            }
        }
    }

    // Gibt die einzige Instanz zurück – erstellt sie beim ersten Aufruf
    public static EmployeeService getInstance() {
        if (instance == null) instance = new EmployeeService();
        return instance;
    }

    // Gibt alle Mitarbeiter zurück (unveränderlich)
    public List<Employee> getAllEmployees() {
        return Collections.unmodifiableList(allEmployees);
    }

    // Gibt nur Mitarbeiter einer bestimmten Rolle zurück
    // Wird z. B. für die ComboBox in MissionControlController verwendet
    public List<Employee> getByRole(String role) {
        return allEmployees.stream()
            .filter(e -> role.equals(e.getRole()))
            .collect(Collectors.toList());
    }

    // Gibt einen Mitarbeiter anhand seiner ID zurück (oder null, falls nicht gefunden)
    public Employee getById(String id) {
        return allEmployees.stream()
            .filter(e -> id.equals(e.getId()))
            .findFirst().orElse(null);
    }

    // Gibt alle Mitarbeiter eines bestimmten Teams zurück
    public List<Employee> getByTeam(String teamName) {
        return allEmployees.stream()
            .filter(e -> teamName.equals(e.getTeam()))
            .collect(Collectors.toList());
    }
}
