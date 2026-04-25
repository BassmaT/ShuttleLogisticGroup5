package com.group5.shuttle.service;

import com.group5.shuttle.model.Employee;

import java.util.List;
import java.util.Optional;

// Abstraktion für den Mitarbeiter-Service (D – Dependency Inversion).
public interface IEmployeeService {
    List<Employee> getAllEmployees();
    Optional<Employee> getById(String id);
    List<Employee> getByRole(String role);
    List<Employee> getByTeam(String teamName);
}
