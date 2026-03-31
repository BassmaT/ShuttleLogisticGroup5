package com.group5.shuttle;
// → Definiert das Package, also in welchem Projektordner diese Klasse liegt.
//   Wichtig für Struktur, Imports und Maven.

import javafx.application.Application;
// → Importiert die JavaFX-Basisklasse "Application". Jede JavaFX-App MUSS davon erben.

import javafx.fxml.FXMLLoader;
// → Loader, der FXML-Dateien lädt und daraus UI-Elemente erzeugt.

import javafx.scene.Scene;
// → Eine Scene ist der sichtbare Bereich im Fenster (Stage). Darin liegen alle UI-Elemente.

import javafx.stage.Stage;
// → Stage ist das eigentliche Fenster der Anwendung.

public class App extends Application {
// → Unsere App-Klasse erbt von Application, damit JavaFX weiß, wo es starten soll.

    @Override
    public void start(Stage stage) {
        // → Die start()-Methode wird automatisch von JavaFX aufgerufen.
        //   Hier definieren wir, was beim Start der App passieren soll.

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_view.fxml"));
            // → Erstellt einen FXML-Loader und sagt ihm, welche FXML-Datei geladen werden soll.
            //   "/view/main_view.fxml" ist der Pfad in src/main/resources.
System.out.println(getClass().getResource("/view/main_view.fxml"));

            Scene scene = new Scene(loader.load(), 800, 600);
            // → Lädt die FXML-Datei und packt sie in eine Scene.
            //   Die Scene ist 800x600 Pixel groß.

            stage.setTitle("Shuttle Dashboard");
            // → Setzt den Fenstertitel.

            stage.setScene(scene);
            // → Verknüpft die Scene mit dem Fenster (Stage).

            stage.show();
            // → Zeigt das Fenster an. Ohne show() passiert gar nichts.

        } catch (Exception e) {
            e.printStackTrace();
            // → Falls etwas schiefgeht (z. B. FXML nicht gefunden), wird der Fehler ausgegeben.
        }
    }
}
