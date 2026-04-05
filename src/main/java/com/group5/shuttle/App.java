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
            // Erstellt einen Loader, der die FXML-Datei für das Haupt-Dashboard liest.
            // getClass().getResource() sucht die Datei im resources-Ordner des Projekts.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml"));

            // Lädt die FXML-Datei und packt das fertige UI in eine 800×600 Pixel große Scene.
            Scene scene = new Scene(loader.load(), 800, 600);

            // Setzt den Text in der Titelleiste des Fensters.
            stage.setTitle("Shuttle Dashboard");

            // Verbindet die Scene mit dem Fenster, damit das UI angezeigt wird.
            stage.setScene(scene);

            // Macht das Fenster sichtbar. Ohne diesen Aufruf bleibt das Fenster unsichtbar.
            stage.show();

        } catch (Exception e) {
            // Gibt Fehler in der Konsole aus, z. B. wenn die FXML-Datei nicht gefunden wird.
            e.printStackTrace();
        }
    }
}
