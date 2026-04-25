package com.group5.shuttle.util;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.IEmployeeService;
import javafx.util.StringConverter;

// Wiederverwendbarer StringConverter für Employee-ComboBoxen.
// Zeigt "Name (Rolle)" an, z. B. "Ellen Vance (Technician)".
public final class EmployeeStringConverter extends StringConverter<Employee> {

    public static final EmployeeStringConverter INSTANCE = new EmployeeStringConverter();

    private final IEmployeeService employeeService = EmployeeService.getInstance();

    private EmployeeStringConverter() {}

    @Override
    public String toString(Employee e) {
        return e == null ? "" : e.getName() + " (" + e.getRole() + ")";
    }

    @Override
    public Employee fromString(String s) {
        if (s == null || s.isEmpty()) return null;
        return employeeService.getAllEmployees().stream()
            .filter(e -> toString(e).equals(s))
            .findFirst().orElse(null);
    }
}
