package com.group5.shuttle.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
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
    protected void loadView(String fxml) {
        try {
            // Erstellt einen Loader für die gewünschte FXML-Datei aus dem resources/view/-Ordner.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxml));

            // Lädt die FXML-Datei und erstellt daraus den UI-Baum (Parent = Wurzelelement).
            Parent root = loader.load();

            // Holt das aktuelle Fenster (Stage) über den Zurück-Button.
            Stage stage = (Stage) getNavigationButton().getScene().getWindow();

            // Tauscht den Inhalt des Fensters durch die neue Seite aus.
            stage.getScene().setRoot(root);

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigationsfehler");
            alert.setHeaderText("Ansicht konnte nicht geladen werden: " + fxml);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
