package com.group5.shuttle;

import com.group5.shuttle.util.Dialogs;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Main class of the JavaFX application.
// It extends Application – this is required for every JavaFX app.
// JavaFX automatically calls the start() method once the framework is ready.
public class App extends Application {

    // This method is called by JavaFX when the application starts.
    // stage is the main window of the app.
    @Override
    public void start(Stage stage) {
        try {
            // Load the login screen first – the dashboard is only shown after successful login.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login_view.fxml"));

            // Loads the FXML file and wraps the finished UI in an 800×600 pixel Scene.
            Scene scene = new Scene(loader.load(), 800, 600);

            // Sets the text in the window's title bar.
            stage.setTitle("Shuttle Dashboard");

            // Connects the Scene to the window so the UI is displayed.
            stage.setScene(scene);

            // Makes the window visible. Without this call the window remains invisible.
            stage.show();

        } catch (Exception e) {
            // Shows an error dialog if the FXML file cannot be loaded.
            Dialogs.showError("Startup Error", "Application could not be started", e.getMessage());
        }
    }
}
