package com.group5.shuttle.service;

// ISP: Fortschritts- und Aktivitätsdaten des Übergabe-Workflows.
// Getrennt von IPartApproval, da Dashboard-Clients (MainController) nur diese
// Metriken brauchen und nie die Freigabe-Aktionen auslösen.
public interface ITakeoverProgress {
    double getProgress();
    boolean isTakeoverComplete();
    String getLastActivity();
}
