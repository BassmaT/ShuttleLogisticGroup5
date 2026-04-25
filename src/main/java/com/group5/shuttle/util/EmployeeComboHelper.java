package com.group5.shuttle.util;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.service.EmployeeService;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;

public final class EmployeeComboHelper {

    private EmployeeComboHelper() {}

    public static void setup(ComboBox<Employee> combo) {
        combo.setItems(FXCollections.observableArrayList(
            EmployeeService.getInstance().getAllEmployees()));
        combo.setConverter(EmployeeStringConverter.INSTANCE);
    }
}
