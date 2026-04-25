package com.group5.shuttle.util;

import javafx.scene.control.Alert;

public final class Dialogs {

    private Dialogs() {}

    public static void showError(String title, String header, String msg) {
        show(Alert.AlertType.ERROR, title, header, msg);
    }

    public static void showInfo(String title, String msg) {
        show(Alert.AlertType.INFORMATION, title, null, msg);
    }

    public static void showWarning(String msg) {
        show(Alert.AlertType.WARNING, null, null, msg);
    }

    private static void show(Alert.AlertType type, String title, String header, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
