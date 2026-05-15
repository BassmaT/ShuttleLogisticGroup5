package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;

// Singleton service: holds the employee list for the entire session in memory.
// Used by MissionControlController, StaffController, and LogisticsController
public class EmployeeService implements IEmployeeService {

    private static final class Holder {
        static final EmployeeService INSTANCE = new EmployeeService();
    }

    private final List<Employee> allEmployees = new ArrayList<>();

    private EmployeeService() {
        allEmployees.addAll(List.of(
            new Employee("EMP-001", "Ellen Vance",   "Technician",     "Team Alpha"),
            new Employee("EMP-002", "Angelo De Simone",   "Logistics",      "Logistics"),
            new Employee("EMP-003", "Leia Organa",   "Planner",        "Planning"),
            new Employee("EMP-004", "Markus Reuter", "Security Chief", "Team Alpha")
        ));
    }

    public static EmployeeService getInstance() {
        return Holder.INSTANCE;
    }

    public List<Employee> getAllEmployees() {
        return Collections.unmodifiableList(allEmployees);
    }

    public List<Employee> getByRole(String role) {
        return allEmployees.stream()
            .filter(e -> role.equals(e.getRole()))
            .collect(Collectors.toList());
    }

    public Optional<Employee> getById(String id) {
        return allEmployees.stream()
            .filter(e -> id.equals(e.getId()))
            .findFirst();
    }

    public List<Employee> getByTeam(String teamName) {
        return allEmployees.stream()
            .filter(e -> teamName.equals(e.getTeam()))
            .collect(Collectors.toList());
    }
}
