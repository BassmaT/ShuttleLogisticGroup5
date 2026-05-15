package com.group5.shuttle.model;

// Represents a single employee with their role and team membership
public class Employee {

    // Unique employee ID, e.g. "EMP-001"
    private String id;

    // Full name of the employee, e.g. "Max Müller"
    private String name;

    // Role: e.g. "Technician", "Security Chief", "Planner", "Logistics"
    private String role;

    // Team name, e.g. "Team Alpha"
    private String team;

    // Constructor – creates a complete employee record.
    public Employee(String id, String name, String role, String team) {
        this.id   = id;
        this.name = name;
        this.role = role;
        this.team = team;
    }

    // Getter methods – required by PropertyValueFactory in TableView
    public String getId()   { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getTeam() { return team; }

    @Override
    public String toString() { return name + " (" + role + ")"; }
}
