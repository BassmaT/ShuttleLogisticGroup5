package com.group5.shuttle.controller;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.SessionState;
import com.group5.shuttle.model.UserRole;


import com.group5.shuttle.util.Dialogs;
import com.group5.shuttle.util.EmployeeListCell;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.util.List;

// Controller for the login screen (login_view.fxml).
// The login is for demo purposes only – the user selects their name from a ComboBox.
// The role is then read from the Employee object and stored in SessionState.
public class LoginController {

    @FXML private ComboBox<Employee> cmbEmployee;
    @FXML private Label lblRole;
    @FXML private Label lblError;
    @FXML private Button btnLogin;

    private final EmployeeService employeeService = EmployeeService.getInstance();

    @FXML
    public void initialize() {
        List<Employee> employees = employeeService.getAllEmployees();
        cmbEmployee.setItems(FXCollections.observableArrayList(employees));

        cmbEmployee.setCellFactory(lv -> new EmployeeListCell());
        cmbEmployee.setButtonCell(new EmployeeListCell());

        // Show role live as the selection changes
        cmbEmployee.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                lblRole.setText("Role: " + selected.getRole());
                lblError.setText("");
            }
        });
    }

    @FXML
    private void handleLogin() {
        Employee selected = cmbEmployee.getSelectionModel().getSelectedItem();

        if (selected == null) {
            lblError.setText("Please choose an employee.");
            return;
        }

        // Derive UserRole from Employee
        UserRole role = switch (selected.getRole()) {
            case "Planner" -> UserRole.PLANNER;
            case "Security Chief" -> UserRole.SECURITY;
            case "Technician" -> UserRole.TECHNICIAN;
            case "Logistics" -> UserRole.LOGISTICIAN;
            default -> throw new IllegalStateException(
                    "Unknown role: " + selected.getRole()
            );
        };

        SessionState.getInstance().setCurrentUser(selected);
        SessionState.getInstance().setUserRole(role);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            Dialogs.showError("Navigation error", "Dashboard can't load.", e.getMessage());
        }
    }
}