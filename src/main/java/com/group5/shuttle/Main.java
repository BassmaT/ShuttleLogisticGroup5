package com.group5.shuttle;

// Einstiegspunkt der Anwendung.
// Diese Klasse existiert nur wegen eines JavaFX-Problems mit manchen Startumgebungen:
// Der direkte Start über App.main() funktioniert nicht immer, weil JavaFX
// den Classpath-Check überspringen muss. Main als Zwischenklasse löst das.
public class Main {

    // Die main-Methode wird als allererstes vom Betriebssystem aufgerufen.
    public static void main(String[] args) {
        // Startet die eigentliche JavaFX-Anwendung (App.java).
        // App.launch() initialisiert JavaFX und ruft dann App.start() auf.
        App.launch(App.class, args);
    }
}
