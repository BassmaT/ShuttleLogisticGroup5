package com.group5.shuttle.controller;

import com.group5.shuttle.model.Employee;
import com.group5.shuttle.service.EmployeeService;
import com.group5.shuttle.service.SessionState;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.stage.Stage;

import java.util.List;

// Controller für den Login-Screen (login_view.fxml).
// Der Login ist nur zur Demo – der Benutzer wählt seinen Namen aus einer ComboBox.
// Danach wird die Rolle aus dem Employee-Objekt gelesen und in SessionState gespeichert.
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

        // Zeigt Name + Rolle in der Dropdown-Liste
        cmbEmployee.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Employee emp, boolean empty) {
                super.updateItem(emp, empty);
                setText(empty || emp == null ? null : emp.getName() + "  (" + emp.getRole() + ")");
            }
        });
        cmbEmployee.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Employee emp, boolean empty) {
                super.updateItem(emp, empty);
                setText(empty || emp == null ? null : emp.getName() + "  (" + emp.getRole() + ")");
            }
        });

        // Rolle live anzeigen sobald Auswahl sich ändert
        cmbEmployee.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                lblRole.setText("Rolle: " + selected.getRole());
                lblError.setText("");
            }
        });
    }

    @FXML
    private void handleLogin() {
        Employee selected = cmbEmployee.getSelectionModel().getSelectedItem();

        if (selected == null) {
            lblError.setText("Bitte einen Mitarbeiter auswählen.");
            return;
        }

        // Eingeloggten User in SessionState speichern
        SessionState.getInstance().setCurrentUser(selected);

        // Zum Haupt-Dashboard wechseln
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigationsfehler");
            alert.setHeaderText("Dashboard konnte nicht geladen werden");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}