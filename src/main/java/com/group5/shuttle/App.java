package com.group5.shuttle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Hauptklasse der JavaFX-Anwendung.
// Sie erbt von Application – das ist bei jeder JavaFX-App Pflicht.
// JavaFX ruft automatisch die start()-Methode auf, sobald das Framework bereit ist.
public class App extends Application {

    // Diese Methode wird von JavaFX aufgerufen, wenn die Anwendung startet.
    // stage ist das Hauptfenster der App.
    @Override
    public void start(Stage stage) {
        try {
            // Zuerst den Login-Screen laden – erst nach Anmeldung wird das Dashboard gezeigt.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login_view.fxml"));

            // Lädt die FXML-Datei und packt das fertige UI in eine 800×600 Pixel große Scene.
            Scene scene = new Scene(loader.load(), 800, 600);

            // Setzt den Text in der Titelleiste des Fensters.
            stage.setTitle("Shuttle Dashboard");

            // Verbindet die Scene mit dem Fenster, damit das UI angezeigt wird.
            stage.setScene(scene);

            // Macht das Fenster sichtbar. Ohne diesen Aufruf bleibt das Fenster unsichtbar.
            stage.show();

        } catch (Exception e) {
            // Zeigt einen Fehlerdialog, falls die FXML-Datei nicht geladen werden kann.
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Startfehler");
            alert.setHeaderText("Anwendung konnte nicht gestartet werden");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
