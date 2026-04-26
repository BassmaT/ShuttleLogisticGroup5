package com.group5.shuttle.controller;

import com.group5.shuttle.util.Dialogs;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

// Abstrakte Basisklasse für alle Controller.
// Sie enthält gemeinsame Funktionalität, die jeder Controller braucht: die Navigation zwischen Seiten.
// "abstract" bedeutet: Diese Klasse kann nicht direkt benutzt werden – man muss von ihr erben.
public abstract class BaseController {

    // Jeder Controller muss diese Methode implementieren und seinen Zurück-Button zurückgeben.
    // Über diesen Button holt sich loadView() das aktuelle Fenster (Stage).
    protected abstract Button getNavigationButton();

    // Lädt eine andere FXML-Seite und tauscht den Inhalt des Fensters aus.
    // fxml ist der Dateiname, z. B. "main_view.fxml".
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
            Dialogs.showError("Navigationsfehler", "Ansicht konnte nicht geladen werden: " + fxml, e.getMessage());
        }
    }
}
