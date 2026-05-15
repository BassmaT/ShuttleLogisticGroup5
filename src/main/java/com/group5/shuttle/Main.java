package com.group5.shuttle;

// Entry point of the application.
// This class exists only due to a JavaFX issue with certain startup environments:
// Starting directly via App.main() does not always work because JavaFX
// must skip the classpath check. Main as an intermediary class solves this.
public class Main {

    // The main method is the very first thing called by the operating system.
    public static void main(String[] args) {
        // Starts the actual JavaFX application (App.java).
        // App.launch() initializes JavaFX and then calls App.start().
        App.launch(App.class, args);
    }
}
