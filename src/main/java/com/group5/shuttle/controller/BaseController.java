package com.group5.shuttle.controller;

import com.group5.shuttle.util.Dialogs;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

// Abstract base class for all controllers.
// It contains common functionality that every controller needs: navigation between pages.
// "abstract" means: this class cannot be used directly – it must be extended.
public abstract class BaseController {

    // Every controller must implement this method and return its back button.
    // loadView() uses this button to retrieve the current window (Stage).
    protected abstract Button getNavigationButton();

    // Loads a different FXML page and replaces the content of the window.
    // fxml is the file name, e.g. "main_view.fxml".
    protected void setupBackButton(Button btn) {
        btn.setOnAction(e -> loadView("main_view.fxml"));
    }

    protected void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxml));
            Parent root = loader.load();
            Stage stage = (Stage) getNavigationButton().getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            Dialogs.showError("Navigation Error", "View could not be loaded: " + fxml, e.getMessage());
        }
    }
}
